package com.lagou.canal.listener;


import com.alibaba.otter.canal.protocol.CanalEntry;
import com.xpand.starter.canal.annotation.CanalEventListener;
import com.xpand.starter.canal.annotation.ListenPoint;

@CanalEventListener
public class BusinessListener {

    @ListenPoint(schema = "lagou_business", table = {"tb_ad"})
    public void adUpdate(CanalEntry.EntryType entryType, CanalEntry.RowData rowData) {
        System.out.println("tb_ad表中的数据变化");
        rowData.getBeforeColumnsList()
                .forEach(column -> System.out.println("before:" + column.getName() + " : " + column.getValue()));
        System.out.println("===============================================================================");
        rowData.getAfterColumnsList()
                .forEach(column -> System.out.println("after:" + column.getName()+ " : " + column.getValue()));
    }
}
