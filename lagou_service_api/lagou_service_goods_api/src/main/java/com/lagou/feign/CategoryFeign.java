package com.lagou.feign;

import com.lagou.pojo.Category;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

public interface CategoryFeign {

    /**
     * 根据ID查询数据
     * @param id
     * @return
     */
    @GetMapping("/findCategoryById/{id}")
    Category findCategoryById(@PathVariable(name = "id") Integer id);
}
