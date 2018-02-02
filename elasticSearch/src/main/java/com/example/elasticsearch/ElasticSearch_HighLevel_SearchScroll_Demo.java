package com.example.elasticsearch;

import org.apache.http.HttpHost;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;

import java.io.IOException;

/**
 *  elasticSearch 搜索API
 *
 *  -- 搜索滚动API
 */
public class ElasticSearch_HighLevel_SearchScroll_Demo {

    public static void main(String[] args) {

        // 初始化搜索滚动上下文
        // 1. 获得对应的连接
        // 实现了Closeable接口,不需要手动的关闭,会自动关闭
        RestHighLevelClient restHighLevelClient = new RestHighLevelClient(RestClient.builder(new HttpHost("127.0.0.1",9200)));

        // 2. 构建搜索请求
        // SearchRequest用于具有与搜索文件，聚合，建议和提供高亮显示所产生的文件的方式中的任何操作。
        // Constructs a new search request against the indices. No indices provided here means that search will run against all indices.
        // 更加索引构造一个新的索引请求,如果没有参数,代表根据所有索引构建新的索引请求
        SearchRequest searchRequest = new SearchRequest("customer");

        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(QueryBuilders.matchQuery("title", "Elasticsearch"));
        searchSourceBuilder.size(20);
        searchRequest.source(searchSourceBuilder);
        // If set, will enable scrolling of the search request for the specified timeout
        // 如果设置，将启用指定超时的搜索请求滚动。设置请求间隔
        searchRequest.scroll(TimeValue.timeValueMinutes(1L));
        SearchResponse searchResponse = null;
        try {
            searchResponse = restHighLevelClient.search(searchRequest);
            // If scrolling was enabled ({@link SearchRequest#scroll(org.elasticsearch.search.Scroll)}, the scroll id that can be used to continue scrolling.
            // 如果滚动启用（{@链接SearchRequest #滚动（org。Elasticsearch.搜索。滚动）}，这滚动ID，可用于继续滚动。
            String scrollId = searchResponse.getScrollId();
            // 获得第一批搜索匹配
            SearchHits hits = searchResponse.getHits();
            System.out.println("滚动ID"+scrollId+",hits:"+hits);
            // 检索所有相关的文件
            // 接收到的滚动标识符SearchScrollRequest与新的滚动间隔一起设置.searchScroll方法发送 。
            // Elasticsearch以新的滚动标识符返回另一批结果。这个新的滚动标识符可以在随后SearchScrollRequest用于检索下一批结果，依此类推。
            // 这个过程应该循环重复，直到没有更多的结果返回，这意味着滚动已经用尽，所有的匹配文件已经被检索。
            // SearchScrollRequest scrollRequest = new SearchScrollRequest(scrollId);
            // 创建SearchScrollRequest通过设置所需的滚动ID和滚动间隔
            // scrollRequest.scroll(TimeValue.timeValueSeconds(30));
            // SearchResponse searchScrollResponse = restHighLevelClient.searchScroll(scrollRequest);
            // 读取新的滚动标识，指向保持活动的搜索上下文，并在以下搜索滚动调用中需要
            // scrollId = searchScrollResponse.getScrollId();
            // 检索另一批搜索匹配
            // hits = searchScrollResponse.getHits();
            // 断言返回的值是否正确
            // assertEquals(3, hits.getTotalHits());
            // assertEquals(1, hits.getHits().length);
            // assertNotNull(scrollId);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
