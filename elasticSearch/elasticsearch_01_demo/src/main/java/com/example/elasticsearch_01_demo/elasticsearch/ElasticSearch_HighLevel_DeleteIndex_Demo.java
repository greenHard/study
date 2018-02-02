package com.example.elasticsearch_01_demo.elasticsearch;

import org.apache.http.HttpHost;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexResponse;
import org.elasticsearch.action.support.IndicesOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.rest.RestStatus;

import java.io.IOException;

/**
 *  elasticSearch 指数API
 *
 *  -- 删除索引API
 */
public class ElasticSearch_HighLevel_DeleteIndex_Demo {
    public static void main(String[] args) {
        // 1. 获得对应的连接
        // 实现了Closeable接口,不需要手动的关闭,会自动关闭
        RestHighLevelClient restHighLevelClient = new RestHighLevelClient(RestClient.builder(new HttpHost("127.0.0.1",9200)));

        // Constructs a new delete index request for the specified indices. 构建一个删除请求对于设置的索引,可以是多个
        DeleteIndexRequest deleteIndexRequest = new DeleteIndexRequest("customer");

        // 可以设置一些请求的参数信息
        // 设置等待所有节点确认的超时时间
        // parsing minutes should be case-sensitive(区分大小写) as 'M' means "months", not "minutes"; this is the only special case.
        //deleteIndexRequest.timeout(TimeValue.timeValueMinutes(2));
        deleteIndexRequest.timeout("2m");

        // 设置连接主节点的超时时间
        //deleteIndexRequest.masterNodeTimeout(TimeValue.timeValueMinutes(2));
        deleteIndexRequest.masterNodeTimeout("2m");

        // indices options that ignores unavailable indices, expands wildcards only to open indices and
        // allows that no indices are resolved from wildcard expressions (not returning an error).
        // 控制如何解决不可用的索引以及如何扩展通配符表达式
        // 允许从通配符表达式中不解析任何索引（不返回错误）,不设置的情况下没有索引会抛出异常
        deleteIndexRequest.indicesOptions(IndicesOptions.lenientExpandOpen());

        // 同步执行
        // restHighLevelClient#indices 会返回 IndicesClient
        // A wrapper for the {@link RestHighLevelClient} that provides methods for accessing the Indices API.
        // 这是对RestHighLevelClient的一个包装,用来提供方法操作索引API
        DeleteIndexResponse deleteIndexResponse = null;
        try {
            // 使用delete API 删除索引 返回删除 deleteIndexResponse A response for a delete index action.
            deleteIndexResponse = restHighLevelClient.indices().deleteIndex(deleteIndexRequest);
        } catch (ElasticsearchException e) {
            if (e.status() == RestStatus.NOT_FOUND) {
                System.out.println("索引没有找到,我们可以做一些操作");
            }
            e.printStackTrace();
        }catch (IOException e){
            System.out.println("删除索引异常");
            e.printStackTrace();
        }

        // 异步执行
        restHighLevelClient.indices().deleteIndexAsync(deleteIndexRequest, new ActionListener<DeleteIndexResponse>() {
            @Override
            public void onResponse(DeleteIndexResponse deleteIndexResponse) {
                // 处理响应结果
            }

            @Override
            public void onFailure(Exception e) {
                // 处理异常结果
            }
        });

        // 操作删除信息响应结果 DeleteIndexResponse
        if (deleteIndexResponse != null) {
            //  Returns whether the response is acknowledged or not 返回是否这个响应有被所有节点任何还是没有
            boolean acknowledged = deleteIndexResponse.isAcknowledged();
            System.out.println(acknowledged);
        }

    }
}
