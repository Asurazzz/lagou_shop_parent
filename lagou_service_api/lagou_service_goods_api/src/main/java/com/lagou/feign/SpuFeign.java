package com.lagou.feign;

import com.lagou.entity.Result;
import com.lagou.pojo.Spu;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author 元敬
 * @Version 1.0
 */
@FeignClient(name = "goods")
@RequestMapping("/spu")
public interface SpuFeign {

    @GetMapping("/{id}")
    Result<Spu> findById(@PathVariable String id);
}
