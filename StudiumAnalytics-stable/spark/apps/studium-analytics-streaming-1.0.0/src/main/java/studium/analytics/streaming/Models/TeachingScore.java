package studium.analytics.streaming.Models;
import com.google.gson.*;
import java.sql.Timestamp;

public class TeachingScore extends Teaching{
    private static final long serialVersionUID = 1L;
    public Double score;

    public TeachingScore(Teaching t ,Double s){
        dbName = t.dbName;
        teachingName = t.teachingName;
        tutorName = t.tutorName;
        userId = t.userId;
        score = s;
    }

    public String toString(){
        Gson serializer = new Gson();
        String teachJson = serializer.toJson(this);
        String s1 = "{\"timestamp\" : "+new Timestamp(System.currentTimeMillis()).getTime()+",";
        s1 += teachJson.substring(1, teachJson.length());
        return s1;
    }
}