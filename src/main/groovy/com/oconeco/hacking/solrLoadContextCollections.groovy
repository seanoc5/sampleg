package com.oconeco.hacking

import com.oconeco.corpusminder.CMHelper
import groovy.cli.picocli.CliBuilder
import groovy.sql.GroovyRowResult

/**
 * hacked up script to load content/docs by context into individual collections, and send them individual solr collections (creating collection as needed)
 */

import groovy.sql.Sql
import groovy.transform.Field
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.apache.solr.client.solrj.SolrClient
import org.apache.solr.client.solrj.response.UpdateResponse
import org.apache.solr.common.SolrInputDocument

@Field
Logger log = LogManager.getLogger(this.class.name)

String scriptName = this.class.simpleName
def cli = new CliBuilder(usage: "groovy ${scriptName} [option]", name: "${scriptName}")
cli.h(longOpt: 'help', 'display usage')
//cli.b(longOpt: 'batchSize', type: Integer, defaultValue: "100", 'batch size for processing')
//cli.b(longOpt: 'batchsize', type: Integer, defaultValue: '50', 'WIP placeholder for batching requests (currently just triggers a log.info status message)')
//cli.c(longOpt: 'corpusminder', type: String, defaultValue: 'corpusminder', 'Solr collection name for CorpusMinder collection')
cli.d(longOpt: 'database', type: String, defaultValue: 'cm_dev', 'sql database to connect to')
cli.l(longOpt: 'solrbase', type: String, defaultValue: 'wsl2ubuntu', required: true, args: 1, 'solr base url')
cli.p(longOpt: 'password', type: String, required: true, args: 1, 'database password')
cli.s(longOpt: 'server', type: String, defaultValue: 'localhost', 'database (postgres) server')
cli.t(longOpt: 'configset', type: String, defaultValue: 'analysis', 'solr configset (must already exist in solr) to use for new collections')
cli.u(longOpt: 'user', type: String, required: true, args: 1, 'database user')
// parse and process parameters
def options = cli.parse(args)
if (options?.h) cli.usage()
else println "Hello ${options.u ? options.u : 'World'}"


// Database connection information
final String dbUrl = "jdbc:postgresql://${options.server}:5432/${options.database}"
final String dbUser = options.u
final String dbPassword = options.p
//final String cmCollection = options.c
final int BATCH_SIZE = 50


//String collection = 'corpusminder'
String solrBase = options.solrbase
final SolrClient solrClient = CMHelper.buildSolrClient(solrBase, 8983)
final List<String> existingCollections = CMHelper.loadSolrCollections(solrClient)

// Initialize a database connection
final Sql sql = Sql.newInstance(dbUrl, dbUser, dbPassword, 'org.postgresql.Driver')

String configset = options.configset
log.info "Starting script ${scriptName} :: configset:$configset -- solrBase: $solrBase "


List<GroovyRowResult> contextRows = CMHelper.loadContextRows(sql, 'select * from context')
int ctxRowNum = 0
contextRows.each { contextRow ->
    ctxRowNum++
    String contextName = contextRow.label
    String collectionName = CMHelper.sanitizeCollectionName(contextName)
    log.info "${ctxRowNum}) STARTING context (id:contextName)==(${contextRow.id}:${contextRow.label})  --  collection name($collectionName)..."

    boolean collectionExists = existingCollections.contains(collectionName)// {it = collectionName}
    if (collectionExists) {
        log.info "\t\t${ctxRowNum}) Collection($collectionName) already exists, no need to create..."
    } else {
        def createCollectionResponse = CMHelper.createCollection(collectionName, solrClient, configset)
        if (createCollectionResponse.success) {
            collectionExists = true
        } else {
            log.warn "${ctxRowNum}) Could not find existing collection, nor could we create a new one (resonse: $createCollectionResponse)"
        }
    }
    if (collectionExists) {
        Long contextId = contextRow.id as Long
        def docRows = CMHelper.loadContextContentRows(sql, contextId)
        List<SolrInputDocument> docSidList = []
        int docRowNum = 0
        log.info "\t\t$ctxRowNum) $contextName has (${docRows.size()}) documents to process... "
        docRows.each { docRow ->
            docRowNum++
            if (docRowNum % BATCH_SIZE == 0) {
                log.info "\t\t$ctxRowNum:$docRowNum) status: still working on collection:($collectionName)..."
            }
            SolrInputDocument solrInputDocument = CMHelper.createContentSolrInputDoc(docRow)
            docSidList << solrInputDocument
        }
        if (docSidList?.size() > 0) {
            // send context docs to solr for indexing/searching
            UpdateResponse updateResponse = solrClient.add(collectionName, docSidList, 100)
            log.info "\t\t${ctxRowNum}) Doc count(${docSidList.size()})  --  Solr updateResponse: ${updateResponse}"

            // update context records in db, set solr_index_date
//        def (Object rowCount, String idList) = updateDBContexts(sql)

//            def contextSolrDoc = CMHelper.createContextSolrInputDoc(contextRow)
//            def ctxUpdateRsp = solrClient.add(cmCollection, contextSolrDoc, 1000)
//            if (ctxUpdateRsp.status == 0) {
//                log.debug "\t\tContext Solr doc: $contextSolrDoc"
//                int qtime = ctxUpdateRsp.response.responseHeader.QTime
//                log.debug "\t\tAdded Context Solr doc response(QTime:$qtime): $ctxUpdateRsp"
//            } else {
//                log.warn "Problem adding context doc, response: ($ctxUpdateRsp) -- doc:$contextSolrDoc"
//            }
        } else {
            log.warn "${ctxRowNum}) NO content docs for context:$contextName ... ???..."
        }
        log.info "\t\t${ctxRowNum}) FINISHED context: (${contextRow.id}:${contextRow.label}..."
    } else {
        log.warn "Trouble creating collection ($contextName)..."
    }

}

log.info "Closing solr client..."
solrClient.close()

log.info("Done!?")

