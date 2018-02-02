package com.example.elasticsearch_01_demo.elasticsearch;

import org.apache.http.HttpHost;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.support.WriteRequest;
import org.elasticsearch.action.support.replication.ReplicationResponse;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.VersionType;
import org.elasticsearch.rest.RestStatus;

import java.io.IOException;

/**
 *  elasticSearch 单个文档API
 *
 *   --- 删除API
 */
public class ElasticSearch_HighLevel_Delete_Demo {
    public static void main(String[] args) {
        // 1. 获得对应的连接
        // 实现了Closeable接口,不需要手动的关闭,会自动关闭
        RestHighLevelClient restHighLevelClient = new RestHighLevelClient(RestClient.builder(new HttpHost("127.0.0.1",9200)));

        // 2. 封装删除请求参数
        // index The index to get the document from 获得文档的索引
        // type  The type of the document   文档的类型
        // id    The id of the document     文档的id
        DeleteRequest deleteRequest = new DeleteRequest("customer","docs","1");

        // 3. 配置请求参数
        deleteRequest.routing("routing");
        // Sets the parent id of this document.
        // 设置此文档的父Id
        deleteRequest.parent("parent");
        // 设置存储版本号
        deleteRequest.version(2);
        // 设置版本类型 内部的,外部的
        deleteRequest.versionType(VersionType.EXTERNAL);
        // A timeout to wait if the index operation can't be performed immediately. Defaults to <tt>1m</tt>.
        // 设置等待索引超时的时间 默认是一分钟
        deleteRequest.timeout(TimeValue.timeValueSeconds(1));
        deleteRequest.timeout("1s");
        // 刷新政策作为一个WriteRequest.RefreshPolicy实例
        deleteRequest.setRefreshPolicy(WriteRequest.RefreshPolicy.WAIT_UNTIL);
        // 刷新政策作为一个String
        deleteRequest.setRefreshPolicy("wait_for");

        // 4. 执行请求,得到返回结果
        DeleteResponse deleteResponse = null;
        try {
            // 同步执行
            deleteResponse = restHighLevelClient.delete(deleteRequest);
            // The index the document was changed in.
            String index = deleteResponse.getIndex();
            // The type of the document changed.
            String type = deleteResponse.getType();
            //  The id of the document changed.
            String id = deleteResponse.getId();
            ReplicationResponse.ShardInfo shardInfo = deleteResponse.getShardInfo();
            if (shardInfo.getTotal() != shardInfo.getSuccessful()) {
                // 处理成功的碎片数量少于总碎片数的情况
                System.out.println();
            }
            if (shardInfo.getFailed() > 0) {
                for (ReplicationResponse.ShardInfo.Failure failure : shardInfo.getFailures()) {
                    String reason = failure.reason();
                    // 处理潜在的失败原因
                }
            }
        } catch (ElasticsearchException e) {
            // 如果请求了特定的文档版本，并且现有文档具有不同的版本号，则会引发版本冲突：
            if (e.status() == RestStatus.CONFLICT) {
                System.out.println("版本号冲突");
            }
            // 当对一个不存在的索引执行获取请求时，响应有404状态码，一个ElasticsearchException被抛出
            if(e.status() == RestStatus.NOT_FOUND){
                System.out.println("索引不存在");
            }
            e.printStackTrace();
        } catch (IOException e){
            System.out.println("执行异常");
            e.printStackTrace();
        }

        // 异步执行
        restHighLevelClient.deleteAsync(deleteRequest, new ActionListener<DeleteResponse>() {
            @Override
            public void onResponse(DeleteResponse deleteResponse) {
                System.out.println("处理返回请求");
            }

            @Override
            public void onFailure(Exception e) {
                System.out.println("处理异常情况");
            }
        });

    }
}
