package studium.analytics.streaming.Models;


import java.io.Serializable;

import com.google.gson.*;
import java.sql.Timestamp;


public class TeachingState implements Serializable {
    private static final long serialVersionUID = 1L;
    public Teaching teaching;
    public Boolean showCaseExists;
    public Boolean notifiesExists ;
    public Boolean descriptionExists;
    public Boolean documentsExists ;
    public Boolean bookingExists;   
    
    public String toString()
    {
        Gson serializer = new Gson();
        String teachStateJson = serializer.toJson(this);
        String s1 = "{\"timestamp\" : "+new Timestamp(System.currentTimeMillis()).getTime()+",";
        s1 += teachStateJson.substring(1, teachStateJson.length());
        return s1;
    }
}