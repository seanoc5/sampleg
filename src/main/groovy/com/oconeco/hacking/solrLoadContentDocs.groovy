package com.oconeco.hacking

import groovy.cli.picocli.CliBuilder
import groovy.sql.GroovyResultSet
import groovy.sql.Sql
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.apache.solr.client.solrj.SolrClient
import org.apache.solr.client.solrj.impl.Http2SolrClient
import org.apache.solr.client.solrj.response.UpdateResponse
import org.apache.solr.common.SolrInputDocument
import org.postgresql.jdbc.PgArray

Logger log = LogManager.getLogger(this.class.name);
String scriptName = this.class.simpleName
log.info "Starting script ${scriptName}"

// import of CliBuilder not shown
// specify parameters
def cli = new CliBuilder(usage: "groovy ${scriptName} [option]", name: "${scriptName}")
//cli.usageMessage.with {
//    headerHeading("@|bold,underline Header heading:|@%n")
//    header("Header 1", "Header 2")                     // before the synopsis
//    synopsisHeading("%n@|bold,underline Usage:|@ ")
//    descriptionHeading("%n@|bold,underline Description heading:|@%n")
//    description("Description 1", "Description 2")      // after the synopsis
//    optionListHeading("%n@|bold,underline Options heading:|@%n")
//    footerHeading("%n@|bold,underline Footer heading:|@%n")
//    footer("Footer 1", "Footer 2")
//}

cli.h(longOpt: 'help', 'display usage')
cli.b(longOpt: 'batchSize', type: Integer, defaultValue: "100", 'batch size for processing')
cli.d(longOpt: 'database', type: String, defaultValue: 'cm_dev', 'sql database to connect to')
cli.p(longOpt: 'password', type: String, required: true, args: 1, 'database password')
cli.s(longOpt: 'server', type: String, defaultValue: 'localhost', 'database (postgres) server')
cli.u(longOpt: 'user', type: String, required: true, args: 1, 'database user')

// parse and process parameters
def options = cli.parse(args)
if (options?.h) {
    cli.usage()
} else {
    println "Hello ${options.u ? options.u : 'World'}"
}


// Database connection information
def dbUrl = "jdbc:postgresql://${options.server}:5432/${options.database}"
def dbUser = options.u
//def dbPassword = System.getenv('dbpass')
def dbPassword = options.p
Integer batchSize = options.batchSize


// Initialize a database connection
Sql sql = Sql.newInstance(dbUrl, dbUser, dbPassword, "org.postgresql.Driver")

String contextsSql = 'select * from context'

String contentQuery = '''
select
    doc.id, doc.display_order,
    doc.content_type, doc.content_subtype,
    doc.mime_type, doc.language, 
    doc.title, doc.description, doc.author,  
    doc.body_text, doc.structured_content, 
    doc.text_size, doc,structured_size,
    doc.publish_date, doc.last_updated, doc.solr_index_date
    , array_agg(DISTINCT src.label) sources
    , array_agg(DISTINCT s.query) queries
    , count(DISTINCT s.query) queryCount
    , array_agg(DISTINCT sr.type)  resultTypes
    , array_agg(DISTINCT ctx.label) contexts
from content doc
    left join public.source src on src.id = doc.source_id
    left join public.search_result_content _src on doc.id = _src.content_id
    left join search_result sr on _src.search_result_documents_id = sr.id
    left join search s on sr.search_id = s.id
    left join context ctx on s.context_id=ctx.id
where
    doc.solr_index_date is null 
    or 
    doc.solr_index_date < doc.last_updated
group by
    doc.id, doc.title, doc.description, doc.display_order  
limit :batchSize
'''
log.info "contentQuery: $contentQuery"

String host = 'dell'
int port = 8983
String collection = 'corpusminder'
String baseUrl = "http://$host:$port/solr/$collection"
SolrClient solrClient = new Http2SolrClient.Builder(baseUrl).build()

List<SolrInputDocument> sidList = []
List batchIds = []
boolean moreRecords = true
int docCount=0
int batchCount = 0
while(moreRecords) {
    batchCount++
    sql.eachRow(contentQuery, [batchSize: options.batchSize]) { row ->
        batchIds << row.id
        SolrInputDocument sid = createSolrInputDoc(row)
        sidList << sid
        log.debug "$batchCount) Row: ${row.id}, ${row.title}"
    }
    UpdateResponse updateResponse = solrClient.add(sidList)
    if (updateResponse.status==0) {
        docCount += sidList.size()
        log.info "$batchCount) (docCount:$docCount) Solr updateResponse: ${updateResponse}"
        String idList = batchIds.join(',')
        String updateSql = "update content set solr_index_date = now() where id in (${idList})"
        def rowCount = sql.executeUpdate(updateSql)
        log.debug "Batch IDS of updated content docs in db: $batchIds"
        if (rowCount == batchIds.size()) {
            log.info "\t\t$batchCount) updated row count:(${rowCount})==BatchID size(${batchIds.size()})"
        } else {
            log.warn "$batchCount) Difference between rowcount:$rowCount and idlist size: ${idList.size()}"
        }
        sidList = []
    } else {
        log.warn "$batchCount) Problem saving batch?? Solr update response: $updateResponse"
    }
    if(batchIds.size() == batchSize) {
        batchIds = []
        moreRecords = true
    } else if(batchIds.size() < batchSize) {
        log.info "$batchCount) Batch ID size(${batchIds.size()}) less than batch size ($batchSize), so assume we have batched through all applicable records, break loop and wrap up... "
        moreRecords = false
    } else {
        log.warn "$batchCount) Batch ID size(${batchIds.size()}) GREATER THAN (??!?) batch size($batchSize)... how did this happen?? Canceling loop, and hoping some smart human reads this message and fixes it... "
        moreRecords = false
    }
}

log.info "Closing solr client..."
solrClient.close()

log.info("Done!?")


SolrInputDocument createSolrInputDoc(GroovyResultSet row) {
    SolrInputDocument sid = new SolrInputDocument()
    sid.setField('id', row.id)
    sid.setField('type_s', 'Content')
    sid.setField('title', row.title)
    sid.setField('description', row.description)
    sid.setField('author', row.author)
    sid.setField('body_text', row.body_text)
    sid.setField('structured_content', row.structured_content)
    sid.setField('queryCount_i', row.queryCount)
    sid.setField('display_order', row.queryCount)
    sid.setField('text_size', row.text_size)
    sid.setField('structured_size', row.structured_size)
    sid.setField('publish_date', row.publish_date)
    sid.setField('last_updated', row.last_updated)
//    sid.setField('', row. )
//    sid.setField('', row. )

    sid.setField('contexts', row.contexts.getArray())
    sid.setField('queries', row.queries.getArray())

    sid.setField('content_type', row.content_type)
    sid.setField('content_subtype', row.content_subtype)
    sid.setField('mime_type', row.mime_type)
    sid.setField('language', row.language)

    PgArray sources = row.sources
    String s = sources.toString()
    def ary = sources.getArray()
    sid.setField('source_ss', sources.getArray())
//    sid.setField('', )
//    sid.setField('', )

    return sid
}
