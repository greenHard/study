package com.example.elasticsearch;

import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.Scroll;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;

import java.io.IOException;

/**
 *  elasticSearch 搜索API
 *
 *  -- 搜索清除滚动API 完整版
 */
public class ElasticSearch_HighLevel_SearchScroll_Demo2 {
    public static void main(String[] args) throws IOException {
        // 初始化搜索滚动上下文
        // 1. 获得对应的连接
        // 实现了Closeable接口,不需要手动的关闭,会自动关闭
        RestHighLevelClient restHighLevelClient = new RestHighLevelClient(RestClient.builder(new HttpHost("127.0.0.1",9200)));
        final Scroll scroll = new Scroll(TimeValue.timeValueMinutes(1L));
        // 通过发送初始化来初始化搜索上下文 SearchRequest
        SearchRequest searchRequest = new SearchRequest("posts");
        searchRequest.scroll(scroll);
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(QueryBuilders.matchQuery("title", "Elasticsearch"));
        searchRequest.source(searchSourceBuilder);

        SearchResponse searchResponse = restHighLevelClient.search(searchRequest);
        String scrollId = searchResponse.getScrollId();
        SearchHit[] searchHits = searchResponse.getHits().getHits();
        // 通过在循环中调用Search Scroll API来检索所有的搜索命中，直到没有文档被返回
        while (searchHits != null && searchHits.length > 0) {
            // 创建一个新的SearchScrollRequest保存上次返回的滚动标识符和滚动间隔
            SearchScrollRequest scrollRequest = new SearchScrollRequest(scrollId);
            scrollRequest.scroll(scroll);
            searchResponse = restHighLevelClient.searchScroll(scrollRequest);
            scrollId = searchResponse.getScrollId();
            searchHits = searchResponse.getHits().getHits();
            // 处理返回的搜索结果
        }
        // 滚动完成后清除滚动上下文
        ClearScrollRequest clearScrollRequest = new ClearScrollRequest();
        clearScrollRequest.addScrollId(scrollId);
        ClearScrollResponse clearScrollResponse = restHighLevelClient.clearScroll(clearScrollRequest);
        boolean succeeded = clearScrollResponse.isSucceeded();
        System.out.println(succeeded);
    }
}
