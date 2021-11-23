package com.lagou.search.service.impl;

import com.alibaba.fastjson.JSON;
import com.lagou.entity.Result;
import com.lagou.pojo.Sku;
import com.lagou.search.feign.SkuFeign;
import com.lagou.search.mapper.SearchMapper;
import com.lagou.search.pojo.SkuInfo;
import com.lagou.search.service.SearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
//    @Override
//    public void importDataToESBySpuId(String spuId) {
//        List<Sku> skuList = skuFeign.findSkuListBySpuId(spuId);
//        List<SkuInfo> skuInfos = JSON.parseArray(JSON.toJSONString(skuList), SkuInfo.class);
//        for (SkuInfo skuInfo : skuInfos) {
//            skuInfo.setSpecMap(JSON.parseObject(skuInfo.getSpec(), Map.class));
//        }
//        searchMapper.saveAll(skuInfos);
//    }

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
