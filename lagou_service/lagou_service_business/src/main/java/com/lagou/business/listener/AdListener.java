package com.lagou.business.listener;


import okhttp3.*;
import okhttp3.Request.Builder;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RabbitListener(queues = "ad_update_queue")
public class AdListener {

    @RabbitHandler
    public void updateAd(String message) {
        System.out.println("接收到消息：" + message);
        String url = "http://192.168.31.128/ad_update?position=" + message;
        OkHttpClient okHttpClient = new OkHttpClient();
        final Request request = new Request.Builder().url(url).build();
        Call call = okHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                // 显示错误信息
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                System.out.println("调用成功====>" + response.message());
            }
        });
    }
}
