import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.core.Logger
import org.apache.solr.client.solrj.SolrQuery
import org.apache.solr.client.solrj.impl.CloudHttp2SolrClient
import org.apache.solr.client.solrj.request.CollectionAdminRequest
import org.apache.solr.client.solrj.response.CollectionAdminResponse
import org.apache.solr.common.util.NamedList
import org.apache.solr.common.util.SimpleOrderedMap

Logger log = LogManager.getLogger(this.class.name);
log.info "Starting script: ${this.class.name}..."


String zkHostString = "localhost:2181" // Your ZooKeeper host string
List urls = [zkHostString]
Optional<String> chroot = Optional.empty()
//Optional<String> chroot = Optional.of('/solr')
def solrClient = new CloudHttp2SolrClient.Builder(urls, chroot ).build()
def query = new SolrQuery('*:*')
def results = solrClient.query('test', query)
log.info "REsult: $results"

CollectionAdminRequest.ClusterStatus request = new CollectionAdminRequest.ClusterStatus();
CollectionAdminResponse response = request.process(solrClient);

// The response contains the cluster status
NamedList<Object> responseValues = response.getResponse();
def cluster = responseValues.get('cluster')             // demo of "proper" get call
SimpleOrderedMap collections = responseValues.cluster.collections    // groovy syntactic sugar to skip getters, and just navigate through response structure
collections.each {Map.Entry e ->
    String colName = e.key
    log.info "Collection: ${colName}"
    Map valMap = e.value
    log.info "\t\tStatus:${valMap.health}"
    if(valMap.shards) {
//        valMap.shards.each { Map smap ->
        valMap.shards.each {String sname, Map svals ->
            String shealth = svals.health
            String sstate = svals.state
            if(shealth=='GREEN' && sstate == 'active') {
                log.info "\t\tshard ${sname} -- ${shealth} :: ${sstate} (${svals.replicas})"
            } else {
                log.warn "PROBLEM?? shard ${sname} -- ${shealth} :: ${sstate} (${svals.replicas})"
            }
        }
    } else {
        log.warn "No shards in collection: ${e.key}}"
    }
}
solrClient.close()

//final SolrClient client = new SolrClient().Builder(zkServers, Optional.empty()).build();
//def client = CloudHttp2SolrClient.Builder.

//final String solrUrl = "http://dell:8983/solr";
//Http2SolrClient client =  new Http2SolrClient.Builder(solrUrl)
//    .withConnectionTimeout(10000, TimeUnit.MILLISECONDS)
//    .build();


//ZkStateReader zkStateReader = client.getZkStateReader();
//try {
//  boolean cont = true;
//
//    zkStateReader.updateClusterState(true);
//    ClusterState clusterState = zkStateReader.getClusterState();
//    Map<String, Slice> slices = clusterState.getSlicesMap(collection);
//    Preconditions.checkNotNull("Could not find collection:" + collection, slices);
//
//        String state = shard.getValue().getStr(ZkStateReader.STATE_PROP);
//        if ((state.equals(Replica.State.RECOVERING) || state.equals(Replica.State.DOWN))
//            && clusterState.liveNodesContain(shard.getValue().getStr(
//            ZkStateReader.NODE_NAME_PROP))) {
//          sawLiveRecovering = true;

//ClusterState state = client.get


//ZkStateReader reader = client.getZkStateReader();
//Collection<Slice> slices = reader.getClusterState().getSlices("mycollection");
//Iterator<Slice> iter = slices.iterator();
//
//while (iter.hasNext()) {
//    Slice slice = iter.next();
//    for (Replica replica : slice.getReplicas()) {
//
//        System.out.println("replica state for " + replica.getStr("core") + " : " + replica.getStr("state"));
//
//        System.out.println(slice.getName());
//        System.out.println(slice.getState());
//    }
//}

log.info "Done...?"
