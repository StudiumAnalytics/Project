package studium.analytics.streaming.Analytics;
import java.util.*;

import studium.analytics.streaming.ESAdapter.ESAdapter;
import studium.analytics.streaming.Utils.SAUtils;
import studium.analytics.streaming.ML.MLPipeline;
import studium.analytics.streaming.Models.*;


import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;

import org.apache.spark.streaming.Durations;
import org.apache.spark.streaming.api.java.JavaDStream;
import org.apache.spark.streaming.api.java.JavaInputDStream;
import org.apache.spark.streaming.api.java.JavaPairDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;
import org.apache.spark.streaming.kafka010.*;

import scala.Tuple2;

import org.apache.spark.ml.linalg.Vectors;

import org.apache.spark.sql.Row;
import org.apache.spark.sql.RowFactory;

public class Processor {
    private static Processor obj;

    public static Processor getUniqueInstance(){
        if (obj == null) obj = new Processor();
        return obj;
    }
    private Processor(){}

    public void startProcessing() throws InterruptedException {
        SparkConf sparkConf = SAUtils.getUniqueInstance().getDefaultSparkConf();
        JavaStreamingContext streamingContext = new JavaStreamingContext(sparkConf, Durations.seconds(1));
        streamingContext.sparkContext().setLogLevel("ERROR");
        
        System.out.println("Hello World! Sergio, Simone & Nics");
        
        MLPipeline.getUniqueInstance().setup();

        viewsPerSection(streamingContext);
        teachingDidOpen(streamingContext);
        viewsPerItem(streamingContext);
        analyzeTeachingScore(streamingContext);
        
        streamingContext.start();
        streamingContext.awaitTermination();

        //to close the es client:
        /*try {
                client.close();
        } catch (IOException e1) {
                e1.printStackTrace();
        }*/
    }
    
    
    private void viewsPerSection(JavaStreamingContext streamingContext) {
            ESAdapter ES = ESAdapter.getUniqueInstance();
            Map<String, Object> kafkaParams = SAUtils.getUniqueInstance().getDefaultKafkaParams();
            kafkaParams.put("group.id", "groupid2");
            Collection<String> topics = Arrays.asList("SectionDidLoad");
            JavaInputDStream<ConsumerRecord<String, String>> messages = KafkaUtils.createDirectStream(
                            streamingContext, LocationStrategies.PreferConsistent(),
                            ConsumerStrategies.<String, String>Subscribe(topics, kafkaParams));
            JavaPairDStream<String, TeachingSection> results = messages
                            .mapToPair(record -> new Tuple2<>(record.key(),
                                            ModelsFactory.getUniqueInstance().getTeachingSection(record.value())));
            JavaDStream<TeachingSection> lines = results.map(tuple2 -> tuple2._2());
            lines.foreachRDD(l -> {
                    List<TeachingSection> sections = l.collect();
                    sections.forEach(t -> {
                            String index = "counters-sectionviews-"+t.teaching.dbName;
                            index = index.toLowerCase();
                            Boolean exists = ES.indexExists(index);
                            if (!exists) ES.createIndexWithTimestampMap(index);
                            ES.indexDocumentAtIndex(index, t.toString(),"");
                    });
            });
    }

    private void viewsPerItem(JavaStreamingContext streamingContext) {
            ESAdapter ES = ESAdapter.getUniqueInstance();
            Map<String, Object> kafkaParams = SAUtils.getUniqueInstance().getDefaultKafkaParams();
            kafkaParams.put("group.id", "groupid3");
            Collection<String> topics = Arrays.asList("NotificationDidOpen", "DocumentDidOpen");
            JavaInputDStream<ConsumerRecord<String, String>> messages = KafkaUtils.createDirectStream(
                            streamingContext, LocationStrategies.PreferConsistent(),
                            ConsumerStrategies.<String, String>Subscribe(topics, kafkaParams));

            JavaPairDStream<String, String> results = messages
                            .mapToPair(record -> new Tuple2<>(record.topic(), record.value()));
            
            JavaPairDStream<String, String> notificationDidOpenResults = results
                            .filter(r -> r._1().equalsIgnoreCase("NotificationDidOpen"));
            JavaPairDStream<String, String> documentDidOpenResults = results
                            .filter(r -> r._1().equalsIgnoreCase("DocumentDidOpen"));

            JavaDStream<NotificationItem> notificationDidOpenLines = notificationDidOpenResults
                            .map(tuple2 -> ModelsFactory.getUniqueInstance().getNotificationItem(tuple2._2()));
            JavaDStream<DocumentItem> documentDidOpenLines = documentDidOpenResults
                            .map(tuple2 -> ModelsFactory.getUniqueInstance().getDocumentItem(tuple2._2()));

            notificationDidOpenLines.foreachRDD(rdd -> {
                    List<NotificationItem> teachings = rdd.collect();
                    teachings.forEach(t -> {
                            String index = "counters-notifications-"+t.teaching.dbName;
                            index = index.toLowerCase();
                            Boolean exists = ES.indexExists(index);
                            if (!exists) ES.createIndexWithTimestampMap(index);
                            ES.indexDocumentAtIndex(index, t.toString(),"");
                    });
            });   

            documentDidOpenLines.foreachRDD(rdd -> {
                    List<DocumentItem> teachings = rdd.collect();
                    teachings.forEach(ts -> {
                            String index = "counters-documents-"+ts.teaching.dbName;
                            index = index.toLowerCase();
                            Boolean exists = ES.indexExists(index);
                            if (!exists) ES.createIndexWithTimestampMap(index);
                            ES.indexDocumentAtIndex(index, ts.toString(),"");
                    });
            });
    }

    private void teachingDidOpen(JavaStreamingContext streamingContext) {
            ESAdapter ES = ESAdapter.getUniqueInstance();
            Map<String, Object> kafkaParams = SAUtils.getUniqueInstance().getDefaultKafkaParams();
            kafkaParams.put("group.id", "groupid4");
            Collection<String> topics = Arrays.asList("TeachingDidLoad", "TeachingStateRefresh");
            JavaInputDStream<ConsumerRecord<String, String>> messages = KafkaUtils.createDirectStream(
                            streamingContext, LocationStrategies.PreferConsistent(),
                            ConsumerStrategies.<String, String>Subscribe(topics, kafkaParams));

            JavaPairDStream<String, String> results = messages
                            .mapToPair(record -> new Tuple2<>(record.topic(), record.value()));

            JavaPairDStream<String, String> teachingDidLoadResults = results
                            .filter(r -> r._1().equalsIgnoreCase("TeachingDidLoad"));
            JavaPairDStream<String, String> teachingStateRefreshResults = results
                            .filter(r -> r._1().equalsIgnoreCase("TeachingStateRefresh"));

            JavaDStream<Teaching> teachingDidLoadLines = teachingDidLoadResults
                            .map(tuple2 -> ModelsFactory.getUniqueInstance().getTeaching(tuple2._2()));
            JavaDStream<TeachingState> teachingStateRefreshLines = teachingStateRefreshResults
                            .map(tuple2 -> ModelsFactory.getUniqueInstance().getTeachingState(tuple2._2()));

            teachingDidLoadLines.foreachRDD(rdd -> {
                    List<Teaching> teachings = rdd.collect();
                    teachings.forEach(t -> {
                            String index = "counters-teachingviews-"+t.dbName;
                            index = index.toLowerCase();
                            Boolean exists = ES.indexExists(index);
                            if (!exists) ES.createIndexWithTimestampMap(index);
                            ES.indexDocumentAtIndex(index, t.toStringWithTimestamp(),"");
                    });
            });   

            teachingStateRefreshLines.foreachRDD(rdd -> {
                    List<TeachingState> teachings = rdd.collect();
                    teachings.forEach(ts -> {
                            String index = "completeness-teachingstate-"+ts.teaching.dbName;
                            index = index.toLowerCase();
                            Boolean exists = ES.indexExists(index);
                            if (!exists) ES.createIndexWithTimestampMap(index);
                            ES.indexDocumentAtIndex(index, ts.toString(),"lastState");
                    });
            });
    }

    private void analyzeTeachingScore(JavaStreamingContext streamingContext) {
        ESAdapter ES = ESAdapter.getUniqueInstance();
        Map<String, Object> kafkaParams = SAUtils.getUniqueInstance().getDefaultKafkaParams();
        kafkaParams.put("group.id", "groupid1");
        Collection<String> topics = Arrays.asList("TeachingDidLoad", "SectionDidLoad", "NotificationDidOpen", "DocumentDidOpen");
        JavaInputDStream<ConsumerRecord<String, String>> messages = KafkaUtils.createDirectStream(
                        streamingContext, LocationStrategies.PreferConsistent(),
                        ConsumerStrategies.<String, String>Subscribe(topics, kafkaParams));
        JavaPairDStream<String, String> results = messages
                        .mapToPair(record -> new Tuple2<>(record.topic(), record.value()));
        

        JavaDStream<InteractionRow> ir = results.map(tuple2 -> ModelsFactory.getUniqueInstance().getInteractionRow(tuple2._1(), tuple2._2()));
        
        ir.foreachRDD(rdd -> {
                if(rdd.collect().size() != 0){
                        JavaPairRDD<String,InteractionRow> r1 = rdd.mapToPair(f -> new Tuple2<String,InteractionRow>(f.teach.dbName,f));
                        JavaPairRDD<String, Iterable<InteractionRow>> r2 = r1.groupByKey();
                        //maybe better to use reduce
                    JavaRDD<TeachingScore> r3 = r2.map( tuple2 ->  SAUtils.getUniqueInstance().getTeachingsWithScore(tuple2._2()));
                    
                        List <TeachingScore> tList = r3.collect();
                        List<Double> normalizedScores = getNormalizedScores(tList,streamingContext);
                        for(int i = 0; i < tList.size(); i++){
                                TeachingScore normalizedScore = new TeachingScore(tList.get(i), normalizedScores.get(i));
                                String index = "current-score-"+ tList.get(i).dbName;
                                index = index.toLowerCase();
                                Boolean exists = ES.indexExists(index);
                                if (!exists) ES.createIndexWithTimestampMap(index);
                                
                                ES.indexDocumentAtIndex(index, normalizedScore.toString(),"last-score");
                                System.out.println("PUNTEGGIO CORSO " + tList.get(i).teachingName + ": " + normalizedScores.get(i) + " appended :" +  tList.get(i).score);
                        }
                }
            
        }); 
}


    private List<Double> getNormalizedScores (List<TeachingScore> teachs, JavaStreamingContext streamingContext){
        ESAdapter ES = ESAdapter.getUniqueInstance();
        MLPipeline ML = MLPipeline.getUniqueInstance();
        
        List<Row> oldScoresSequences = new ArrayList<Row>();
        List<Row> newScoresSequences = new ArrayList<Row>();
        for(TeachingScore teach : teachs){
                String index = "pool-teachingscore-"+teach.dbName;
                index = index.toLowerCase();
                Boolean exists = ES.indexExists(index);
                List<Double> historyScores;
                if (!exists) {
                        ES.createIndexWithTimestampMap(index);
                        historyScores = new ArrayList<Double>();
                        historyScores = Collections.nCopies(100, 0.15);
                }
                else{
                        historyScores = ES.getHistoryScores(teach, streamingContext);
                        if(historyScores.size() < 100){
                                System.out.println("< 100 SCORES");
                                int currentScoresCount = historyScores.size();
                                int scoresCountToCreate = 100 - currentScoresCount;
                                List<Double> createdScores = new ArrayList<Double>(Collections.nCopies(scoresCountToCreate, 0.15));
                                createdScores.addAll(historyScores);
                                historyScores = createdScores;
                        }
                        else ES.deleteOldestScore(teach);
                        
                }
                ES.appendNewScore(teach);
                
                //creiamo una lista modificata per wheightedScore
                List<Double> listForUpperBound = historyScores;
                List<Double> listForWeightedScore = new ArrayList<Double>(historyScores);
                //modifichiamo questa lista
                listForWeightedScore.remove(0);
                listForWeightedScore.add(Double.valueOf(teach.score));

                List<double[]> groups = groupScoreSequenceBy10(streamingContext, listForUpperBound, listForWeightedScore);
                double[] oldScoresGrouped = groups.get(0);
                double[] newScoresGrouped = groups.get(1);
                
                //Row che vanno nelle liste che passeremo ai modelli ML
                Row oldScoresGroupedRow = RowFactory.create(0.0, Vectors.dense(oldScoresGrouped));
                Row newScoresGroupedRow = RowFactory.create(0.0, Vectors.dense(newScoresGrouped));
                oldScoresSequences.add(oldScoresGroupedRow);
                newScoresSequences.add(newScoresGroupedRow);
                        
                } 
        
        //passiamo i dati ai modelli ML
        List<Double> max = ML.upperBoundML(oldScoresSequences);
        List<Double> scores = ML.weightScoreML(newScoresSequences);
        //normalizziamo secondo i risultati forniti dai modelli ML
        List<Double> normalizedScores = new ArrayList<Double>();
        for(int i = 0; i < scores.size(); i++){
                Double points = 10 *  (scores.get(i)/ max.get(i));
                if(points > 10) points = 10.0;
                normalizedScores.add(points);
        }
        return normalizedScores; 
}


private List<double[]> groupScoreSequenceBy10(JavaStreamingContext streamingContext, List<Double> listForUpperBound,List<Double> listForWeightedScore){
        //liste di mappe riferite ad un singolo corso che useremo per i modelli di ml 
        JavaRDD<Double> oldScoresRDD = streamingContext.sparkContext().parallelize(listForUpperBound);
        double[] oldScoresGrouped = {0,0,0,0,0,0,0,0,0,0};
        int oldScoresGroupedSize = 0;
        //WheightedScore ML
        JavaRDD<Double> newScoresRDD = streamingContext.sparkContext().parallelize(listForWeightedScore);
        double[] newScoresGrouped = {0,0,0,0,0,0,0,0,0,0};
        int newScoresGroupedSize = 0;
        //raggruppamento ed inserimento negli array di double
        JavaPairRDD<Double,Long> oldScoresZippedRDD = oldScoresRDD.zipWithIndex();
        JavaPairRDD<Double,Long> newScoresZippedRDD = newScoresRDD.zipWithIndex();
        for(int i = 1; i<= 10; i++){
                Long upper = i * 10l;
                Long lower = (i-1)*10l;
                JavaRDD<Double> oldGroupedScoresRDD = oldScoresZippedRDD.filter(tuple2 -> (tuple2._2() >= lower && tuple2._2() < upper)).map(tuple2 -> tuple2._1());
                JavaRDD<Double> newGroupedScoresRDD = newScoresZippedRDD.filter(tuple2 -> (tuple2._2() >= lower && tuple2._2() < upper)).map(tuple2 -> tuple2._1());

                oldScoresGroupedSize++;
                newScoresGroupedSize++;
                for(Double score : oldGroupedScoresRDD.collect()){
                        oldScoresGrouped[oldScoresGroupedSize-1] += score; 
                }
                for(Double score : newGroupedScoresRDD.collect()){
                        newScoresGrouped[newScoresGroupedSize-1] += score;
                }
        }
        List<double[]> res = new ArrayList<double[]>();
        res.add(oldScoresGrouped);
        res.add(newScoresGrouped);
        return res;
}
}