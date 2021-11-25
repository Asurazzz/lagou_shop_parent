package com.lagou.feign;

import com.lagou.entity.Result;
import com.lagou.pojo.Sku;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

/**
 * @author 元敬
 * @Version 1.0
 */
@FeignClient(name = "goods")
@RequestMapping("/sku")
public interface SkuFeign {

    @GetMapping("/{id}")
    public Result<Sku> findById(@PathVariable String id);

    /**
     * 库存变更&销量变更
     */
    @PostMapping("/changeCount")
    public Result changeInventoryAndSaleNumber(@RequestParam(value = "username") String username);

    @PostMapping("/resumeStockNum")
    public Result resumeStockNum(@RequestParam String skuId, @RequestParam Integer num);
}
