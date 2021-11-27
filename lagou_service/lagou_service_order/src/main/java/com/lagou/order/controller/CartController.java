package com.lagou.order.controller;


import com.lagou.entity.Result;
import com.lagou.entity.StatusCode;
import com.lagou.order.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/cart")
public class CartController {

    @Autowired
    private CartService cartService;

    private String userName = "lagou_user";

    @RequestMapping("/add")
    public Result add(String id, Integer num) {
        cartService.add(id, num, userName);
        return new Result(true, StatusCode.OK, "添加成功!");
    }
}
