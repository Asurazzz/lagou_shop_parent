package com.lagou.search.service.impl;

import com.alibaba.fastjson.JSON;
import com.lagou.entity.Result;
import com.lagou.pojo.Sku;
import com.lagou.search.feign.SkuFeign;
import com.lagou.search.mapper.SearchMapper;
import com.lagou.search.pojo.SkuInfo;
import com.lagou.search.service.SearchService;
import org.apache.commons.lang.StringUtils;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.StringTerms;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.SearchResultMapper;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.aggregation.impl.AggregatedPageImpl;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class SearchServiceImpl implements SearchService {

    private static final Integer PAGE_SIZE = 5;

    @Autowired
    private ElasticsearchTemplate esTemplate;

    @Autowired
    private SearchMapper searchMapper;

    @Autowired
    private SkuFeign skuFeign;

    @Override
    public void createIndexAndMapping() {
        // ????????????
        esTemplate.createIndex(SkuInfo.class);
        // ????????????
        esTemplate.putMapping(SkuInfo.class);
    }


    /*** ??????spuid???????????????ES????????? * @param spuId ??????id */
    @Override
    public void importDataToES(String spuId) {
        List<Sku> skuList = skuFeign.findListBySpuId(spuId);
        List<SkuInfo> skuInfos = JSON.parseArray(JSON.toJSONString(skuList), SkuInfo.class);
        // ????????????
        for (SkuInfo skuInfo : skuInfos) {
            skuInfo.setSpecMap(JSON.parseObject(skuInfo.getSpec(), Map.class));
        }
        searchMapper.saveAll(skuInfos);
    }

    /**
     * ????????????
     *
     * @param paramMap
     * @return
     */
    @Override
    public Map search(Map<String, String> paramMap) {
        if (paramMap == null) {
            return null;
        }

        // ?????????????????????
        Map<String, Object> resultMap = new HashMap<>();

        // ?????????????????????
        BoolQueryBuilder boolQueryBuilder = setKeyword(paramMap);

        // ???????????????
        fieldsFilter(paramMap, boolQueryBuilder);


        // 1.??????????????????
        NativeSearchQueryBuilder nativeSearchQueryBuilder = new NativeSearchQueryBuilder();
        nativeSearchQueryBuilder.withQuery(boolQueryBuilder);

        // ??????????????????????????????
        String skuBrand = "skuBrand";
        nativeSearchQueryBuilder.addAggregation(AggregationBuilders.terms(skuBrand).field("brandName"));

        // ??????????????????????????????
        String skuSpec = "skuSpec";
        nativeSearchQueryBuilder.addAggregation(AggregationBuilders.terms(skuSpec).field("spec.keyword").size(10000));

        // ???????????????
        setHighlight(nativeSearchQueryBuilder);


        // ????????????
        setOrder(paramMap, nativeSearchQueryBuilder);


        // ?????????????????????
        setPageInfo(paramMap, nativeSearchQueryBuilder);


        // 2.????????????
        // AggregatedPage<SkuInfo> aggregatedPage = esTemplate.queryForPage(nativeSearchQueryBuilder.build(), SkuInfo.class);
        AggregatedPage<SkuInfo> aggregatedPage = executeQuery(nativeSearchQueryBuilder);
        // 3.??????????????????????????????
        // ?????????
        resultMap.put("rows", aggregatedPage.getContent());
        // ????????????
        resultMap.put("total", aggregatedPage.getTotalElements());
        // ?????????
        resultMap.put("totalPages", aggregatedPage.getTotalPages());

        // ??????????????????
        // aggregatedPage.getAggregations().get(skuBrand);
        // ???????????????????????????
        getBrandAgg(resultMap, skuBrand, aggregatedPage);

        // ???????????????????????????????????????Map<String,Set<String>>
        getSpecAgg(resultMap, skuSpec, aggregatedPage);


        return resultMap;
    }

    private void getSpecAgg(Map<String, Object> resultMap, String skuSpec, AggregatedPage<SkuInfo> aggregatedPage) {
        StringTerms specTerms = (StringTerms) aggregatedPage.getAggregation(skuSpec);
        // [{'??????':''??????, '??????':'44'},{'??????':''??????, '??????':'44'}]
        List<String> specList = specTerms.getBuckets()
                .stream().map(bucket -> bucket.getKeyAsString()).collect(Collectors.toList());
        // ??????????????????
        Map<String, Set<String>> specMap = specList(specList);
        // ???????????????
        resultMap.put("specList", specMap);
    }

    private void getBrandAgg(Map<String, Object> resultMap, String skuBrand, AggregatedPage<SkuInfo> aggregatedPage) {
        StringTerms stringTerms = (StringTerms) aggregatedPage.getAggregation(skuBrand);
        // ???stringTerms?????????list
//        List<String> brandList = new ArrayList<>();
//        for (StringTerms.Bucket bucket : stringTerms.getBuckets()) {
//            String keyAsString = bucket.getKeyAsString();
//            brandList.add(keyAsString);
//        }
        List<String> brandList = stringTerms.getBuckets()
                .stream().map(bucket -> bucket.getKeyAsString()).collect(Collectors.toList());
        resultMap.put("brandList", brandList);
    }

    private AggregatedPage<SkuInfo> executeQuery(NativeSearchQueryBuilder nativeSearchQueryBuilder) {
        AggregatedPage<SkuInfo> aggregatedPage = esTemplate.queryForPage(
                nativeSearchQueryBuilder.build(),
                SkuInfo.class,
                new SearchResultMapper() {
                    /**
                     * ???????????????????????????????????????
                     * @param response ????????????????????????
                     * @param aClass    ????????????
                     * @param pageable  ????????????
                     * @param <T>
                     * @return
                     */
                    @Override
                    public <T> AggregatedPage<T> mapResults(SearchResponse response, Class<T> aClass, Pageable pageable) {

                        List<T> list = new ArrayList<>();
                        // ?????????????????????
                        SearchHits hits = response.getHits();
                        for (SearchHit searchHit : hits) {
                            // ??????????????????????????????????????? ??????????????????????????????
                            SkuInfo skuInfo = JSON.parseObject(searchHit.getSourceAsString(), SkuInfo.class);
                            // ????????????????????????
                            Map<String, HighlightField> highlightFields = searchHit.getHighlightFields();
                            // ??????????????????
                            if (highlightFields != null && highlightFields.size() > 0) {
                                HighlightField highlightField = highlightFields.get("name");
                                if (highlightField != null) {
                                    // ??????????????????
                                    Text[] fragments = highlightField.getFragments();
                                    StringBuffer sb = new StringBuffer();
                                    for (Text text : fragments) {
                                        sb.append(text.toString());
                                    }
                                    // ??????
                                    skuInfo.setName(sb.toString());
                                    list.add((T) skuInfo);
                                }
                            }
                        }

                        return new AggregatedPageImpl<>(list, pageable, hits.getTotalHits(), response.getAggregations());
                    }
                });
        return aggregatedPage;
    }

    private void setPageInfo(Map<String, String> paramMap, NativeSearchQueryBuilder nativeSearchQueryBuilder) {
        String pageNum = paramMap.get("pageNum");
        if (StringUtils.isEmpty(pageNum)) {
            pageNum = "1";
        }
        nativeSearchQueryBuilder.withPageable(PageRequest.of(Integer.parseInt(pageNum) - 1, PAGE_SIZE));
    }

    private void setOrder(Map<String, String> paramMap, NativeSearchQueryBuilder nativeSearchQueryBuilder) {
        String sortField = paramMap.get("sortField");
        String sortRule = paramMap.get("sortRule");
        if (!StringUtils.isEmpty(sortField) && !(StringUtils.isEmpty(sortRule))) {
            nativeSearchQueryBuilder.withSort(new FieldSortBuilder(sortField).order(SortOrder.valueOf(sortRule.toUpperCase())));
        }
    }

    private void setHighlight(NativeSearchQueryBuilder nativeSearchQueryBuilder) {
        HighlightBuilder.Field field = new HighlightBuilder.Field("name");
        // ?????????????????????
        field.preTags("<span style='color:red'");
        field.postTags("</span>");
        nativeSearchQueryBuilder.withHighlightFields(field);
    }

    private void fieldsFilter(Map<String, String> paramMap, BoolQueryBuilder boolQueryBuilder) {
        // ????????????
        if (!StringUtils.isBlank(paramMap.get("brand"))) {
            boolQueryBuilder.filter(QueryBuilders.termQuery("brandName", paramMap.get("brand")));
        }

        // ???????????? spec_xxx=value
        for (String key : paramMap.keySet()) {
            if (key.startsWith("spec_")) {
                String value = paramMap.get(key);
                boolQueryBuilder.filter(QueryBuilders
                        .termQuery("specMap." + key.substring(5) + ".keyword", value));
            }
        }

        // ???????????? 0-500 500-1000  1000-1500.......
        String price = paramMap.get("price");
        if (!StringUtils.isBlank(price)) {
            price = price.replace("???", "").replace("??????", "");
            String[] prices = price.split("-");
            if (prices != null && prices.length > 0) {
                boolQueryBuilder.filter(QueryBuilders.rangeQuery("price").gte(prices[0]));
                if (prices.length == 2) {
                    boolQueryBuilder.filter(QueryBuilders.rangeQuery("price").lte(prices[1]));
                }
            }
        }
    }

    /**
     * ???????????????
     * @param paramMap
     * @return
     */
    private BoolQueryBuilder setKeyword(Map<String, String> paramMap) {
        String keywords = paramMap.get("keywords");
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        boolQueryBuilder.must(QueryBuilders.matchQuery("name", keywords).operator(Operator.AND));
        return boolQueryBuilder;
    }

    /**
     * ??????????????????????????????
     *
     * @param specList
     * @return
     */
    private Map<String, Set<String>> specList(List<String> specList) {
        Map<String, Set<String>> specMap = new HashMap<>();
        for (String spec : specList) {
            // ???json????????????Map
            Map map = JSON.parseObject(spec, Map.class);
            // ??????map
            Set<Map.Entry<String, String>> entries = map.entrySet();
            for (Map.Entry<String, String> entry : entries) {
                String key = entry.getKey();
                String value = entry.getValue();
                Set<String> specValues = specMap.get(key);
                if (specValues == null) {
                    specValues = new HashSet<>();
                }
                // ???value?????????set?????????
                specValues.add(value);
                specMap.put(key, specValues);
            }
        }
        return specMap;
    }

    /**
     * *
     * ?????????????????????ES?????????
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
