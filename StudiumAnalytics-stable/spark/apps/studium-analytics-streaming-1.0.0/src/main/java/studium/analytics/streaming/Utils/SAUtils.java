package studium.analytics.streaming.Utils;

import java.util.*;
import studium.analytics.streaming.Models.*;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.spark.SparkConf;

public class SAUtils {
    private static SAUtils obj;

    public static SAUtils getUniqueInstance(){
        if (obj == null) obj = new SAUtils();
        return obj;
    }
    private SAUtils(){}
    public SparkConf getDefaultSparkConf(){
        SparkConf sparkConf = new SparkConf();
        sparkConf.setAppName("StudiumAnalytics");
        sparkConf.set("es.nodes", "localhost");
        sparkConf.set("es.port", "9200");
        sparkConf.set("es.index.auto.create", "true");
        sparkConf.set("es.nodes.discovery", "true");
        sparkConf.set("es.batch.size.entries", "1");
        return sparkConf;
    }

    

    public Map<String,Object> getDefaultKafkaParams(){
        Map<String, Object> kafkaParams = new HashMap<>();
            kafkaParams.put("bootstrap.servers", "localhost:9092");
            kafkaParams.put("key.deserializer", StringDeserializer.class);
            kafkaParams.put("value.deserializer", StringDeserializer.class);
            kafkaParams.put("auto.offset.reset", "latest");
            kafkaParams.put("enable.auto.commit", false);
        return kafkaParams;
    }

    public TeachingScore getTeachingsWithScore( Iterable<InteractionRow> i){
        Double res = 0.0;
        for(InteractionRow el : i){
                if(el.interactionTopic.equals("TeachingDidLoad")){
                        res +=  1.5;
                }
                else if(el.interactionTopic.equals("SectionDidLoad")){
                        res +=  0.5;
        
                }
                else if(el.interactionTopic.equals("NotificationDidOpen")){
                        res += 3.0;
                }
                else if(el.interactionTopic.equals("DocumentDidOpen")){
                        res += 3.0;
                }   
    }
    TeachingScore t = new TeachingScore(i.iterator().next().teach,res);
    return t;
}
}