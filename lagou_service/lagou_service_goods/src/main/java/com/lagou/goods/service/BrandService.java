package com.lagou.goods.service;

import com.github.pagehelper.Page;
import com.lagou.pojo.Brand;

import java.util.List;
import java.util.Map;

public interface BrandService {

    /**
     * 查询所有品牌
     * @return
     */
    public List<Brand> queryAll();

    Brand findById(Integer id);

    void add(Brand brand);

    void update(Brand brand);

    void delete(Integer id);

    List<Brand> findList(Map searchMap);

    /**
     * 分页查询
     * @param page
     * @param size
     * @return
     */
    Page<Brand> findPage(int page, int size);
}
