package com.lagou.canal.listener;


import com.alibaba.otter.canal.protocol.CanalEntry;
import com.xpand.starter.canal.annotation.CanalEventListener;
import com.xpand.starter.canal.annotation.ListenPoint;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;

@CanalEventListener
public class BusinessListener {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @ListenPoint(schema = "lagou_business", table = {"tb_ad"})
    public void adUpdate(CanalEntry.EntryType entryType, CanalEntry.RowData rowData) {
//        System.out.println("tb_ad表中的数据变化");
//        rowData.getBeforeColumnsList()
//                .forEach(column -> System.out.println("before:" + column.getName() + " : " + column.getValue()));
//        System.out.println("===============================================================================");
//        rowData.getAfterColumnsList()
//                .forEach(column -> System.out.println("after:" + column.getName()+ " : " + column.getValue()));

        System.out.println("tb_ad表中的数据发生了改变==============>");
        for (CanalEntry.Column column : rowData.getAfterColumnsList()) {
            if ("position".equals(column.getName())) {
                System.out.println("发送消息到mq  ad_update_queue========>" + column.getValue());
                // 发送position的值到MQ
                // 将发送到mq，不管客户端有没有消费，只要是监听变动就发送
                rabbitTemplate.convertAndSend("", "ad_update_queue", column.getValue());
                // 只要确定消费者已经接收到了消息，才会发送下一条
                // rabbitTemplate.convertSendAndReceive();
            }
        }
    }
}
