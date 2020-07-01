package studium.analytics.streaming.ML;

import java.io.IOException;
import java.util.*;
import studium.analytics.streaming.ML.MLPipeline;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.ml.Pipeline;
import org.apache.spark.ml.PipelineModel;
import org.apache.spark.ml.PipelineStage;
import org.apache.spark.ml.feature.VectorAssembler;
import org.apache.spark.ml.linalg.VectorUDT;
import org.apache.spark.ml.regression.LinearRegression;
import org.apache.spark.ml.regression.LinearRegressionModel;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.Metadata;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;
import org.sparkproject.dmg.pmml.Model;

public class MLPipeline {
        private static MLPipeline obj;
        private StructType schema;
        private PipelineModel weightedScoreModel;
        private PipelineModel upperBoundModel;
        private SparkSession spark;

        public static MLPipeline getUniqueInstance() {
                if (obj == null)
                        obj = new MLPipeline();
                return obj;
        }

        private MLPipeline() {
        }

        public void setup() {
                schema = new StructType(new StructField[] {
                                new StructField("label", DataTypes.DoubleType, false, Metadata.empty()),
                                new StructField("features", new VectorUDT(), false, Metadata.empty()) });
                spark = SparkSession.builder().appName("MachineLearning").getOrCreate();
                // to load training
                startUpperBoundMLTraining();
                startWeightedScoreMLTraining();
        }

        public void startWeightedScoreMLTraining() {
        StructType schemaForReading = new StructType(new StructField[] {
                        new StructField("label", DataTypes.DoubleType, false, Metadata.empty()),
                        new StructField("p1", DataTypes.DoubleType, false, Metadata.empty()),
                        new StructField("p2", DataTypes.DoubleType, false, Metadata.empty()),
                        new StructField("p3", DataTypes.DoubleType, false, Metadata.empty()),
                        new StructField("p4", DataTypes.DoubleType, false, Metadata.empty()),
                        new StructField("p5", DataTypes.DoubleType, false, Metadata.empty()),
                        new StructField("p6", DataTypes.DoubleType, false, Metadata.empty()),
                        new StructField("p7", DataTypes.DoubleType, false, Metadata.empty()),
                        new StructField("p8", DataTypes.DoubleType, false, Metadata.empty()),
                        new StructField("p9", DataTypes.DoubleType, false, Metadata.empty()),
                        new StructField("p10", DataTypes.DoubleType, false, Metadata.empty()) });

        Dataset<Row> readRow = spark.sqlContext().read().option("mode", "DROPMALFORMED")
                        .format("org.apache.spark.sql.execution.datasources.v2.csv.CSVDataSourceV2")
                        .option("header", "true").schema(schemaForReading).load("/opt/tap/MLTrainingData/weightedScore.csv");
        VectorAssembler assembler = new VectorAssembler().setInputCols(
                        new String[] { "p1", "p2", "p3", "p4", "p5", "p6", "p7", "p8", "p9", "p10" })
                        .setOutputCol("features");
        Dataset<Row> training = assembler.transform(readRow);
        LinearRegression lr = new LinearRegression();
        lr.setMaxIter(100).setRegParam(0.001).setElasticNetParam(0.0001);
        Pipeline pipeline = new Pipeline().setStages(new PipelineStage[] { lr });
        // Fit the pipeline to training documents.
        weightedScoreModel = pipeline.fit(training);
       // weightedScoreModel = PipelineModel.load("./MLPipelines/WeightedScorePipeline");
       /* try {
                
                weightedScoreModel.write().overwrite().save("./MLPipelines/WeightedScorePipeline");
        } catch (IOException e) {
                e.printStackTrace();
        }*/
        System.out.println("weightedScore learning finished");
    }

    public void startUpperBoundMLTraining(){
            StructType schemaForReading = new StructType(new StructField[]{
                    new StructField("label", DataTypes.DoubleType, false, Metadata.empty()),
                    new StructField("p1", DataTypes.DoubleType, false, Metadata.empty()),
                    new StructField("p2", DataTypes.DoubleType, false, Metadata.empty()),
                    new StructField("p3", DataTypes.DoubleType, false, Metadata.empty()),
                    new StructField("p4", DataTypes.DoubleType, false, Metadata.empty()),
                    new StructField("p5", DataTypes.DoubleType, false, Metadata.empty()),
                    new StructField("p6", DataTypes.DoubleType, false, Metadata.empty()),
                    new StructField("p7", DataTypes.DoubleType, false, Metadata.empty()),
                    new StructField("p8", DataTypes.DoubleType, false, Metadata.empty()),
                    new StructField("p9", DataTypes.DoubleType, false, Metadata.empty()),
                    new StructField("p10", DataTypes.DoubleType, false, Metadata.empty())
            });

            Dataset<Row> readRow = spark.sqlContext().read()
            .option("mode", "DROPMALFORMED")
            .format("org.apache.spark.sql.execution.datasources.v2.csv.CSVDataSourceV2")
            .option("header", "true")
            .schema(schemaForReading)
            .load("/opt/tap/MLTrainingData/upperBound.csv");
            VectorAssembler assembler = new VectorAssembler()
            .setInputCols(new String[]{"p1", "p2", "p3", "p4", "p5", "p6", "p7", "p8", "p9", "p10"})
            .setOutputCol("features");
            Dataset<Row> training = assembler.transform(readRow);
            LinearRegression lr = new LinearRegression();
            lr.setMaxIter(100).setRegParam(0.001).setElasticNetParam(0.0001);
            Pipeline pipeline = new Pipeline().setStages(new PipelineStage[] {lr});
            upperBoundModel = pipeline.fit(training);
            //upperBoundModel = PipelineModel.load("./MLPipelines/UpperBoundPipeline");
            /*try {
                upperBoundModel.write().overwrite().save("./MLPipelines/UpperBoundPipeline");
                } catch (IOException e) {
                        e.printStackTrace();
                }*/
            System.out.println("upperBound learning finished");
    }


    
    public List<Double> upperBoundML(List<Row> oldScores){   
        System.out.println("UPPER BOUND SEQ:");
        System.out.println(oldScores.toString());
        Dataset<Row> test = spark.createDataFrame(oldScores, schema);
        Dataset<Row> results = upperBoundModel.transform(test);
        Dataset<Row> rows = results.select("features", "label", "prediction");
        
        List<Double> res = new ArrayList<Double>();
        for (Row r: rows.collectAsList()) {
                res.add((Double)r.get(2));   
        };

        System.out.println("UPPER BOUND RES: " +res.toString());
        return res;
    }

    public List<Double> weightScoreML(List<Row> newScores){
            System.out.println("WEIGHTED SCORE SEQ:");
            System.out.println(newScores.toString());
            Dataset<Row> test = spark.createDataFrame(newScores, schema);
            Dataset<Row> results = weightedScoreModel.transform(test);
            Dataset<Row> rows = results.select("features", "label", "prediction");
            
            List<Double> res = new ArrayList<Double>();
            for (Row r: rows.collectAsList()) {
                    res.add((Double)r.get(2));   
            };
            System.out.println("WHEIGHTED SCORE RES: " + res.toString());
            return res;
    }

}