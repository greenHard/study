package com.example.elasticsearch;

import org.apache.http.HttpHost;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.DocWriteRequest;
import org.elasticsearch.action.DocWriteResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.support.ActiveShardCount;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.unit.ByteSizeUnit;
import org.elasticsearch.common.unit.ByteSizeValue;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.rest.RestStatus;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 *  elasticSearch 多文档API
 *
 *   --- 批量API
 */
public class ElasticSearch_HighLevel_Bulk_Demo {


    public static void main(String[] args) {
        // 1. 获得对应的连接
        // 实现了Closeable接口,不需要手动的关闭,会自动关闭
        RestHighLevelClient restHighLevelClient = new RestHighLevelClient(RestClient.builder(new HttpHost("127.0.0.1", 9200)));

        // 2. 封装批量请求参数
        // 批量API仅支持以JSON或SMILE编码的文档。以任何其他格式提供文档将导致错误。
        // 不同对的操作类型可以添加到相同的bulkRequest中
        BulkRequest bulkRequest = new BulkRequest();
        // 添加一个删除请求到批量请求中
        bulkRequest.add(new DeleteRequest("posts", "doc", "3"));
        // 添加一个更新请求到批量请求中
        bulkRequest.add(new UpdateRequest("posts", "doc", "2")
                .doc(XContentType.JSON,"other", "test"));
        // 添加一个索引请求到批量请求中
        bulkRequest.add(new IndexRequest("posts", "doc", "4")
                .source(XContentType.JSON,"field", "baz"));

        // 3. 设置批量请求可选的参数
        // A timeout to wait if the index operation can't be performed immediately. Defaults to <tt>1m</tt>.
        // 设置等待索引超时的时间 默认是一分钟
        bulkRequest.timeout(TimeValue.timeValueSeconds(1));
        bulkRequest.timeout("1s");

        bulkRequest.setRefreshPolicy("wait_for");
        // 设置在更新之前必须激活的分片副本的数量
        bulkRequest.waitForActiveShards(2);
        // ActiveShardCount可以是ActiveShardCount.ALL， ActiveShardCount.ONE,ActiveShardCount.NONE或ActiveShardCount.DEFAULT（默认）
        // private static final int ALL_ACTIVE_SHARDS = -1;
        // public static final ActiveShardCount DEFAULT = new ActiveShardCount(ACTIVE_SHARD_COUNT_DEFAULT);
        // public static final ActiveShardCount ALL = new ActiveShardCount(ALL_ACTIVE_SHARDS);
        // public static final ActiveShardCount NONE = new ActiveShardCount(0);
        // public static final ActiveShardCount ONE = new ActiveShardCount(1);
        bulkRequest.waitForActiveShards(ActiveShardCount.ALL);

        // 4. 执行批量请求,处理响应
        BulkResponse bulkResponse = null;
        try {
            // 同步执行
            // A response of a bulk execution. Holding a response for each item responding (in order) of the
            // bulk requests. Each item holds the index/type/id is operated on, and if it failed or not (with the
            // failure message).
            bulkResponse = restHighLevelClient.bulk(bulkRequest);

            // 批量响应提供了一种快速检查一个或多个操作是否失败的方法：
            //  Has anything failed with the execution.
            // true如果至少有一个操作失败，则 返回此方法
            if (bulkResponse.hasFailures()){
                System.out.println("操作失败了。。");
            }
            // 迭代所有操作的结果
            for (BulkItemResponse bulkItemResponse : bulkResponse.getItems()) {
                if (bulkItemResponse.isFailed()) {
                    BulkItemResponse.Failure failure = bulkItemResponse.getFailure();
                    System.out.println("检索失败操作的失败"+failure);

                }
                // 检索操作的响应（成功与否），可以是IndexResponse， UpdateResponse或者DeleteResponse哪些都可以被视为DocWriteResponse实例
                DocWriteResponse docWriteResponse = bulkItemResponse.getResponse();
                if (bulkItemResponse.getOpType() == DocWriteRequest.OpType.INDEX
                        || bulkItemResponse.getOpType() == DocWriteRequest.OpType.CREATE) {
                    IndexResponse indexResponse = (IndexResponse) docWriteResponse;
                    System.out.println("处理索引操作的响应"+indexResponse);

                } else if (bulkItemResponse.getOpType() == DocWriteRequest.OpType.UPDATE) {
                    UpdateResponse updateResponse = (UpdateResponse) docWriteResponse;
                    System.out.println("处理更新操作的响应"+updateResponse);

                } else if (bulkItemResponse.getOpType() == DocWriteRequest.OpType.DELETE) {
                    DeleteResponse deleteResponse = (DeleteResponse) docWriteResponse;
                    System.out.println("处理删除操作的响应"+deleteResponse);
                }
            }
        }catch (ElasticsearchException e) {
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
        restHighLevelClient.bulkAsync(bulkRequest, new ActionListener<BulkResponse>() {
            @Override
            public void onResponse(BulkResponse bulkResponse) {
                System.out.println("处理返回请求");
            }

            @Override
            public void onFailure(Exception e) {
                System.out.println("处理异常情况");
            }
        });

        // ** 批量执行器
        // BulkProcessor 通过提供一个工具类，允许它们被添加到所述处理器透明地执行索引/更新/删除的操作简化了批量API的使用。
        // the BulkProcessor requires the following components: 执行器需要以下组件
        // RestHighLevelClient  This client is used to execute the BulkRequest and to retrieve the BulkResponse 用来执行请求和接收返回值
        // BulkProcessor.Listener(内部类) is listener is called before and after every BulkRequest execution or when a BulkRequest failed
        // BulkRequest执行之前和之后被调用，或者当一个BulkRequest失败 时被调用
        // 1. 创建一个listener
        BulkProcessor.Listener listener = new BulkProcessor.Listener() {
            @Override
            public void beforeBulk(long executionId, BulkRequest request) {
                System.out.println("执行请求执行调用");
            }

            @Override
            public void afterBulk(long executionId, BulkRequest request, BulkResponse response) {
                System.out.println("执行请求之后调用");
            }

            @Override
            public void afterBulk(long executionId, BulkRequest request, Throwable failure) {
                System.out.println("执行失败时做什么处理");
            }
        };

        // 2. BulkProcessor执行  java8新特性,静态方法的引用
        BulkProcessor.Builder builder = BulkProcessor.builder(restHighLevelClient::bulkAsync, listener);
        // BulkProcessor.Builder提供的方法来配置如何BulkProcessor 应该处理请求的执行：
        // 根据当前添加的操作数设置何时刷新新的批量请求（默认为1000，使用-1来禁用它）
        builder.setBulkActions(500);
        // 根据当前添加的操作的大小设置何时刷新新的批量请求（默认为5Mb，使用-1来禁用它）
        builder.setBulkSize(new ByteSizeValue(1L, ByteSizeUnit.MB));
        // 设置允许执行的并发请求数（默认为1，使用0只允许执行一个请求）
        builder.setConcurrentRequests(0);
        // 设置一个刷新间隔，BulkRequest如果间隔过去，则清除任何挂起（默认为未设置）
        builder.setFlushInterval(TimeValue.timeValueSeconds(10L));
        // 设置一个不变的退休政策，最初等待1秒钟，然后重试3次。看BackoffPolicy.noBackoff()， BackoffPolicy.constantBackoff()并BackoffPolicy.exponentialBackoff() 有更多的选择。
        builder.setBackoffPolicy(BackoffPolicy.constantBackoff(TimeValue.timeValueSeconds(1L), 3));

        // 一旦批量执行器创建好,请求都可以添加到批量执行器中
        // 创建执行器
        BulkProcessor bulkProcessor = builder.build();
        IndexRequest one = new IndexRequest("posts", "doc", "1").
                source(XContentType.JSON, "title", "In which order are my Elasticsearch queries executed?");
        IndexRequest two = new IndexRequest("posts", "doc", "2")
                .source(XContentType.JSON, "title", "Current status and upcoming changes in Elasticsearch");
        IndexRequest three = new IndexRequest("posts", "doc", "3")
                .source(XContentType.JSON, "title", "The Future of Federated Search in Elasticsearch");
        bulkProcessor.add(one);
        bulkProcessor.add(two);
        bulkProcessor.add(three);

        // 关闭执行器
        // 第一种方式
        // Closes the processor. If flushing by time is enabled, then it's shutdown. Any remaining bulk actions are flushed.
        // 等待时间达到的,会关闭执行器
        // timeout The maximum time to wait for the bulk requests to complete  最大等待时间
        // The time unit of the {@code timeout} argument   时间单位
        try {
            bulkProcessor.awaitClose(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            System.out.println("执行器关闭失败"+bulkProcessor);
            e.printStackTrace();
        }

        // 第二种方式
        // Closes the processor. If flushing by time is enabled, then it's shutdown. Any remaining bulk actions are flushed.
        // 立即关闭执行器
        bulkProcessor.close();
    }
}
