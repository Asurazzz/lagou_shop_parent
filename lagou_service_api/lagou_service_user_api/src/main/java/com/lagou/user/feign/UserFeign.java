package com.lagou.user.feign;

import com.lagou.entity.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

/**
 * @author 元敬
 * @Version 1.0
 */
@FeignClient(name = "user")
@RequestMapping("/user")
public interface UserFeign {

    @GetMapping({"/load/{username}"})
    public Result findById(@PathVariable String username);

    /**
     * 增加积分
     * @param points
     */
    @PostMapping("/points/add")
    Result addPoints(@RequestParam(value = "points") Integer points);

}
