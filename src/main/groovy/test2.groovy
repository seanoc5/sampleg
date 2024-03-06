import com.johnsnowlabs.nlp.SparkNLP
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.apache.spark.sql.Dataset
import org.apache.spark.sql.Row
import org.apache.spark.sql.SparkSession


Logger log = LogManager.getLogger(this.class.name);
log.info "Starting script: ${this.class.name}..."

SparkSession sparkNlp = SparkNLP.start();


// Initialize Spark session
SparkSession spark = SparkSession
  .builder()
  .appName("Spark NLP Example")
  .master("local[*]") // Use "local[*]" to run with as many worker threads as logical cores on your machine
  .config("spark.jars.packages", "com.johnsnowlabs.nlp:spark-nlp_2.12:3.4.0") // Replace "3.4.0" with the version of Spark NLP you are using
  .getOrCreate()

// Load your text file
//String filePath = "/home/sean/work/content-analysis/src/main/resources/sample-doc-short.txt" // Update this path to your text file
String filePath = "/home/sean/work/content-analysis/src/main/resources/sample-doc.txt" // Update this path to your text file
log.info "path: $filePath"
Dataset<Row> df = spark.read().text(filePath).toDF('textCol')

log.info "Done...?"
