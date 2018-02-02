package com.example.elasticsearch;

import org.apache.http.HttpHost;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.support.IndicesOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.unit.Fuzziness;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.metrics.avg.Avg;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.profile.ProfileResult;
import org.elasticsearch.search.profile.ProfileShardResult;
import org.elasticsearch.search.profile.aggregation.AggregationProfileShardResult;
import org.elasticsearch.search.profile.query.CollectorResult;
import org.elasticsearch.search.profile.query.QueryProfileShardResult;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.ScoreSortBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.elasticsearch.search.suggest.Suggest;
import org.elasticsearch.search.suggest.term.TermSuggestion;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 *  elasticSearch 搜索API
 *
 *  -- 搜索API
 */
public class ElasticSearch_HighLevel_Search_Demo {
    public static void main(String[] args) {
        // 1. 获得对应的连接
        // 实现了Closeable接口,不需要手动的关闭,会自动关闭
        RestHighLevelClient restHighLevelClient = new RestHighLevelClient(RestClient.builder(new HttpHost("127.0.0.1",9200)));

        // 2. 构建搜索请求
        // SearchRequest用于具有与搜索文件，聚合，建议和提供高亮显示所产生的文件的方式中的任何操作。
        // Constructs a new search request against the indices. No indices provided here means that search will run against all indices.
        // 更加索引构造一个新的索引请求,如果没有参数,代表根据所有索引构建新的索引请求
        SearchRequest searchRequest = new SearchRequest("customer");

        // 2.1 设置搜索请求参数的封装
        // 设置要执行搜索的文档类型,默认执行所有类型
        searchRequest.types("DOC");
        // 设置一个路由参数
        searchRequest.routing("routing");
        // 设置IndicesOptions控制如何解决不可用的索引以及如何扩展通配符表达式
        searchRequest.indicesOptions(IndicesOptions.lenientExpandOpen());
        // 设置执行搜索的首选项。默认为随机的碎片。可以设置为 _local喜欢当地的碎片，_primary只执行原碎片，
        // 或自定义值，它保证在不同的请求中使用相同的顺序。
        searchRequest.preference("preference");

        // 3. 构建Query
        // 第一种方式 :通过构造方法创建一个完整的文本匹配,查询kimchy在用户属性中
        MatchQueryBuilder matchQueryBuilder = new MatchQueryBuilder("user","kimchy");
        // 在匹配查询上启用模糊匹配
        // Sets the fuzziness used when evaluated to a fuzzy query type. Defaults to "AUTO"
        matchQueryBuilder.fuzziness(Fuzziness.AUTO);
        // Sets the length of a length of common (non-fuzzy) prefix for fuzzy match queries
        // 在匹配查询中设置前缀长度选项
        matchQueryBuilder.prefixLength(3);
        // When using fuzzy or prefix type query, the number of term expansions to use.
        // 设置最大扩展选项来控制查询的模糊处理
        matchQueryBuilder.maxExpansions(10);

        // 第二种方式:通过QueryBuilders创建
        // QueryBuilder matchQueryBuilder = QueryBuilders.matchQuery("user", "kimchy")
        //         .fuzziness(Fuzziness.AUTO)
        //         .prefixLength(3)
        //         .maxExpansions(10);

        // 4. 构建SearchSourceBuilder
        // 搜索行为的大多数选项都可以在其上设置 SearchSourceBuilder，其中包含或多或少相当于Rest API的搜索请求主体中的选项。
        // 构造方法可以读取输入流
        // 使用无参构造创建searchSourceBuilder
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        // 设置查询,可以是任何类型的QueryBuilder
        // Sets the search query for this request.
        // @see org.elasticsearch.index.query.QueryBuilders  这里面的所有Query
        // 第一种方式
        searchSourceBuilder.query(QueryBuilders.termQuery("user","kimchy"));
        // 将QueryBuilder添加到searchSourceBuilder中
        // 第二种方式
        // searchSourceBuilder.query(matchQueryBuilder);
        // 设置from确定结果索引的选项以开始搜索。默认为0。
        searchSourceBuilder.from(10);
        // 设置size确定要返回的搜索匹配数量的选项。默认为10。
        searchSourceBuilder.size(20);
        // 设置一个可选的超时，控制允许搜索的时间。
        searchSourceBuilder.timeout(TimeValue.timeValueMinutes(1));
        // 4.1 构建排序
        // searchSourceBuilder 允许添加一个或者多个SortBuilder 实例 有四种实现
        // FieldSortBuilder(字段), ScoreSortBuilder(得分), GeoDistanceSortBuilder(地理位置) and ScriptSortBuilder(脚本)
        // SortBuilder 可以通过构造方法创建或者 SortBuilders进行获取
        // 按降序排序_score（默认）
        searchSourceBuilder.sort(new ScoreSortBuilder().order(SortOrder.DESC));
        // 也按_uid字段 升序排列
        searchSourceBuilder.sort(new FieldSortBuilder("_uid").order(SortOrder.ASC));
        // 4.2 构建属性过滤
        // 获取特定的字段
        // 关闭_source完全检索
        searchSourceBuilder.fetchSource(false);
        // fetchSource方法可以接受一个或多个通配符模式的数组，以更细粒度地控制包含或排除哪些字段：
        String[] includeFields = new String[] {"title", "user", "innerObject.*"};
        String[] excludeFields = new String[] {"_type"};
        searchSourceBuilder.fetchSource(includeFields, excludeFields);
        // 4.3 构建高亮显示
        // 高亮显示通过HighlightBuilder实现。不同的突出行为可以为每个字段定义中添加一个或多个HighlightBuilder实例。
        // 通过构造方法创建highlightBuilder
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        // 创建一个高亮属性
        HighlightBuilder.Field highlightTitle = new HighlightBuilder.Field("title");
        // 设置字段突出显示类型
        // are <tt>unified(统一)</tt>, <tt>plain</tt> and <tt>fvj</tt>.
        // Defaults to <tt>unified</tt>.
        highlightTitle.highlighterType("unified");
        // 将指定的元素添加到列表的末尾
        highlightBuilder.field(highlightTitle);
        // 可以添加多个
        HighlightBuilder.Field highlightUser = new HighlightBuilder.Field("user");
        highlightBuilder.field(highlightUser);
        // 将高亮构建添加到searchSourceBuilder中
        searchSourceBuilder.highlighter(highlightBuilder);
        // 4.4 之后只需要将SearchSourceBuilder添加到searchRequest即可
        searchRequest.source(searchSourceBuilder);

        // 5.执行搜索请求
        SearchResponse searchResponse = null;
        try {
            searchResponse = restHighLevelClient.search(searchRequest);
            // 5.1 检索聚合
            // 5.1.1 SearchResponse通过首先获取聚合树的根目录Aggregations，然后通过名称获取聚合来获取聚合
            // 获取聚合树的根目录Aggregations
            Aggregations aggregations = searchResponse.getAggregations();
            // 获得by_company的聚合
            // 如果你访问集合的名字，你需要根据聚集您要求的类型指定聚合界面，否则会抛出ClassCastException：
            // 例如: Range range = aggregations.get("by_company");
            Terms byCompanyAggregation = aggregations.get("by_company");
            // Get the bucket for the given term, or null if there is no such bucket.
            // 获取与之关联的存储桶 Elastic
            Terms.Bucket elasticBucket = byCompanyAggregation.getBucketByKey("Elastic");
            // average_age从该桶 获取子聚合
            Avg averageAge = elasticBucket.getAggregations().get("average_age");
            // 获取对应的值
            double avg = averageAge.getValue();
            System.out.println(avg);

            // 5.2 也可以将所有聚合作为由聚合名称键入的映射来访问
            // Returns the {@link Aggregation}s keyed by aggregation name.
            // Map<String, Aggregation> aggregationMap = aggregations.getAsMap();
            // Terms byCompanyAggregation = (Terms) aggregationMap.get("by_company");
            // 返回所有的顶级聚合列表：
            // List<Aggregation> aggregationList = aggregations.asList();
            // 迭代所有聚合，然后决定如何根据其类型进一步处理它们
            // for (Aggregation agg : aggregations) {
            //    String type = agg.getType();
            //    if (type.equals(TermsAggregationBuilder.NAME)) {
            //       Bucket elasticBucket = ((Terms) agg).getBucketByKey("Elastic");
            //       long numberOfDocs = elasticBucket.getDocCount();
            //    }
            // }

            // 5.2 检索建议
            // 使用Suggest该类来访问建议
            Suggest suggest = searchResponse.getSuggest();
            // 建议可以通过名称检索。你需要把它们分配给正确类型的建议类（这里TermSuggestion),否则ClassCastException就抛出一个
            TermSuggestion termSuggestion = suggest.getSuggestion("suggest_user");
            // 迭代建议条目
            for (TermSuggestion.Entry entry : termSuggestion.getEntries()) {
                // 在一个条目中迭代该项
                for (TermSuggestion.Entry.Option option : entry) {
                    String suggestText = option.getText().string();
                }
            }

            // 5.3 检索分析结果
            // If profiling was enabled, this returns an object containing the profile results from each shard.
            // If profiling was not enabled, this will return null
            // 分析结果是SearchResponse使用该getProfileResults()方法检索的。
            // 该方法返回一个Map包含执行中ProfileShardResult涉及的每个分片的对象 SearchRequest。
            // ProfileShardResult存储在Map使用唯一标识分析结果对应的分片的键中。
            Map<String, ProfileShardResult> profileResults = searchResponse.getProfileResults();
            for (Map.Entry<String, ProfileShardResult> profilingResult : profileResults.entrySet()) {
                // 检索标识ProfileShardResult属于 哪个分片的密钥
                String key = profilingResult.getKey();
                // 检索ProfileShardResult给定的分片
                ProfileShardResult profileShardResult = profilingResult.getValue();
                System.out.println("密钥:"+key+",分片"+profileShardResult);
                // ProfileShardResult对象本身包含一个或多个查询简档的结果，一个用于抵靠底层Lucene索引执行的每个查询
                // 检索列表 profileShardResult#getQueryProfileResults
                List<QueryProfileShardResult> queryProfileShardResults = profileShardResult.getQueryProfileResults();
                // 迭代每个 QueryProfileShardResult
                for (QueryProfileShardResult queryProfileShardResult : queryProfileShardResults) {
                    // 每个都QueryProfileShardResult提供对详细的查询树执行的访问，作为ProfileResult对象列表返回
                    // 检索Lucene收集器的性能分析结果
                    CollectorResult collectorResult = queryProfileShardResult.getCollectorResult();
                    // 检索Lucene收集器的名称
                    String collectorName = collectorResult.getName();
                    // 检索用于执行Lucene收集器的毫秒时间
                    Long collectorTimeInMillis = collectorResult.getTime();
                    // 检索子收集器的配置文件结果（如果有的话）
                    List<CollectorResult> profiledShardChildren = collectorResult.getProfiledChildren();
                    // 检索 AggregationProfileShardResult
                    AggregationProfileShardResult aggsProfileResults = profileShardResult.getAggregationProfileResults();
                    // 迭代聚合配置文件结果
                    for (ProfileResult profileResult : aggsProfileResults.getProfileResults()) {
                        // 检索聚合类型（对应于用于执行聚合的Java类）
                        String aggName = profileResult.getQueryName();
                        // 检索用于执行Lucene收集器的毫秒时间
                        long aggTimeInMillis = profileResult.getTime();
                        // 检索子聚合的配置文件结果（如果有的话）
                        List<ProfileResult> aggsProfiledChildren = profileResult.getProfiledChildren();
                    }
                    for (ProfileResult profileResult : queryProfileShardResult.getQueryResults()) {
                        // 检索Lucene查询的名称
                        String queryName = profileResult.getQueryName();
                        // 检索执行Lucene查询所用的毫秒时间
                        long queryTimeInMillis = profileResult.getTime();
                        // 检索子查询的配置文件结果（如果有的话）
                        List<ProfileResult> profiledChildren = profileResult.getProfiledChildren();
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }


    }
}
