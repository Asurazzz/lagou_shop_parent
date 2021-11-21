package com.lagou.goods.controller;

import com.github.pagehelper.Page;
import com.lagou.entity.PageResult;
import com.lagou.entity.Result;
import com.lagou.entity.StatusCode;
import com.lagou.goods.service.SpuService;
import com.lagou.pojo.Goods;
import com.lagou.pojo.Spu;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * {
 * "spu": {
 * "name": "拉勾手机",
 * "caption": "东半球最好用的手机",
 * "brandId": 8557,
 * "category1Id": 558,
 * "category2Id": 559,
 * "category3Id": 560,
 * "freightId": 10,
 * "image": "http://www.lagou.com/image/1.jpg",
 * "images": "http://www.lagou.com/image/1.jpg,http://www.lagou.com/ima ge/2.jpg",
 * "introduction": "这个是商品详情，html代码",
 * "paraItems": "{'出厂年份':'2021','赠品':'充电器'}",
 * "saleService": "七天包退,闪电退货",
 * "sn": "020102331",
 * "specItems": "{'颜色':['红','绿'],'机身内存': ['64G','8G']}",
 * "templateId": 42
 * },
 * "skuList": [
 * {
 * "sn": "10192010292",
 * "num": 100,
 * "alertNum": 20,
 * "price": 900000,
 * "spec": "{'颜色':'红';,'机身内存':'64G'}",
 * "image": "http://www.lagou.com/image/1.jpg",
 * "images": "http://www.lagou.com/image/1.jpg,http://www.lagou.com/ima ge/2.jpg",
 * "status": "1",
 * "weight": 130
 * },
 * {
 * "sn": "10192010293",
 * "num": 100,
 * "alertNum": 20,
 * "price": 600000,
 * "spec": "{'颜色':'绿';,'机身内存':'32G'}",
 * "image": "http://www.lagou.com/image/1.jpg",
 * "images": "http://www.lagou.com/image/1.jpg,http://www.lagou.com/ima ge/2.jpg",
 * "status": "1",
 * "weight": 130
 * }
 * ]
 * }
 */
@RestController
@CrossOrigin
@RequestMapping("/spu")
public class SpuController {


    @Autowired
    private SpuService spuService;

    /**
     * 查询全部数据
     *
     * @return
     */
    @GetMapping
    public Result findAll() {
        List<Spu> spuList = spuService.findAll();
        return new Result(true, StatusCode.OK, "查询成功", spuList);
    }

    /***
     * 根据ID查询数据
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public Result findById(@PathVariable String id) {
        Goods goods = spuService.findGoodsById(id);
        return new Result(true, StatusCode.OK, "查询成功", goods);
    }


    /***
     * 新增数据
     * @param goods
     * @return
     */
    @PostMapping
    public Result add(@RequestBody Goods goods) {
        spuService.add(goods);
        return new Result(true, StatusCode.OK, "添加成功");
    }


    /***
     * 修改数据
     * @param goods
     * @param id
     * @return
     */
    @PutMapping(value = "/{id}")
    public Result update(@RequestBody Goods goods, @PathVariable String id) {
        goods.getSpu().setId(id);
        spuService.update(goods);
        return new Result(true, StatusCode.OK, "修改成功");
    }


    /***
     * 根据ID删除品牌数据
     * @param id
     * @return
     */
    @DeleteMapping(value = "/{id}")
    public Result delete(@PathVariable String id) {
        spuService.delete(id);
        return new Result(true, StatusCode.OK, "删除成功");
    }

    /***
     * 多条件搜索品牌数据
     * @param searchMap
     * @return
     */
    @GetMapping(value = "/search")
    public Result findList(@RequestParam Map searchMap) {
        List<Spu> list = spuService.findList(searchMap);
        return new Result(true, StatusCode.OK, "查询成功", list);
    }


    /***
     * 分页搜索实现
     * @param searchMap
     * @param page
     * @param size
     * @return
     */
    @GetMapping(value = "/search/{page}/{size}")
    public Result findPage(@RequestParam Map searchMap, @PathVariable int page, @PathVariable int size) {
        Page<Spu> pageList = spuService.findPage(searchMap, page, size);
        PageResult pageResult = new PageResult(pageList.getTotal(), pageList.getResult());
        return new Result(true, StatusCode.OK, "查询成功", pageResult);
    }


    /**
     * 审核
     *
     * @param id
     * @return
     */
    @PutMapping("/audit/{id}")
    public Result audit(@PathVariable String id) {
        spuService.audit(id);
        return new Result(true, StatusCode.OK, "审核成功！");
    }

    /**
     * 下架
     *
     * @param id
     * @return
     */
    @PutMapping("/pull/{id}")
    public Result pull(@PathVariable String id) {
        spuService.pull(id);
        return new Result();
    }

    /**
     * 上架
     * @param id
     * @return
     */
    @PutMapping("/put/{id}")
    public Result put(@PathVariable String id) {
        spuService.put(id);
        return new Result();
    }

}
