package com.example.elasticsearch_01_demo.elasticsearch;

import org.apache.http.HttpHost;
import org.elasticsearch.Build;
import org.elasticsearch.Version;
import org.elasticsearch.action.main.MainResponse;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.cluster.ClusterName;

import java.io.IOException;

/**
 *  elasticSearch 其他API
 *
 *  -- 信息API
 */
public class ElasticSearch_HighLevel_Info_Demo {

    public static void main(String[] args) throws IOException {
        // 初始化搜索滚动上下文
        // 1. 获得对应的连接
        // 实现了Closeable接口,不需要手动的关闭,会自动关闭
        RestHighLevelClient restHighLevelClient = new RestHighLevelClient(RestClient.builder(new HttpHost("127.0.0.1",9200)));

        // Get the cluster info otherwise provided when sending an HTTP request to port 9200
        // 通过HTTP向9200发送请求,获取集群的信息
        MainResponse mainResponse = restHighLevelClient.info();
        // 获取集群名称
        ClusterName clusterName = mainResponse.getClusterName();
        // 检索集群的唯一标识符
        String clusterUuid = mainResponse.getClusterUuid();
        // 检索请求在其上执行的节点的名称
        String nodeName = mainResponse.getNodeName();
        // 检索执行请求的节点的版本
        Version version = mainResponse.getVersion();
        // 检索执行请求的节点的构建信息
        Build build = mainResponse.getBuild();

        System.out.println(clusterName+","+clusterUuid+","+nodeName+","+version+","+build);
    }
}
