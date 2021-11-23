package com.lagou.search.feign;

import com.lagou.entity.Result;
import com.lagou.pojo.Sku;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

@FeignClient(name = "goods")
@RequestMapping("/sku")
public interface SkuFeign {

    @GetMapping(value = "/search")
    Result findList(@RequestParam Map searchMap);

    @GetMapping("/findListBySpuId/{spuId}")
    List<Sku> findListBySpuId(@PathVariable String spuId);
}
