package com.example.elasticsearch_01_demo.elasticsearch;

import org.apache.http.HttpHost;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.DocWriteResponse;
import org.elasticsearch.action.support.ActiveShardCount;
import org.elasticsearch.action.support.replication.ReplicationResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.Strings;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.VersionType;
import org.elasticsearch.index.get.GetResult;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.script.Script;
import org.elasticsearch.script.ScriptType;
import org.elasticsearch.search.fetch.subphase.FetchSourceContext;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 *  elasticSearch 单个文档API
 *
 *   --- 更新API
 */
public class ElasticSearch_HighLevel_update_Demo {
    public static void main(String[] args) {
        // 1. 获得对应的连接
        // 实现了Closeable接口,不需要手动的关闭,会自动关闭
        RestHighLevelClient restHighLevelClient = new RestHighLevelClient(RestClient.builder(new HttpHost("127.0.0.1",9200)));

        // 2. 封装删除请求参数
        // index The index to get the document from 获得文档的索引
        // type  The type of the document   文档的类型
        // id    The id of the document     文档的id
        UpdateRequest updateRequest = new UpdateRequest("customer","docs","1");

        // update with a script
        // 脚本可以被提供作为一个内联脚本
        // 1. 脚本参数作为一个内联map对象
        Map<String, Object> parameters = Collections.singletonMap("count", 4);
        // 2. 创建一个内联脚本使用painless语言和之前的参数
        // Constructor for a script that does not need to use compiler options.
        // type The {@link ScriptType}.  脚本类型
        // lang  The language for this {@link Script} if the {@link ScriptType} is {@link ScriptType#INLINE}.
        // For {@link ScriptType#STORED} scripts this should be null be specified to access scripts stored as part of the stored scripts deprecated API.
        // idOrCode The id for this {@link Script} if the {@link ScriptType} is {@link ScriptType#STORED}.
        // The code for this {@link Script} if the {@link ScriptType} is {@link ScriptType#INLINE}.
        // params   The user-defined params to be bound for script execution.
        Script inline = new Script(ScriptType.INLINE, "painless", "ctx._source.field += params.count", parameters);
        updateRequest.script(inline);

        // 使用stored脚本
        Script stored =
                new Script(ScriptType.STORED, null, "increment-field", parameters);
        updateRequest.script(stored);

        // 3. 更新文档参数的封装,可以用不同的方式
        // 第一种方式: 手写String串
        // 第二种方式: 提供文档源map自动转成json
        Map<String, String> jsonMap = new HashMap<>();
        jsonMap.put("name","张三");
        updateRequest.doc(jsonMap);
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

        // 如果文档还不存在,可以使用如下方式
        // 类似于局部文档更新时，内容upsert文件可以使用接受的方法定义String，Map，XContentBuilder或 Object密钥对。
        String jsonString = "{\"created\":\"2017-01-01\"}";
        updateRequest.upsert(jsonString, XContentType.JSON);

        // 4. 设置文档请求的参数配置
        // Controls the shard routing of the request. Using this value to hash the shard
        //控制分片路由的请求,用这个值进行hash分片
        updateRequest.routing("routing");

        // Sets the parent id of this document.
        // 设置此文档的父Id
        updateRequest.parent("parent");

        // A timeout to wait if the index operation can't be performed immediately. Defaults to <tt>1m</tt>.
        // 设置等待索引超时的时间 默认是一分钟
        updateRequest.timeout(TimeValue.timeValueSeconds(1));
        updateRequest.timeout("1s");

        updateRequest.setRefreshPolicy("wait_for");

        // 设置存储版本号
        updateRequest.version(2);
        // 设置版本类型 内部的,外部的
        updateRequest.versionType(VersionType.EXTERNAL);
        // Sets the number of retries of a version conflict occurs because the document was updated between
        // getting it and updating it. Defaults to 0.
        // 设置重试更新操作的次数在版本冲突的时候,如果要更新的文档已被更新操作的获取和索引阶段之间的另一个操作更改.
        updateRequest.retryOnConflict(3);
        // Indicates whether the response should contain the updated _source.
        // 启用源检索，默认禁用
        updateRequest.fetchSource(true);
        // 配置特定字段的源排除和源包含
        String[] includes = new String[]{"message", "*Date"};
        String[] excludes = Strings.EMPTY_ARRAY;
        FetchSourceContext fetchSourceContext = new FetchSourceContext(true, includes, excludes);
        updateRequest.fetchSource(fetchSourceContext);
        // Should this update attempt to detect if it is a noop? Defaults to true.
        // 禁用弄noop检测
        updateRequest.detectNoop(true);
        // 指示无论文档是否存在，脚本都必须运行，即如果脚本不存在，则脚本负责创建文档。
        updateRequest.scriptedUpsert(true);
        // 如果不存在，则表明部分文档必须被用作upsert文档。
        updateRequest.docAsUpsert(true);
        // 设置在更新之前必须激活的分片副本的数量
        updateRequest.waitForActiveShards(2);
        // ActiveShardCount可以是ActiveShardCount.ALL， ActiveShardCount.ONE,ActiveShardCount.NONE或ActiveShardCount.DEFAULT（默认）
        // private static final int ALL_ACTIVE_SHARDS = -1;
        // public static final ActiveShardCount DEFAULT = new ActiveShardCount(ACTIVE_SHARD_COUNT_DEFAULT);
        // public static final ActiveShardCount ALL = new ActiveShardCount(ALL_ACTIVE_SHARDS);
        // public static final ActiveShardCount NONE = new ActiveShardCount(0);
        // public static final ActiveShardCount ONE = new ActiveShardCount(1);
        updateRequest.waitForActiveShards(ActiveShardCount.ALL);

        // 5. 执行请求,获得响应
        UpdateResponse updateResponse =null;
        try {
            // 同步执行
            updateResponse = restHighLevelClient.update(updateRequest);
            // The index the document was changed in.
            String index = updateResponse.getIndex();
            // The type of the document changed.
            String type = updateResponse.getType();
            //  The id of the document changed.
            String id = updateResponse.getId();
            if (updateResponse.getResult() == DocWriteResponse.Result.CREATED) {
                System.out.println("处理第一次创建文档的情况（upsert）");
            } else if (updateResponse.getResult() == DocWriteResponse.Result.UPDATED) {
                System.out.println("处理文档更新的情况");
            } else if (updateResponse.getResult() == DocWriteResponse.Result.DELETED) {
                System.out.println("处理文档删除的情况");
            } else if (updateResponse.getResult() == DocWriteResponse.Result.NOOP) {
                System.out.println("处理文档不受更新影响的情况，即不对文档执行操作（noop）");
            }
            ReplicationResponse.ShardInfo shardInfo = updateResponse.getShardInfo();
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
            // 当updateRequest启用源搜索的时候
            // 检索更新的文档作为一个 GetResult
            GetResult result = updateResponse.getGetResult();
            if (result.isExists()) {
                // 检索更新文档的来源 String
                String sourceAsString = result.sourceAsString();
                // 检索更新文档的来源 Map<String, Object>
                Map<String, Object> sourceAsMap = result.sourceAsMap();
                // 检索更新文档的来源 byte[]
                byte[] sourceAsBytes = result.source();
            } else {
                // 处理文档来源不存在于响应中的场景（默认情况下是这种情况）
                System.out.println("文档来源不存在于响应中");
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
        restHighLevelClient.updateAsync(updateRequest, new ActionListener<UpdateResponse>() {
            @Override
            public void onResponse(UpdateResponse updateResponse) {
                System.out.println("处理返回请求");
            }

            @Override
            public void onFailure(Exception e) {
                System.out.println("处理异常情况");
            }
        });

    }
}
