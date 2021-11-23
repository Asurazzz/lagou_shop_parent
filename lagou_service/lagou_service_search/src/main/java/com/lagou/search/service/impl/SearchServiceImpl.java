package com.lagou.search.service.impl;

import com.lagou.search.pojo.SkuInfo;
import com.lagou.search.service.SearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.stereotype.Service;

@Service
public class SearchServiceImpl implements SearchService {

    @Autowired
    private ElasticsearchTemplate esTemplate;

    @Override
    public void createIndexAndMapping() {
        // 创建索引
        esTemplate.createIndex(SkuInfo.class);
        // 创建映射
        esTemplate.putMapping(SkuInfo.class);
    }
}
