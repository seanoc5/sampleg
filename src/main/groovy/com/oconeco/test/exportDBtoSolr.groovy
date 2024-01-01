package com.oconeco.test
import groovy.sql.Sql
import groovy.transform.Field
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.core.Logger
import org.apache.solr.client.solrj.SolrClient
import org.apache.solr.client.solrj.impl.Http2SolrClient
import org.apache.solr.client.solrj.response.UpdateResponse
import org.apache.solr.common.SolrDocumentList
import org.apache.solr.common.SolrInputDocument

import java.sql.ResultSet

Logger log = LogManager.getLogger(this.class.name);
log.info "Starting script: ${this.class.name}..."

String host = 'dell'
@Field
Date batchDate = new Date()

int port = 8983
String collection = 'corpusminder'
String baseUrl = "http://$host:$port/solr/$collection"
SolrClient client = new Http2SolrClient.Builder(baseUrl).build()

String dbUrl = "jdbc:postgresql://${host}/cm_dev"
String dbUser = "corpusminder"
String dbPassword = "pass1234"
String dbDriver = "org.postgresql.Driver"

Sql sql = Sql.newInstance(dbUrl, dbUser, dbPassword, dbDriver)
SolrDocumentList solrDocuments = new SolrDocumentList()
int i = 0
sql.eachRow('SELECT * FROM context') { resultSet ->
    i++
    log.debug "$i) $resultSet "
    SolrInputDocument sid = createContextInputDoc(resultSet)
    solrDocuments.add(sid)
}
UpdateResponse response = client.add(solrDocuments, 100)
log.info "Solr Docs list: ${solrDocuments.size()} -- response: $response"

SolrInputDocument createContextInputDoc(ResultSet resultSet){
    SolrInputDocument sid = new SolrInputDocument()
    sid.addField('id', resultSet.id)
    sid.addField('type_s', 'Context')
    sid.addField('batchDate_tdt', batchDate)
    sid.addField('time_s', resultSet.time)
    sid.addField('dateCreated_tdt', resultSet.date_created)
    sid.addField('lastUpdated_tdt', resultSet.last_updated)
    sid.addField('label_txt_en', resultSet.label)
    sid.addField('description_t', resultSet.description)
//    sid.addField('', resultSet.)
    return sid
}


