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
String collection = 'linguture'
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

//URL schemaFileUrl = new URL('http://dell:8983/solr/solr_system/admin/file?_=1703995554287&file=lang&wt=json')
def parser = new JsonSlurper()
//def schema = parser.parse(schemaFileUrl)
//log.info "Schema size: ${schema.size()}"

String fieldDefXml = '''
 <fieldType name="text_en" class="solr.TextField" positionIncrementGap="100">
    <analyzer type="index">
      <tokenizer name="standard"/>
      <filter name="stop" ignoreCase="true" words="lang/stopwords_en.txt"/>
      <filter name="lowercase"/>
      <filter name="englishPossessive"/>
      <filter protected="protwords.txt" name="keywordMarker"/>
      <filter name="porterStem"/>
    </analyzer>
    <analyzer type="query">
      <tokenizer name="standard"/>
      <filter ignoreCase="true" synonyms="synonyms.txt" name="synonymGraph" expand="true"/>
      <filter name="stop" ignoreCase="true" words="lang/stopwords_en.txt"/>
      <filter name="lowercase"/>
      <filter name="englishPossessive"/>
      <filter protected="protwords.txt" name="keywordMarker"/>
      <filter name="porterStem"/>
    </analyzer>
  </fieldType>
'''

String fieldDefJson = '''
{
  "add-field-type": {
    "name": "text_en_ms"
    "class": "solr.TextField",
    "positionIncrementGap": "100",
    "analyzer": {
      "tokenizer": {
        "class": "solr.Standard"
        "filters": [
          {
            "class": "solr.LowerCaseFilterFactory"
          },
          {
            "class": "solr.TrimFilterFactory"
          },
          {
            "class": "solr.EnglishMinimalStemmer"
          }
        ]
      }
    }
  }
}'''
def json = new JsonSlurper().parseText(fieldDefJson)
URL addFieldTypeUrl = new URL("http://dell:8983/solr/${collection}/schema")


URL schemaUrl = new URL("http://dell:8983/solr/${collection}/schema")
json = parser.parse(schemaUrl)
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
        log.info "\t\tremove dynamic copyfield rule (BUCKET): $src -> $dest"
        SchemaRequest.DeleteCopyField deleteCopyField = new SchemaRequest.DeleteCopyField(src, dest)
        def rc = deleteCopyField.process(client, collection)
        log.info "\t\tdelete result: $rc"
    } else {
        log.info "Leaving unknown copyField: $src -> $dest"
    }
}

//.setReplicationFactor(1)
//.setConfigName("bar")
//.process(cloudClient);

client.close()
log.info "Done...?"
