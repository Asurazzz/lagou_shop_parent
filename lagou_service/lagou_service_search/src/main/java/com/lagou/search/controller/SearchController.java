package com.lagou.search.controller;


import com.lagou.entity.Result;
import com.lagou.entity.StatusCode;
import com.lagou.search.service.SearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/search")
public class SearchController {
    @Autowired
    private SearchService searchService;


    @GetMapping
    public Map search(@RequestParam Map<String, String> paramMap) throws Exception {
        Map resultMap = searchService.search(paramMap);
        return resultMap;
    }


    @GetMapping("/createIndexAndMapping")
    public Result createIndexAndMapping() {
        searchService.createIndexAndMapping();
        return new Result(true, StatusCode.OK, "创建成功！");
    }


    @GetMapping("importAll")
    public Result importAllSkuList() {
        searchService.importAll();
        return new Result(true, StatusCode.OK, "导入成功！");
    }
}
