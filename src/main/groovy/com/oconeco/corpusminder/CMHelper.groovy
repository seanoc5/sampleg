package com.oconeco.corpusminder

import groovy.sql.GroovyRowResult
import groovy.sql.Sql
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.apache.solr.client.solrj.SolrClient
import org.apache.solr.client.solrj.impl.Http2SolrClient
import org.apache.solr.client.solrj.request.CollectionAdminRequest
import org.apache.solr.client.solrj.response.CollectionAdminResponse
import org.apache.solr.common.SolrInputDocument
import org.postgresql.jdbc.PgArray

public class CMHelper {
    public static Logger log = LogManager.getLogger(this.class.name);

    /**
     * build solr input doc for context information
     * @param row
     * @param contentDocs
     * @return SolrInputDocument
     */
    public static SolrInputDocument createContextSolrInputDoc(GroovyRowResult row, def contentDocs=null) {
        SolrInputDocument sid = new SolrInputDocument()
        sid.setField('id', row.id)
        sid.setField('type_s', 'Context')
        sid.setField('title', row.label)
        sid.setField('description', row.description)
        sid.setField('level', row.level)
    //    sid.setField('body_text', row.body_text)
    //    sid.setField('queryCount_i', row.queryCount)
        sid.setField('last_updated', row.last_updated)
        sid.setField('date_created', row.date_created)
        return sid
    }


    /**
     * build solr input doc for Doc/Content information
     * @param row
     * @param contentDocs
     * @return SolrInputDocument
     */
    public static SolrInputDocument createContentSolrInputDoc(GroovyRowResult row) {
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


    /**
     *get context information from database (e.g. cm_dev, dm_prod....)
     * @param sql object
     * @param sql statement for select
     * @return all relevant rows (todo -- look at batching when contexts get too many)
     */
    public static List<GroovyRowResult> loadContextRows(Sql sql, String contextsSql) {
        log.info "Load context content/docs with sql string: $contextsSql"
        List<GroovyRowResult> contextRows = sql.rows(contextsSql)
    }


    /**
     *get Content/Doc information from database (e.g. cm_dev, dm_prod....)
     * @param sql object
     * @param context id to get just those docs relevant to the context
     * @return all relevant rows (todo -- look at batching when contexts get too many)
     *
     * note: this is likely most useful for creating individual solr collection per context,and do term analysis on that set of content (e.g. terms componenet)
     */
    public static  List<GroovyRowResult> loadContextContentRows(Sql sql, Long contextId) {
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
        ctx.id = :contextId
    group by
        doc.id, doc.title, doc.description, doc.display_order  
    '''
        log.debug "contentQuery: $contentQuery"

        List<GroovyRowResult> contentRows = sql.rows(contentQuery, [contextId: contextId])
        return contentRows
    }


    /*public List updateDBContexts(Sql sql) {
        String idList = contextC.join(',')
        String updateSql = "update content set solr_index_date = now() where id in (${idList})"
        def rowCount = sql.executeUpdate(updateSql)
        log.debug "Batch IDS of updated content docs in db: $batchIds"
        [rowCount, idList]
    }*/


    /**
     * simple wrapper around solrj to build a client for CorpusMinder use (or others, but mostly CM)
     * @param host
     * @param port
     * @param collection - pass empty or null (or nothing) to get a base client
     * Note: base client will need collection param on each collection-specific call
     * @return SolrClient
     */
    public static SolrClient buildSolrClient(String host, int port, String collection = null) {
        String baseUrl = "http://$host:$port/solr"
        if(collection){
            baseUrl = baseUrl + "/$collection"
            log.info "Build solr client with collection ($collection) url: $baseUrl"
        } else {
            log.info "no collection given, setting to base url: $baseUrl"
        }
        SolrClient solrClient = new Http2SolrClient.Builder(baseUrl).build()
    }


    /**
     * get a list of all current collection names. most helpful for deciding if we use existing collection,
     * or create new collection for a given context
     * @param solrClient
     * @return list of existing collection names
     */
    public static List<String> listSolrCollections(SolrClient solrClient){
        List<String> existingCollections = CollectionAdminRequest.listCollections(solrClient);
        return existingCollections
    }


    /**
     * Simple wrapper to create a collection -- leaves room for tuning and improving the process in the future (if needed)
     * @param collectionName
     * @param solrClient
     * @param replicas
     * @param shards
     * @param configSet
     * @return solr response
     */
    public static CollectionAdminResponse createCollection(String collectionName, SolrClient solrClient, String configSet, int replicas = 1, int shards = 1) {
        CollectionAdminRequest.Create creator = CollectionAdminRequest.createCollection(collectionName, configSet, shards, replicas)
        CollectionAdminResponse createResponse = creator.process(solrClient)
    }


    /**
     * simeple wrapper for how to convert free-form text into something safe/sane for a collection name
     * @param contextName
     * @return sanitized name
     */
    public static  String sanitizeCollectionName(String contextName) {
        contextName.trim().replaceAll(/[^A-Za-z0-9]/, "_")
    }

}
