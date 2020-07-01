
package studium.analytics.streaming.Models;
import com.google.gson.*;

public class ModelsFactory {
    static ModelsFactory obj;

    private ModelsFactory(){}

    public static ModelsFactory getUniqueInstance(){
        if (obj == null) obj = new ModelsFactory();
        return obj;
    }

    public Teaching getTeaching(String json){
        Gson serializer = new Gson();
        Teaching teach = serializer.fromJson(json,Teaching.class);
        return teach;
    }
    public TeachingState getTeachingState(String json){
        Gson serializer = new Gson();
        TeachingState teachState = serializer.fromJson(json,TeachingState.class);
        return teachState;
   }
    public TeachingSection getTeachingSection(String json){

        Gson serializer = new Gson();
        TeachingSection teachSection = serializer.fromJson(json,TeachingSection.class);
        return teachSection;
    }
    public NotificationItem getNotificationItem(String json){
        Gson serializer = new Gson();
        NotificationItem notificationItem= serializer.fromJson(json,NotificationItem.class);
        return notificationItem;
    }
    public Item getItem(String json){
        Gson serializer = new Gson();
        Item item = serializer.fromJson(json,Item.class);
        return item;
    }
    public DocumentItem getDocumentItem(String json){
        Gson serializer = new Gson();
        DocumentItem documentItem= serializer.fromJson(json,DocumentItem.class);
        return documentItem;
    }

    public InteractionRow getInteractionRow(String topic ,String json){
        if(topic.equals("TeachingDidLoad")){
            Teaching teach = getTeaching(json);
            return new InteractionRow(teach,topic);
        }
        else if(topic.equals("SectionDidLoad")){
            TeachingSection teachSection = getTeachingSection(json);
            return new InteractionRow(teachSection.teaching,topic);

        }
        else if(topic.equals("NotificationDidOpen")){
            NotificationItem n = getNotificationItem(json);
            return new InteractionRow(n.teaching,topic);
        }
        else if(topic.equals("DocumentDidOpen")){
            DocumentItem d = getDocumentItem(json); 
            return new InteractionRow(d.teaching,topic);
        }
        return new InteractionRow();
    }
}