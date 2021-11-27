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
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;


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
        RedisSerializer stringRediSserializer = new StringRedisSerializer();
        redisTemplate.setKeySerializer(stringRediSserializer);
        redisTemplate.setHashKeySerializer(stringRediSserializer);
        //redisTemplate.setHashValueSerializer(stringRediSserializer);


        // 如果传递过来的是<= 0 的情况
        if (num <= 0) {
            // 移除购物车的该商品明细
            redisTemplate.boundHashOps(CART + userName).delete(id);
            return;
        }
        // 判断购物车中是否有该商品
        OrderItem orderItem = (OrderItem) redisTemplate.boundHashOps(CART + userName).get(id);
        if (orderItem != null) {
            orderItem.setNum(orderItem.getNum() + num);
            orderItem.setMoney(orderItem.getNum() * orderItem.getPrice());
            orderItem.setPayMoney(orderItem.getNum() * orderItem.getPrice());
        } else {
            // 1.访问商品微服务获得sku和spu
            Result<Sku> skuResult = skuFeign.findById(id);
            Sku sku = skuResult.getData();
            Result<Spu> spuResult = spuFeign.findById(sku.getSpuId());
            Spu spu = spuResult.getData();
            // 2.转换成OrderItem
            orderItem = parseToOrderItem(num, sku, spu);
        }

        // 3. 保存
        redisTemplate.boundHashOps(CART + userName).put(id, orderItem);
    }

    @Override
    public Map list(String userName) {
        RedisSerializer stringRediSserializer = new StringRedisSerializer();
        redisTemplate.setKeySerializer(stringRediSserializer);
        redisTemplate.setHashKeySerializer(stringRediSserializer);
        // 定义返回结果
        Map<String, Object> resultMap = new HashMap<>();
        // 读取
        List<OrderItem> values = redisTemplate.boundHashOps(CART + userName).values();
        resultMap.put("orderItemList", values);
        // 商品总数以及商品总价格
        Integer totalNum = 0;
        Integer totalPrice = 0;
        for (OrderItem orderItem : values) {
            totalNum += orderItem.getNum();
            totalPrice += orderItem.getMoney();
        }
        resultMap.put("totalNum", totalNum);
        resultMap.put("totalPrice", totalPrice);
        return resultMap;
    }

    @Override
    public void delete(String skuId, String userName) {
        RedisSerializer stringRediSserializer = new StringRedisSerializer();
        redisTemplate.setKeySerializer(stringRediSserializer);
        redisTemplate.setHashKeySerializer(stringRediSserializer);
        redisTemplate.opsForHash().delete(CART + userName, skuId);
    }

    @Override
    public void updateCheckedStatus(String skuId, Boolean checked, String userName) {
        RedisSerializer stringRediSserializer = new StringRedisSerializer();
        redisTemplate.setKeySerializer(stringRediSserializer);
        redisTemplate.setHashKeySerializer(stringRediSserializer);
        // 读取购物车中所有的key，一个又一个skuid
        Set keys = redisTemplate.boundHashOps(CART + userName).keys();
        for (Object key : keys) {
            if (key.equals(skuId)) {
                OrderItem orderItem = (OrderItem) redisTemplate.boundHashOps(CART + userName).get(skuId);
                orderItem.setChecked(checked);
                redisTemplate.boundHashOps(CART + userName).put(skuId, orderItem);
            }
        }
    }

    private OrderItem parseToOrderItem(Integer num, Sku sku, Spu spu) {
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
        return orderItem;
    }
}
