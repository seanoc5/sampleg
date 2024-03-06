//import com.johnsnowlabs.nlp.DocumentAssembler
//import com.johnsnowlabs.nlp.SparkNLP
//import com.johnsnowlabs.nlp.annotators.Tokenizer
//import com.johnsnowlabs.nlp.annotators.sbd.pragmatic.SentenceDetector
//import org.apache.logging.log4j.LogManager
//import org.apache.logging.log4j.Logger
//import org.apache.spark.sql.SparkSession
//
//Logger log = LogManager.getLogger(this.class.name);
//log.info "Starting script: ${this.class.name}..."
//
//SparkSession sparkNlp = SparkNLP.start();
//
//
//// Initialize Spark session
////SparkSession spark = SparkSession
////  .builder()
////  .appName("Spark NLP Example")
////  .master("local[*]") // Use "local[*]" to run with as many worker threads as logical cores on your machine
////  .config("spark.jars.packages", "com.johnsnowlabs.nlp:spark-nlp_2.12:3.4.0") // Replace "3.4.0" with the version of Spark NLP you are using
////  .getOrCreate()
//
//// Load your text file
////String filePath = "/home/sean/work/content-analysis/src/main/resources/sample-doc-short.txt" // Update this path to your text file
//String filePath = "/home/sean/work/content-analysis/src/main/resources/sample-doc.txt" // Update this path to your text file
//log.info "path: $filePath"
////Dataset<Row> df = spark.read().text(filePath).toDF('textCol')
//
//
//
///*
//SparkSession spark = SparkSession
//                .builder()
//                .appName("PipelineExample")
//                .config("spark.master", "local")
//                .getOrCreate();
//
//Dataset<Row> data = spark.read().format("csv")
//                .option("inferSchema", "true")
//                .option("header", "true")
//                .option("multiLine", "true")
//                .option("escape", "\"")
//                .load("data.csv");
//*/
//
//String text = 'This is a test. John Snow Labs is involved in this test. I would like spark-nlp to parse this test text.'
//
//// Create NLP pipeline
//DocumentAssembler documentAssembler = new DocumentAssembler()
//        .setInputCol("text")
//        .setOutputCol("document");
//
//Tokenizer tokenizer = new Tokenizer()
//        .setInputCol("document")
//        .setOutputCol("token");
//
//SentenceDetector sentenceDetector = new SentenceDetector()
//        .setInputCol("token")
//        .setOutputCol("sentence");
//
//// Apply pipeline to text
//spark.createDataFrame(text, "string")
//        .withColumn("text", spark.functions(text))
//        .withColumn("document", documentAssembler.transform("text"))
//        .withColumn("token", tokenizer.transform("document"))
//        .withColumn("sentence", sentenceDetector.transform("token"))
//        .select("sentence")
//        .show(false); // show all sentences without truncation
//
//spark.stop();
//
///*
//
//DocumentAssembler document_assembler = (DocumentAssembler) new DocumentAssembler().setInputCol("text").setOutputCol("document");
//
//SentenceDetector sentence_detector = (SentenceDetector) ((SentenceDetector) new SentenceDetector().setInputCols(new String[] {"document"})).setOutputCol("sentence");
//
//Tokenizer tokenizer = (Tokenizer)((Tokenizer) new Tokenizer().setInputCols(new String[] {"sentence"})).setOutputCol("token");
//
//Normalizer normalizer = (Normalizer)((Normalizer) new Normalizer().setInputCols(new String[]{"token"})).setOutputCol("normalized");
//
//LemmatizerModel lemmatizer = (LemmatizerModel)((LemmatizerModel) LemmatizerModel.pretrained("lemma_antbnc", "en", "public/models").setInputCols(new String[]{"normalized"})).setOutputCol("lemma");
//
//Finisher finisher = new Finisher().setInputCols(new String[]{"document", "lemma"}).setOutputCols(new String[]{"document", "lemma"});
//
//Pipeline pipeline = new Pipeline().setStages(new PipelineStage[]{document_assembler, sentence_detector, tokenizer, normalizer, lemmatizer, finisher});
//
// Fit the pipeline to training documents.
//PipelineModel pipelineFit = pipeline.fit(data);
//Dataset<Row> dataet = pipelineFit.transform(data);
//*/
//
//log.info "Done...?"
