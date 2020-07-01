package studium.analytics.streaming.ESAdapter;

import java.io.IOException;
import java.util.List;

import studium.analytics.streaming.Models.*;


import org.elasticsearch.action.ActionListener;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RequestOptions;

import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortOrder;
import java.util.*;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.streaming.api.java.JavaStreamingContext;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;


public class ESAdapter {
    private static ESAdapter obj;
    private RestHighLevelClient ESClient;

    public static ESAdapter getUniqueInstance(){
        if (obj == null) obj = new ESAdapter();
        return obj;
    }

    private ESAdapter(){
        ESClient = new RestHighLevelClient(RestClient.builder(new HttpHost("localhost", 9200, "http")));
     }

    public void createIndexWithTimestampMap(String index){

            CreateIndexRequest cRequest= new CreateIndexRequest(index);
            cRequest.mapping("{" +
            "  \"properties\": {" +
            "    \"timestamp\": {" +
            "      \"type\": \"date\"," +
            "      \"format\": \"epoch_millis\"" +
            "    }" +
            "  }" +
            "}", 
            XContentType.JSON);
            try {
                    ESClient.indices().create(cRequest, RequestOptions.DEFAULT);
            } catch (IOException e1) {
                    e1.printStackTrace();
            }
    }

    public void indexDocumentAtIndex(String index , String jsonDoc, String docID){
            IndexRequest request = new IndexRequest(index);
            request.id(docID);
            request.source(jsonDoc,XContentType.JSON);
            ESClient.indexAsync(request, RequestOptions.DEFAULT,new ActionListener<IndexResponse>() {
                    @Override
                    public void onResponse(IndexResponse bulkResponse) {
                            System.out.println("ok");
                    }
                    @Override
                    public void onFailure(Exception e) {
                            System.out.println(e.getMessage());
                    }
            });

    }

    public Boolean indexExists(String index){
            GetIndexRequest gRequest = new GetIndexRequest(index);
            Boolean exists;
            try {
                    exists = ESClient.indices().exists(gRequest, RequestOptions.DEFAULT);
            } catch (IOException e2) {
                    exists = false;
                    e2.printStackTrace();
            }
            return exists;
    }


    public  void appendNewScore(TeachingScore teachWithScore){
        String index = "pool-teachingscore-"+teachWithScore.dbName;
        index = index.toLowerCase();
        indexDocumentAtIndex(index, teachWithScore.toString(),"");
    }

    public  void deleteOldestScore(TeachingScore teachWithScore){

            String index = "pool-teachingscore-"+teachWithScore.dbName;
            index = index.toLowerCase();
            SearchRequest searchRequest = new SearchRequest(index); 
            SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder(); 
            searchSourceBuilder.query(QueryBuilders.matchAllQuery()); 
            searchSourceBuilder.size(1);
            searchSourceBuilder.sort(new FieldSortBuilder("timestamp").order(SortOrder.ASC));
            searchRequest.source(searchSourceBuilder);
            SearchResponse searchResponse;
            try {
                    searchResponse = ESClient.search(searchRequest, RequestOptions.DEFAULT);
                    SearchHits hits = searchResponse.getHits();
                    if(hits.getHits().length  != 0){
                            SearchHit  oldest = hits.getAt(0);
                            String id = oldest.getId();
                            DeleteRequest requestDel = new DeleteRequest(index , id); 
                            ESClient.delete(requestDel, RequestOptions.DEFAULT);
                    }
                    
            } catch (IOException e) {
                    e.printStackTrace();
            }
            
    }
    public List<Double> getHistoryScores(Teaching teach , JavaStreamingContext streamingContext){
        String index = "pool-teachingscore-"+teach.dbName;
        index = index.toLowerCase();
        SearchRequest searchRequest = new SearchRequest(index); 
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder(); 
        searchSourceBuilder.query(QueryBuilders.matchAllQuery()); 
        searchSourceBuilder.size(100);
        searchSourceBuilder.sort(new FieldSortBuilder("timestamp").order(SortOrder.ASC));
        searchRequest.source(searchSourceBuilder);
        try {
                SearchResponse searchResponse = ESClient.search(searchRequest, RequestOptions.DEFAULT);
                SearchHits hits = searchResponse.getHits();
                SearchHit[] searchHits = hits.getHits();
                List<SearchHit> searchHitsList = new ArrayList<SearchHit>(Arrays.asList(searchHits));
                List<Map<String,Object>> hitsMapList = new ArrayList<Map<String,Object>>();
                searchHitsList.forEach(hit->{ //non è possibile farlo su rdd, perchè SearchHit non risulta essere serializzabile, non va il parallelize.
                        hitsMapList.add(hit.getSourceAsMap());
                });
                JavaRDD<Map<String,Object>> hitMapsRDD = streamingContext.sparkContext().parallelize(hitsMapList);
                JavaRDD<Double> hitsRDD = hitMapsRDD.map(hit-> (Double) hit.get("score"));
                return hitsRDD.collect();
                
        } catch (IOException e) {
                System.out.println("error - reading search stack trace");
                e.printStackTrace();
                return null;
        }
}



}