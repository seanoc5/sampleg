import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.core.Logger
import org.apache.solr.client.solrj.SolrClient
import org.apache.solr.client.solrj.impl.CloudSolrClient
import org.apache.solr.common.cloud.ClusterState

Logger log = LogManager.getLogger(this.class.name);
log.info "Starting script: ${this.class.name}..."

final List<String> zkServers = new ArrayList<String>();
zkServers.add("dell:2181");
//zkServers.add("zookeeper2:2181");
//zkServers.add("zookeeper3:2181");
final SolrClient client = new CloudSolrClient.Builder(zkServers, Optional.empty()).build();

ClusterState state = client.getClusterState()

client.setDefaultCollection("mycollection");
client.connect();

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
