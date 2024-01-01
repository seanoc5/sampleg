package com.oconeco.test

import groovy.json.JsonSlurper
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.core.Logger
import org.apache.solr.client.solrj.SolrClient
import org.apache.solr.client.solrj.impl.Http2SolrClient
import org.apache.solr.client.solrj.request.schema.SchemaRequest

Logger log = LogManager.getLogger(this.class.name);
log.info "Starting script: ${this.class.name}..."

String host = 'dell'

int port = 8983
String collection = 'linguiture'
collection = 'solr_system'
String baseUrl = "http://$host:$port/solr"
SolrClient client = new Http2SolrClient.Builder(baseUrl).build()
//def ping = client.ping(collection)
//log.info "Ping response: $ping"

//BS solr api found lacking, can't find anything to give me/download the configset I want!!?!
//final SolrRequest configsetRequest = new ConfigSetAdminRequest.List()
//def configsList = configsetRequest.process(client)
//log.info "ConfigSetAdminRequest.list(): $configsList"

//final SolrRequest configInfoRequest = new ConfigSetAdminRequest.Info()      // broken/non-existent in solr 9??
//def configsList = configsetRequest.process(client)      // broken??

URL schemaFileUrl = new URL('http://dell:8983/solr/solr_system/admin/file?_=1703995554287&file=lang&wt=json')
def parser = new JsonSlurper()
//def schema = parser.parse(schemaFileUrl)
//log.info "Schema size: ${schema.size()}"

URL schemaUrl = new URL("http://dell:8983/solr/${collection}/schema")
//URL copyFieldsUrl = new URL('http://dell:8983/solr/solr_system/schema/copyFields&wt=json')
def json = parser.parse(schemaUrl)
Map schema = json.schema
log.info "Schema object size: ${schema.size()}"

def dynamicFields = schema.dynamicFields
List<Map<String, Object>> copyFields = schema.copyFields
copyFields.each {
    String src = it.source
    def dest = it.dest
    if(dest instanceof String){
        dest = [dest]
    }
    if(dest[0].endsWith('_str')){
        log.info "\t\tremove dynamic copyfield rule ('_str'): $src -> $dest"
        SchemaRequest.DeleteCopyField deleteCopyField = new SchemaRequest.DeleteCopyField(src, dest)
        def rc = deleteCopyField.process(client, collection)
        log.info "\t\tdelete result: $rc"
    } else if(dest[0].contains('bucket')) {
        log.info "\t\tremove dynamic copyfield rule (NOT _str): $src -> $dest"
        SchemaRequest.DeleteCopyField deleteCopyField = new SchemaRequest.DeleteCopyField(src, dest)
        def rc = deleteCopyField.process(client, collection)
        log.info "\t\tdelete result: $rc"
    } else {
        log.info "Leaving unknown copyField: $src -> $dest"
    }
}

//CollectionAdminRequest.Create req = new CollectionAdminRequest.Create(collection, );
//response = client.request(req)
//.setReplicationFactor(1)
//.setConfigName("bar")
//.process(cloudClient);

log.info "Done...?"
