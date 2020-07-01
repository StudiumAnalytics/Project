package studium.analytics.streaming.Models;


import java.io.Serializable;

import com.google.gson.*;
import java.sql.Timestamp;

public class Item implements Serializable {
    private static final long serialVersionUID = 1L;
    public Teaching teaching;
    public String date;
    public String title;    
    public String toString()
    {
        Gson serializer = new Gson();
        String itemJson = serializer.toJson(this);
        String s1 = "{\"timestamp\" : "+new Timestamp(System.currentTimeMillis()).getTime()+",";
        s1 += itemJson.substring(1, itemJson.length());
        return s1;
    }
}