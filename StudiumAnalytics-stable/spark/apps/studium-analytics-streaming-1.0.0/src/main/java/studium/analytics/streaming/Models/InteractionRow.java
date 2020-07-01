package studium.analytics.streaming.Models;


import java.io.Serializable;

import com.google.gson.*;
import java.sql.Timestamp;

public class InteractionRow implements Serializable {
    private static final long serialVersionUID = 1L;
    public Teaching teach;
    public String interactionTopic;    
    public InteractionRow(Teaching teach, String topic){

        this.teach=teach;
        this.interactionTopic=topic;
    }
    public InteractionRow(){}
    public String toString()
    {
        Gson serializer = new Gson();
        String itemJson = serializer.toJson(this);
        String s1 = "{\"timestamp\" : "+new Timestamp(System.currentTimeMillis()).getTime()+",";
        s1 += itemJson.substring(1, itemJson.length());
        return s1;
    }
}


