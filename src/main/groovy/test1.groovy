import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.apache.spark.sql.SparkSession

//import com.johnsnowlabs.nlp.DocumentAssembler
//import com.johnsnowlabs.nlp.annotators.Tokenizer
//import org.apache.spark.sql.SparkSession
//import com.johnsnowlabs.nlp.embeddings.EmbeddingsHelper;
//import com.johnsnowlabs.nlp.pretrained.PretrainedPipeline;
//import org.apache.spark.ml.Pipeline;
//import org.apache.spark.ml.PipelineModel;
//import org.apache.spark.ml.PipelineStage;

//import com.johnsnowlabs.nlp.util.


Logger log = LogManager.getLogger(this.class.name);
log.info "Starting script: ${this.class.name}..."

//SparkSession spark = com.johnsnowlabs.nlp.SparkNLP.start();

// Initialize Spark session
//SparkSession spark = SparkSession
//  .builder()
//  .appName("Spark NLP Example")
//  .master("local[*]") // Use "local[*]" to run with as many worker threads as logical cores on your machine
////  .config("spark.jars.packages", "com.johnsnowlabs.nlp:spark-nlp_2.12:3.4.0") // Replace "3.4.0" with the version of Spark NLP you are using
//  .getOrCreate()

// Initialize Spark NLP
//SparkNLP.start()
Map<String, String> argsMap = ['na':'none']
SparkSession spark = com.johnsnowlabs.nlp.SparkNLP.start(false, false, true, '4gb', './', './', './', argsMap);

//DocumentAssembler document = new DocumentAssembler();
//document.setInputCol("text");
//document.setOutputCol("document");
//document.setCleanupMode("disabled");
//
//Tokenizer tokenizer = new Tokenizer();
//tokenizer.setInputCols(new String[]{"document"});
//tokenizer.setOutputCol("token");

//Pipeline pipeline = new Pipeline();
//pipeline.setStages(new PipelineStage[]{document, tokenizer});


/*
SparkNLPConfig sparkNLPConfig = new SparkNLPConfig.Builder()
    .sparkSession(spark)
    // Add any other configurations here if necessary
    .build()
    SparkNLP.start(sparkNLPConfig)
*/

// Load Pretrained Pipeline
//PretrainedPipeline pipeline = new PretrainedPipeline("explain_document_ml")

// Load your text file
//String filePath = "/home/sean/work/content-analysis/src/main/resources/sample-doc-short.txt" // Update this path to your text file
String filePath = "/home/sean/work/content-analysis/src/main/resources/sample-doc.txt" // Update this path to your text file
log.info "path: $filePath"
def df = spark.read().text(filePath).toDF('textCol')
//def df = spark.read.text(filePath).toDF("text")

// Apply NLP pipeline to your data
//def result = pipeline.transform(df)

// Show results
//result.show(false)


log.info "Done...?"
