package com.example.elasticsearch;

import org.apache.http.HttpHost;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.Strings;
import org.elasticsearch.index.VersionType;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.search.fetch.subphase.FetchSourceContext;

import java.io.IOException;
import java.util.Map;

/**
 *  elasticSearch 单个文档API
 *
 *   --- 获取API
 */
public class ElasticSearch_HighLevel_Get_Demo {
    public static void main(String[] args) {

        // 1. 获得对应的连接
        // 实现了Closeable接口,不需要手动的关闭,会自动关闭
        RestHighLevelClient restHighLevelClient = new RestHighLevelClient(RestClient.builder(new HttpHost("127.0.0.1",9200)));

        // 2. 封装获取请求参数
        // index The index to get the document from 获得文档的索引
        // type  The type of the document   文档的类型
        // id    The id of the document     文档的id
        GetRequest  getRequest = new GetRequest("customer","docs","1");

        // 2,1 请求可选参数的设置
        // 允许设置取资源上下文对请求,控制结果是否返回,如何返回
        // Allows setting the {@link FetchSourceContext} for this request, controlling if and how _source should be returned.
        // 禁用源检索，默认启用
        // getRequest.fetchSourceContext(new FetchSourceContext(false));

        // 配置特定字段的源排除和源包含
        String[] includes = new String[]{"message", "*Date"};
        String[] excludes = Strings.EMPTY_ARRAY;
        FetchSourceContext fetchSourceContext = new FetchSourceContext(true, includes, excludes);
        getRequest.fetchSourceContext(fetchSourceContext);

        //  Explicitly specify the stored fields that will be returned. By default, the <tt>_source</tt> field will be returned.
        // 显式指定将返回的存储字段
        getRequest.storedFields("message");
        //控制分片路由的请求,用这个值进行hash分片
        getRequest.routing("routing");
        // Sets the parent id of this document.
        // 设置此文档的父Id
        getRequest.parent("parent");
        // 设置存储版本号
        getRequest.version(2);
        // 设置版本类型 内部的,外部的
        getRequest.versionType(VersionType.EXTERNAL);
        // 设置执行搜索的首选项。默认为随机的碎片。可以设置为 _local喜欢当地的碎片，_primary只执行原碎片，
        // 或自定义值，它保证在不同的请求中使用相同的顺序。
        getRequest.preference("preference");
        // 设置实时标志为false（true默认）
        getRequest.realtime(false);
        // Should a refresh be executed before this get operation causing the operation to
        // return the latest value. Note, heavy get should not set this to true. Defaults
        // to false.
        // 设置为true保证get操作之前,去获取最新的值,如果重新获取值,不应该设置为true
        getRequest.refresh(true);

        // 3. 执行请求,获取响应
        try {
            // 同步执行
            GetResponse getResponse = restHighLevelClient.get(getRequest);
            // The index the document was changed in.
            String index = getResponse.getIndex();
            // The type of the document changed.
            String type = getResponse.getType();
            //  The id of the document changed.
            String id = getResponse.getId();

            // Does the document exists.
            // 判断文档是否存在
            if (getResponse.isExists()) {
                long version = getResponse.getVersion();
                // The source of the document (as a string). 获取资源以字符串的方式
                String sourceAsString = getResponse.getSourceAsString();
                // The source of the document (As a map).  获取资源以map的方式
                Map<String, Object> sourceAsMap = getResponse.getSourceAsMap();
                //   // The source of the document (As a array).  获取资源以byte数组的方式
                byte[] sourceAsBytes = getResponse.getSourceAsBytes();
            } else {
                System.out.println("查询文档不存在");
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
        }catch (IOException e){
            System.out.println("执行异常");
            e.printStackTrace();
        }

        // 异步执行
        restHighLevelClient.getAsync(getRequest, new ActionListener<GetResponse>() {
            @Override
            public void onResponse(GetResponse documentFields) {
                System.out.println("处理返回请求");
            }

            @Override
            public void onFailure(Exception e) {
                System.out.println("处理异常情况");
            }
        });

    }
}
