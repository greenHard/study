package com.example.elasticsearch_01_demo.elasticsearch;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.apache.http.impl.nio.reactor.IOReactorConfig;
import org.apache.http.message.BasicHeader;
import org.apache.http.nio.entity.NStringEntity;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.ssl.SSLContexts;
import org.elasticsearch.client.*;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyStore;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

/**
 * elasticSearch 简单API的使用(全)
 * 参照 https://www.elastic.co/guide/en/elasticsearch/client/java-rest/current/java-rest-low.html
 */
public class ElasticSearch_LowApi_demo01 {

    public static void main(String[] args) throws Exception {

        // 第一步: 获取连接和连接的配置
        RestClientBuilder clientBuilder = RestClient.builder(new HttpHost("127.0.0.1", 9200, "https"));

        // 身份认证可以通过clientBuilder的setHttpClientConfigCallback方法
        // 创建认证对象
        final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();

        // 设置认证用户名和密码
        credentialsProvider.setCredentials(AuthScope.ANY,
                new UsernamePasswordCredentials("user", "password"));

        // setHttpClientConfigCallback 可以用来验证和可以用来加密(加密不建议使用)
        // the type of keystore.

        // 密钥的类型
         KeyStore truststore = KeyStore.getInstance("jks");

        // 客户端store的路径
        //  1. the path string or initial part of the path string
        //  2. additional strings to be joined to form the path string
        Path path = Paths.get("C:\\Users\\m1304\\Desktop\\zheng\\","truststore.jks");
        try (InputStream is = Files.newInputStream(path)) {
              truststore.load(is, "changeit".toCharArray());
        }
        SSLContextBuilder sslBuilder = SSLContexts.custom().loadTrustMaterial(truststore, null);
        final SSLContext sslContext = sslBuilder.build();
        clientBuilder.setHttpClientConfigCallback((httpClientBuilder)->{
                // 禁用抢占式身份验证
                httpClientBuilder.disableAuthCaching();
                // 作为一个参数暴露多种方法来配置加密通信接收setSSLContext，setSSLSessionStrategy并且 setConnectionManager
                return httpClientBuilder.setSSLContext(sslContext);
                // return httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
                }
        );


        // Set the default headers that need to be sent with each request, to prevent having to specify them with each single request
        Header[] deafaultHeaders = new Header[]{new BasicHeader("header", "value")};
        clientBuilder.setDefaultHeaders(deafaultHeaders);

        // 设置相同请求最大的超时时间 默认是30000
        clientBuilder.setMaxRetryTimeoutMillis(10000);

        // 设置一个监听， 每次节点失败时都会收到通知，以防需要采取措施。在启用失败时进行嗅探时内部使用。
        clientBuilder.setFailureListener(new RestClient.FailureListener() {
            @Override
            public void onFailure(HttpHost host) {
                // 进行回调函数的处理
            }
        });

        // 设置一个监听,允许修改默认的请求配置,例如请求超时，验证，或任何 org.apache.http.client.config.RequestConfig.Builder 允许设置的任何内容
        clientBuilder.setRequestConfigCallback(new RestClientBuilder.RequestConfigCallback() {
            @Override
            public RequestConfig.Builder customizeRequestConfig(RequestConfig.Builder requestConfigBuilder) {
                // 设置了连接超时时间,默认是1秒,减少了网络通信时间,默认是30秒 都是以毫秒为单位的
                return requestConfigBuilder.setConnectTimeout(5000).setSocketTimeout(10000);
            }
        });

        // 设置一个监听,设置允许修改http客户端配置的回调（例如通过ssl进行的加密通信或org.apache.http.impl.nio.client.HttpAsyncClientBuilder 允许设置的任何内容
        clientBuilder.setHttpClientConfigCallback(new RestClientBuilder.HttpClientConfigCallback() {
            @Override
            public HttpAsyncClientBuilder customizeHttpClient(HttpAsyncClientBuilder httpAsyncClientBuilder) {
                // 修改了线程数
                return httpAsyncClientBuilder.setProxy(new HttpHost("127.0.0.1", 9200)).setDefaultIOReactorConfig(IOReactorConfig.custom().setIoThreadCount(1).build());
            }
        });

        // 第一步: 获取客户端连接
        RestClient restClient = clientBuilder.build();

        // Returns an immutable map, mapping only the specified key to the
        // specified value.  The returned map is serializable.  返回不可改变的map,可序列化的
        Map<String, String> params = Collections.singletonMap("pretty", "true");
        String jsonString = "{\"size\": 20, \n" +
                " \t\"query\": {\n" +
                "    \t\"bool\": {\n" +
                "      \t\"must\": [\n" +
                "        \t{\"match\": {\"address\": \"Place\"}}\n" +
                "  \t\t],\n" +
                "      \t\"filter\": {\n" +
                "        \t\"range\": {\n" +
                "          \t\t\"balance\": {\n" +
                "            \t\t\"gte\": 30000,\n" +
                "            \t\t\"lte\": 50000\n" +
                "          \t\t}\n" +
                "        \t}\n" +
                "      \t}\n" +
                "    }\n" +
                "  }\n" +
                "}";
        // The ContentType specified for the HttpEntity is important because it will be used to set the Content-Type header so that Elasticsearch can properly parse the content.
        HttpEntity entity = new NStringEntity(jsonString, ContentType.APPLICATION_JSON);

        // 第二步: 准备请求参数
        // the http method  http请求方法
        // the path of the request (without host and port) http请求路径不包括host和端口
        // the query_string parameters  请求参数
        // entity the body of the request, null if not applicable  请求实体内容
        // headers the optional request headers 可选择的请求头
        Response response;
        try {
            // 同步调用
             response = restClient.performRequest("GET", "/blank/account/_search", params,entity);

             // 第三步:处理响应信息
            if (response != null) {
                // 获取Entity对象,里面有响应体
                // 该 HttpEntity的getContent方法来得方便，它返回InputStream 来自先前缓冲的响应主体的读取
                response.getEntity();
                // 获取响应的主机
                response.getHost();
                // 获取响应状态行，您可以从中检索状态码
                response.getStatusLine();
                // 获取所有响应头，也可以通过名称检索 getHeader(String)
                response.getHeaders();
                //关于执行的请求的信息
                response.getRequestLine();
            }
        } catch (ResponseException e) {
            // 通信问题（例如SocketTimeoutException）
            System.out.println("-----通信出现问题,请检查你的网络-----");
            e.printStackTrace();

        }catch (IOException e){
            // 返回了一个响应，但是它的状态代码指示了一个错误（不2xx）。
            // 一个ResponseException来自一个有效的http响应，因此它暴露了相应的Response对象，它允许访问返回的响应。
            System.out.println("-----响应出错-----");
            e.printStackTrace();
        }finally {
            //第三步: 关闭连接
            try {
                restClient.close();
            } catch (IOException e) {
                restClient = null;
                e.printStackTrace();
            }
        }


        //异步调用
        // method the http method  http请求的方式
        // endpoint the path of the request (without host and port) 请求的路径
        // params the query_string parameters  查询的参数
        // entity the body of the request, null if not applicable  请求实体内容
        // httpAsyncResponseConsumerFactory the {@link HttpAsyncResponseConsumerFactory } used to create one
        //         * {@link HttpAsyncResponseConsumer
        //            } callback per retry. Controls how the response body gets streamed from a non-blocking HTTP
        //                    * connection on the client side. http异步响应工厂
        // 返回监听器  responseListener the {@link ResponseListener  } to notify when the request is completed or fails
        // headers the optional request headers 请求头
        // 每个请求尝试创建回调实例的可选工厂发送异步请求。控制响应正文如何从客户端的非阻塞HTTP连接进行流式传输。
        // 如果未提供，则使用默认实现，将整个响应主体缓存在堆内存中，最大为100 MB。
        // 设置为30 MB
        // 计数器,同时只能有一个线程去操作这个计数器
        final CountDownLatch downLatch  = new CountDownLatch(2);
        for(int i =0;i<2;i++){
            HttpAsyncResponseConsumerFactory consumerFactory = new HttpAsyncResponseConsumerFactory.HeapBufferedResponseConsumerFactory(30 * 1024 * 1024);
            restClient.performRequestAsync("POST", "/blank/account/_search",params, entity, consumerFactory, new ResponseListener() {
                @Override
                public void onSuccess(Response response) {
                    // 处理返回的响应
                }

                @Override
                public void onFailure(Exception exception) {
                    // 处理返回的异常
                }
            });
        }
        try {
            downLatch.await();
        } catch (InterruptedException e) {
            System.out.println("------有线程没走完,出现异常了-------");
            e.printStackTrace();
        }
        System.out.println("所有任务都走完了,success");

    }
}
