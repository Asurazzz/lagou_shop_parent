package com.lagou.goods.dao;

import com.lagou.order.pojo.OrderItem;
import com.lagou.pojo.Sku;
import org.apache.ibatis.annotations.Update;
import tk.mybatis.mapper.common.Mapper;

public interface SkuMapper extends Mapper<Sku> {

    /**
     * 递减库存
     * @param orderItem
     * @return
     */
    @Update("UPDATE tb_sku SET num=num-# {num},sale_num=sale_num+#{num} WHERE id=#{skuId} AND num>=#{num}")
    int decrCount(OrderItem orderItem);
}
