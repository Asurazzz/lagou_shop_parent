package com.lagou.order.service.impl;

import com.lagou.entity.Result;
import com.lagou.feign.SkuFeign;
import com.lagou.feign.SpuFeign;
import com.lagou.order.pojo.OrderItem;
import com.lagou.order.service.CartService;
import com.lagou.pojo.Sku;
import com.lagou.pojo.Spu;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;


@Service
public class CartServiceImpl implements CartService {

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private SpuFeign spuFeign;

    @Autowired
    protected SkuFeign skuFeign;

    private static final String CART = "CART_";

    @Override
    public void add(String id, Integer num, String userName) {
        // 1.访问商品微服务获得sku和spu
        Result<Sku> skuResult = skuFeign.findById(id);
        Sku sku = skuResult.getData();
        Result<Spu> spuResult = spuFeign.findById(sku.getSpuId());
        Spu spu = spuResult.getData();
        // 2.转换成OrderItem
        OrderItem orderItem = new OrderItem();
        orderItem.setCategoryId1(spu.getCategory1Id());
        orderItem.setCategoryId2(spu.getCategory2Id());
        orderItem.setCategoryId3(spu.getCategory3Id());
        orderItem.setSpuId(spu.getId());
        orderItem.setSkuId(sku.getId());
        orderItem.setName(sku.getName());
        orderItem.setPrice(sku.getPrice());
        orderItem.setNum(num);
        orderItem.setMoney(num * orderItem.getPrice());
        orderItem.setPayMoney(num * orderItem.getPrice());
        orderItem.setImage(sku.getImage());
        orderItem.setWeight(sku.getWeight() * num);
        // 3. 保存
        redisTemplate.boundHashOps(CART + userName).put(id, orderItem);
    }
}
