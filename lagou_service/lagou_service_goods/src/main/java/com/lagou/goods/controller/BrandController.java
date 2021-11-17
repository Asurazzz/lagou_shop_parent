package com.lagou.goods.controller;

import com.github.pagehelper.Page;
import com.lagou.entity.PageResult;
import com.lagou.entity.Result;
import com.lagou.entity.StatusCode;
import com.lagou.goods.service.BrandService;
import com.lagou.pojo.Brand;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.sql.ResultSet;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/brand")
public class BrandController {


    @Autowired
    private BrandService brandService;

    @GetMapping
    public Result findAll() {
        List<Brand> brandList = brandService.queryAll();
        return new Result(true, StatusCode.OK, "查询成功！", brandList);
    }

    /**
     * Get -> http://127.0.0.1:9011/brand/1000
     * 根据id查询
     * @param id
     * @return
     */
    @RequestMapping("/{id}")
    public Result findById(@PathVariable Integer id) {
        Brand brand = brandService.findById(id);
        return new Result(true, StatusCode.OK, "查询成功！", brand);
    }

    @PostMapping
    public Result add(@RequestBody Brand brand) {
        brandService.add(brand);
        return new Result(true, StatusCode.OK, "更新成功！");
    }

    /**
     * 根据id修改品牌信息
     * @param brand
     * @param id
     * @return
     */
    @PutMapping(value = "/{id}")
    public Result update(@RequestBody Brand brand, @PathVariable Integer id) {
        brand.setId(id);
        brandService.update(brand);
        return new Result(true, StatusCode.OK, "操作成功！");
    }

    @DeleteMapping(value = "/{id}")
    public Result delete(@PathVariable Integer id) {
        brandService.delete(id);
        return new Result(true, StatusCode.OK, "操作成功！");
    }

    /**
     * 多条件搜索品牌数据
     * @param searchMap
     * @return
     */
    @GetMapping("/search")
    public Result findList(@RequestParam Map searchMap) {
        List<Brand> list = brandService.findList(searchMap);
        return new Result(true, StatusCode.OK, "查询成功！", list);
    }

    /**
     * 分页搜索实现
     * @param page
     * @param size
     * @return
     */
    @GetMapping(value = "/search/{page}/{size}")
    public Result findPage(@PathVariable int page, @PathVariable int size) {
        Page<Brand> pageList = brandService.findPage(page, size);
        PageResult pageResult = new PageResult(pageList.getTotal(),pageList.getResult());
        return new Result(true, StatusCode.OK, "查询成功！", pageResult);
    }


}
