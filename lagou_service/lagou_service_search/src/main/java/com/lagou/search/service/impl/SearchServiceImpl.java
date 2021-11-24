package com.lagou.search.service.impl;

import com.alibaba.fastjson.JSON;
import com.lagou.entity.Result;
import com.lagou.pojo.Sku;
import com.lagou.search.feign.SkuFeign;
import com.lagou.search.mapper.SearchMapper;
import com.lagou.search.pojo.SkuInfo;
import com.lagou.search.service.SearchService;
import org.apache.commons.lang.StringUtils;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.StringTerms;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class SearchServiceImpl implements SearchService {

    @Autowired
    private ElasticsearchTemplate esTemplate;

    @Autowired
    private SearchMapper searchMapper;

    @Autowired
    private SkuFeign skuFeign;

    @Override
    public void createIndexAndMapping() {
        // 创建索引
        esTemplate.createIndex(SkuInfo.class);
        // 创建映射
        esTemplate.putMapping(SkuInfo.class);
    }


    /*** 根据spuid导入数据到ES索引库 * @param spuId 商品id */
    @Override
    public void importDataToES(String spuId) {
        List<Sku> skuList = skuFeign.findListBySpuId(spuId);
        List<SkuInfo> skuInfos = JSON.parseArray(JSON.toJSONString(skuList), SkuInfo.class);
        // 设置规格
        for (SkuInfo skuInfo : skuInfos) {
            skuInfo.setSpecMap(JSON.parseObject(skuInfo.getSpec(), Map.class));
        }
        searchMapper.saveAll(skuInfos);
    }

    /**
     * 商品搜索
     *
     * @param paramMap
     * @return
     */
    @Override
    public Map search(Map<String, String> paramMap) {
        if (paramMap == null) {
            return null;
        }

        // 定义返回结果集
        Map<String, Object> resultMap = new HashMap<>();

        // 获取查询关键词
        String keywords = paramMap.get("keywords");
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        boolQueryBuilder.must(QueryBuilders.matchQuery("name", keywords).operator(Operator.AND));

        // 品牌过滤
        if (!StringUtils.isBlank(paramMap.get("brand"))) {
            boolQueryBuilder.filter(QueryBuilders.termQuery("brandName", paramMap.get("brand")));
        }

        // 规格过滤 spec_xxx=value
        for (String key : paramMap.keySet()) {
            if (key.startsWith("spec_")) {
                String value = paramMap.get(key);
                boolQueryBuilder.filter(QueryBuilders
                        .termQuery("specMap." + key.substring(5) + ".keyword", value));
            }
        }


        // 1.构建查询条件
        NativeSearchQueryBuilder nativeSearchQueryBuilder = new NativeSearchQueryBuilder();
        nativeSearchQueryBuilder.withQuery(boolQueryBuilder);

        // 添加品牌（分组）聚合
        String skuBrand = "skuBrand";
        nativeSearchQueryBuilder.addAggregation(AggregationBuilders.terms(skuBrand).field("brandName"));

        // 添加规格（分组聚合）
        String skuSpec = "skuSpec";
        nativeSearchQueryBuilder.addAggregation(AggregationBuilders.terms(skuSpec).field("spec.keyword").size(10000));

        // 2.执行查询
        AggregatedPage<SkuInfo> aggregatedPage =
                esTemplate.queryForPage(nativeSearchQueryBuilder.build(), SkuInfo.class);
        // 3.从返回结果中获得信息
        // 结果集
        resultMap.put("rows", aggregatedPage.getContent());
        // 总条目数
        resultMap.put("total", aggregatedPage.getTotalElements());
        // 总页数
        resultMap.put("totalPages", aggregatedPage.getTotalPages());

        // 取出品牌聚合
        // aggregatedPage.getAggregations().get(skuBrand);
        // 通过别名获取结果集
        StringTerms stringTerms = (StringTerms) aggregatedPage.getAggregation(skuBrand);
        // 将stringTerms转换为list
//        List<String> brandList = new ArrayList<>();
//        for (StringTerms.Bucket bucket : stringTerms.getBuckets()) {
//            String keyAsString = bucket.getKeyAsString();
//            brandList.add(keyAsString);
//        }
        List<String> brandList = stringTerms.getBuckets()
                .stream().map(bucket -> bucket.getKeyAsString()).collect(Collectors.toList());
        resultMap.put("brandList", brandList);

        // 取出规格聚合并完成类型转换Map<String,Set<String>>
        StringTerms specTerms = (StringTerms) aggregatedPage.getAggregation(skuSpec);
        // [{'颜色':''蓝色, '尺码':'44'},{'颜色':''蓝色, '尺码':'44'}]
        List<String> specList = specTerms.getBuckets()
                .stream().map(bucket -> bucket.getKeyAsString()).collect(Collectors.toList());
        // 定义返回结果
        Map<String, Set<String>> specMap = specList(specList);
        // 放到结果中
        resultMap.put("specList", specMap);


        return resultMap;
    }

    /**
     * 实现规格列表展示实现
     *
     * @param specList
     * @return
     */
    private Map<String, Set<String>> specList(List<String> specList) {
        Map<String, Set<String>> specMap = new HashMap<>();
        for (String spec : specList) {
            // 将json串转换为Map
            Map map = JSON.parseObject(spec, Map.class);
            // 遍历map
            Set<Map.Entry<String, String>> entries = map.entrySet();
            for (Map.Entry<String, String> entry : entries) {
                String key = entry.getKey();
                String value = entry.getValue();
                Set<String> specValues = specMap.get(key);
                if (specValues == null) {
                    specValues = new HashSet<>();
                }
                // 将value添加到set集合中
                specValues.add(value);
                specMap.put(key, specValues);
            }
        }
        return specMap;
    }

    /**
     * *
     * 导入全部数据到ES索引库
     */
    @Override
    public void importAll() {
        Map paramMap = new HashMap();
        paramMap.put("status", "1");
        Result result = skuFeign.findList(paramMap);
        List<SkuInfo> skuInfos = JSON.parseArray(JSON.toJSONString(result.getData()), SkuInfo.class);
        for (SkuInfo skuInfo : skuInfos) {
            skuInfo.setPrice(skuInfo.getPrice());
            skuInfo.setSpecMap(JSON.parseObject(skuInfo.getSpec(), Map.class));
        }
        searchMapper.saveAll(skuInfos);
    }

}
