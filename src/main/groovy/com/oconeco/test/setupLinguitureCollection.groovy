package com.oconeco.test

import groovy.json.JsonSlurper
import groovy.transform.Field
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.core.Logger
import org.apache.solr.client.solrj.SolrClient
import org.apache.solr.client.solrj.impl.Http2SolrClient
import org.apache.solr.client.solrj.request.CollectionAdminRequest
import org.apache.solr.client.solrj.request.schema.AnalyzerDefinition
import org.apache.solr.client.solrj.request.schema.FieldTypeDefinition
import org.apache.solr.client.solrj.request.schema.SchemaRequest
import org.apache.solr.client.solrj.response.CollectionAdminResponse
import org.apache.solr.client.solrj.response.UpdateResponse
import org.apache.solr.client.solrj.response.schema.SchemaRepresentation
import org.apache.solr.client.solrj.response.schema.SchemaResponse

@Field
Logger log = LogManager.getLogger(this.class.name);
log.info "Starting script: ${this.class.name}..."

String host = 'dell'
int port = 8983
String collection = 'linguiture'
String baseUrl = "http://$host:$port/solr"
List<Map<String, Object>> collectionDetails = [
        [collectionName: 'linguiture', config: '_default', shards: 'shard1', numReplicas: 1],
        [collectionName: 'corpusminder', config: '_default', shards: 'shard1', numReplicas: 1],
        [collectionName: 'solr_system', config: '_default', shards: 'shard1', numReplicas: 1]
]

SolrClient client = new Http2SolrClient.Builder(baseUrl).build()

def biz = cleanAndSetupCollections(client, collectionDetails)


def parser = new JsonSlurper()

URL schemaUrl = new URL("http://dell:8983/solr/${collection}/schema")
def json = parser.parse(schemaUrl)

Map schema = json.schema
log.info "Schema object size: ${schema.size()}"

SchemaRequest request = new SchemaRequest();
SchemaResponse response = request.process(client, collection);
SchemaRepresentation schemaRepresentation = response.getSchemaRepresentation();
log.info "\t\tSchema Rep: $schemaRepresentation"

List<Map<String, Object>> responses2 = cleanAndCustomizeFieldTypes(schema, client, collection)
List<Map<String, Object>> responses = cleanSchema(schema, client, collection)

client.close()

log.info "Done...?"


// ---------------------- Methods -----------------
public List<Map<String, Object>> cleanSchema(Map<String, Object> schema, SolrClient client, String collection) {
    List<UpdateResponse> responseList = []

    List rspList = cleanCopyFields(schema, client, collection)
    responseList.addAll(rspList)

    rspList = cleanAndCustomizeFields(schema, client, collection)
    responseList.addAll(rspList)

    return responseList
}

public List<UpdateResponse> cleanCopyFields(Map<String, Object> schema, SolrClient client, String collection) {
    List<UpdateResponse> responseList = []

    List<Map<String, Object>> copyFields = schema.copyFields
    copyFields.each {
        String src = it.source
        def dest = it.dest
        if (dest instanceof String) {
            dest = [dest]
        }
        if (dest[0].endsWith('_str')) {
            log.info "\t\tremove dynamic copyfield rule ('_str'): $src -> $dest"
            SchemaRequest.DeleteCopyField deleteCopyField = new SchemaRequest.DeleteCopyField(src, dest)
            SchemaResponse.UpdateResponse rc = deleteCopyField.process(client, collection)
            responseList.add(rc)
            log.info "\t\tdelete result: $rc"
        } else if (dest[0].contains('bucket')) {
            log.info "\t\tremove dynamic copyfield rule (NOT _str): $src -> $dest"
            SchemaRequest.DeleteCopyField deleteCopyField = new SchemaRequest.DeleteCopyField(src, dest)
            def rc = deleteCopyField.process(client, collection)
            responseList.add(rc)
            log.info "\t\tdelete result: $rc"
        } else {
            log.info "Leaving unknown copyField: $src -> $dest"
        }
    }
    return responseList
}


/**
 * hacked beginning of function to remove old/junk fields, and add (hardcoded) customized fields as needed
 * todo -- make this more flexible and config driven
 * @param schema
 * @param client
 * @param collection
 * @return
 */
public List<UpdateResponse> cleanAndCustomizeFields(Map<String, Object> schema, SolrClient client, String collection) {
    List<UpdateResponse> responseList = []

    List<Map<String, Object>> fields = schema.fields
/*
    def txtsEnField = fields['text_']
    fields.each {
        String name = it.name
        String type = it.type
        log.info "Field name: $name -- type:$type"
    }
*/
    return responseList
}

public List<UpdateResponse> cleanAndCustomizeFieldTypes(Map<String, Object> schema, SolrClient client, String collection) {
    List<UpdateResponse> responseList = []

    List<Map<String, Object>> fieldTypes = schema.fieldTypes

    Map<String, Object> txtEnField = fieldTypes.find { it.name = 'text_en' }

//    def foo = createFieldTypeRequest('sean_test_en')
    def bar = createFieldTypeModifyRequest("text_es", client, collection)

//    def SchemaRequest.AddFieldType
//            fieldTypes . each {
//        String name = it.name
//        String type = it.type
//        log.info "Field name: $name -- type:$type"
//    }

    return responseList
}

public List<CollectionAdminResponse> cleanAndSetupCollections(Http2SolrClient client, List<Map<String, Object>> collectionsToCreate) {
    List<CollectionAdminResponse> responses = []
    CollectionAdminRequest.List collectionAdminRequest = new CollectionAdminRequest.List()
    CollectionAdminResponse car = collectionAdminRequest.process(client)
    List collections = car.response.collections
    List<String> junkCollections = collections.findAll { String colLName ->
        colLName ==~ /(foo|linguture|._designer_.*AUTOCREATED)/
    }
    junkCollections.each {
        CollectionAdminRequest.Delete adminReq = new CollectionAdminRequest.Delete(it)
        CollectionAdminResponse resp = adminReq.process(client)
        responses.add(resp)
        log.info "Delete coll($it) -- response:$resp"
    }

    collectionsToCreate.each { Map collDetails ->
        String collName = collDetails.collectionName
        String config = collDetails.config
        String shards = collDetails.shards
        int numReplicas = collDetails.numReplicas

        if (collections.contains(collName)) {
            log.info "Collection ($collName) already exists, nothing more to do..."
        } else {
            CollectionAdminRequest.Create createReq = new CollectionAdminRequest.Create(collName, config, shards, numReplicas)
            CollectionAdminResponse resp = createReq.process(client)
            responses.add(resp)
            log.info "Create Req response: $resp"
        }
    }

    return responses
}


def createFieldTypeModifyRequest(String fieldTypeName, SolrClient client, String collName) {
    SchemaRequest.FieldType originalFieldType = new SchemaRequest.FieldType(fieldTypeName);
    def fieldType = originalFieldType.process(client, collName)
    Map<String, Object> updatedFieldAttributes = fieldType.getFieldType()
    // Modifies the original attributes
    updatedFieldAttributes.put("type", fieldTypeName);

    // Updates the field type of the field
    SchemaRequest.ReplaceField replaceFieldRequest = new SchemaRequest.ReplaceField(updatedFieldAttributes);

    // Processes the requests
//       List<SchemaRequest.Update> list = new ArrayList<>(3);
////       list.add(addFieldTypeRequest);
//       list.add(replaceFieldRequest);
//       SchemaRequest.MultiUpdate multiUpdateRequest = new SchemaRequest.MultiUpdate(list);
//       SchemaResponse.UpdateResponse multipleUpdatesResponse = multiUpdateRequest.process(getSolrClient());
    return replaceFieldRequest
}


SchemaRequest.AddFieldType createFieldTypeRequest(String fieldTypeName) {
    FieldTypeDefinition fieldTypeDefinition = new FieldTypeDefinition();
    Map<String, Object> fieldTypeAttributes = new LinkedHashMap<>();
    fieldTypeAttributes.put("name", fieldTypeName);
    fieldTypeAttributes.put("class", "solr.TextField");
    fieldTypeDefinition.setAttributes(fieldTypeAttributes);

    AnalyzerDefinition indexAnalyzerDefinition = new AnalyzerDefinition();
    Map<String, Object> iTokenizerAttributes = new LinkedHashMap<>();
    iTokenizerAttributes.put("class", "standard");
//    iTokenizerAttributes.put("delimiter", "/");
    indexAnalyzerDefinition.setTokenizer(iTokenizerAttributes);
    fieldTypeDefinition.setIndexAnalyzer(indexAnalyzerDefinition);

    AnalyzerDefinition queryAnalyzerDefinition = new AnalyzerDefinition();
    Map<String, Object> qTokenizerAttributes = new LinkedHashMap<>();
    qTokenizerAttributes.put("class", "solr.KeywordTokenizerFactory");
    queryAnalyzerDefinition.setTokenizer(qTokenizerAttributes);
    fieldTypeDefinition.setQueryAnalyzer(queryAnalyzerDefinition);
    return new SchemaRequest.AddFieldType(fieldTypeDefinition);
}

/*
    Map<String, Object> fieldTypeAttributes = new LinkedHashMap<>();
    String fieldName = "text_ms_en";
    fieldTypeAttributes.put("name", fieldName);
    fieldTypeAttributes.put("class", "solr.TextField");
//    fieldTypeAttributes.put("omitNorms", true);
//    fieldTypeAttributes.put("positionIncrementGap", 0);
    FieldTypeDefinition fieldTypeDefinition = new FieldTypeDefinition();
    fieldTypeDefinition.setAttributes(fieldTypeAttributes);
    SchemaRequest.AddFieldType addFieldTypeRequest =
            new SchemaRequest.AddFieldType(fieldTypeDefinition);
    SchemaResponse.UpdateResponse addFieldTypeFirstResponse =
            addFieldTypeRequest.process(getSolrClient());
    assertValidSchemaResponse(addFieldTypeFirstResponse);

 */
