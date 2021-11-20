package com.lagou.goods.dao;

import com.lagou.pojo.Spec;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;
import java.util.Map;

public interface SpecMapper extends Mapper<Spec> {

    /**
     * 根据商品分类名称查询规格列表
     * @param categoryName
     * @return
     */
    @Select("SELECT name,options FROM tb_spec WHERE template_id IN ( SELECT template_id FROM tb_category WHERE NAME=#{categoryName}) order by seq")
    List<Map> findListByCategoryName(@Param("categoryName") String categoryName);
}
