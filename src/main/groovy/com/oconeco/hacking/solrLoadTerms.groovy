package com.oconeco.hacking

import com.oconeco.corpusminder.CMHelper
import groovy.cli.picocli.CliBuilder
import groovy.sql.GroovyResultSet
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

def cli = new CliBuilder(usage: "groovy ${scriptName} [option]", name: "${scriptName}")
cli.h(longOpt: 'help', 'display usage')
cli.c(longOpt: 'collection', 'display usage')
cli.s(longOpt: 'solrurl', type: String, defaultValue: 'http://wsl2ubuntu:8983/solr', 'solr server url')

// parse and process parameters
def options = cli.parse(args)


String baseUrl = options.solrurl
SolrClient solrClient = new Http2SolrClient.Builder(baseUrl).build()
List<String> existingCollections = CMHelper.listSolrCollections(solrClient)


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
