package studium.analytics.streaming.Models;


import com.google.gson.*;
import java.io.Serializable;

import java.sql.Timestamp;

public class Teaching implements Serializable {
    private static final long serialVersionUID = 1L;
    public String dbName;
    public String teachingName;
    public String tutorName;
    public String userId;



    public String toString(){
        Gson serializer = new Gson();
        String teachJson = serializer.toJson(this);
        return teachJson;
    }
    
    public String toStringWithTimestamp(){
        Gson serializer = new Gson();
        String teachJson = serializer.toJson(this);
        String s1 = "{\"timestamp\" : "+new Timestamp(System.currentTimeMillis()).getTime()+",";
        s1 += teachJson.substring(1, teachJson.length());
        return s1;
    }

    
}