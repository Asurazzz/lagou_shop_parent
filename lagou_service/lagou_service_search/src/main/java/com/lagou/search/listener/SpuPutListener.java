package com.lagou.search.listener;


import com.lagou.search.service.SearchService;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@RabbitListener(queues = "search_add_queue")
public class SpuPutListener {

    @Autowired
    private SearchService searchService;

    @RabbitHandler
    public void addDataToES(String spuId) {
        System.out.println("===接收到需要商品上架的spuId为 ======" + spuId);
        searchService.importDataToES(spuId);
    }
}
