package com.example.elasticsearch;

import org.apache.http.HttpHost;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.DocWriteRequest;
import org.elasticsearch.action.DocWriteResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.support.replication.ReplicationResponse;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.VersionType;
import org.elasticsearch.rest.RestStatus;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 *  elasticSearch 单个文档API
 *
 *   --- 索引API
 */
public class ElasticSearch_HighLevel_Add_Demo {
    public static void main(String[] args) {

        // 1. 获得对应的连接
        // 实现了Closeable接口,不需要手动的关闭,会自动关闭
        RestHighLevelClient restHighLevelClient = new RestHighLevelClient(RestClient.builder(new HttpHost("127.0.0.1",9200)));

        // index 索引名称
        // type  索引类型
        // id    文档id
        // 第一步: 创建一个索引请求
        IndexRequest indexRequest = new IndexRequest("blank","account","20");

        // 第二步: 创建一个文档来源
        // 文档来源可以通过不同的方式提供
        // 第一种方式: 手写String串
        // 第二种方式: 提供文档源map自动转成json
        Map<String, String> jsonMap = new HashMap<>();
        jsonMap.put("name","张三");
        indexRequest.source(jsonMap);
        // 第三种方式: 利用JACKSON序列化
        // instance a json mapper
        // ObjectMapper mapper = new ObjectMapper(); // create once, reuse
        // generate json
        // byte[] json = mapper.writeValueAsBytes(yourbeaninstance);
        // 第四种方式: 作为一个XContentBuilder对象提供的文档源，Elasticsearch内置的帮助器来生成JSON内容
        // XContentBuilder xContentBuilder = null;
        // String json = null;
        // try {
        //     xContentBuilder = XContentFactory.jsonBuilder();
        //     // 这里可以使用链式编程
        //     xContentBuilder.startObject().field("address","北京").field("name","张三").endObject();
        //     json = xContentBuilder.toString();
        // } catch (IOException e) {
        //     e.printStackTrace();
        // }

        // 第三步: 设置请求参数
        // Controls the shard routing of the request. Using this value to hash the shard
        //控制分片路由的请求,用这个值进行hash分片
        indexRequest.routing("routing");

        // Sets the parent id of this document.
        // 设置此文档的父Id
        indexRequest.parent("parent");

        // A timeout to wait if the index operation can't be performed immediately. Defaults to <tt>1m</tt>.
        // 设置等待索引超时的时间 默认是一分钟
        indexRequest.timeout(TimeValue.timeValueSeconds(1));
        indexRequest.timeout("1s");

        indexRequest.setRefreshPolicy("wait_for");

        // 设置存储版本号
        indexRequest.version(2);
        // 设置版本类型 内部的,外部的
        indexRequest.versionType(VersionType.EXTERNAL);

        // Sets the type of operation to perform. 设置操作的类型
        // opType must be 'create' or 'index'  opType
        indexRequest.opType(DocWriteRequest.OpType.CREATE);
        indexRequest.opType("create");

        // Sets the ingest pipeline to be executed before indexing the document
        // 在索引文档之前设置要执行的摄取管道。
        indexRequest.setPipeline("pipeline");

        // 第四步: 执行请求
        // 同步执行
         try {
             IndexResponse indexResponse = restHighLevelClient.index(indexRequest);
         } catch (ElasticsearchException e){
             // 1. 如果opType设置为create同一个索引，类型和ID的文件已经存在，则会发生同样的情况：
             // 2. 版本冲突
             if (e.status() == RestStatus.CONFLICT) {
                 System.out.println("版本发生冲突,会抛出这个异常");
             }
         }catch (IOException e) {
             e.printStackTrace();
         }

        // 异步执行
         restHighLevelClient.indexAsync(indexRequest, new ActionListener<IndexResponse>() {
             @Override
             public void onResponse(IndexResponse indexResponse) {
        //第五步: 处理响应信息
                 // 处理返回结果
                 // The index the document was changed in.
                 String index = indexResponse.getIndex();
                 // The type of the document changed.
                 String type = indexResponse.getType();
                 //  The id of the document changed.
                 String id = indexResponse.getId();
                 // Returns the current version of the doc.
                 long version = indexResponse.getVersion();

                 if (indexResponse.getResult() == DocWriteResponse.Result.CREATED) {
                     // 处理（如果需要的话）第一次创建文档的情况
                     System.out.println();
                 } else if (indexResponse.getResult() == DocWriteResponse.Result.UPDATED) {
                     // 处理（如果需要的话）文档被重写的情况，因为它已经存在
                     System.out.println();
                 }
                 ReplicationResponse.ShardInfo shardInfo = indexResponse.getShardInfo();
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
             }
             @Override
             public void onFailure(Exception e) {
                  // 处理异常情况
             }
         });
    }
}
