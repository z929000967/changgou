package com.changgou.canal;

import com.alibaba.otter.canal.protocol.CanalEntry;
import com.changgou.item.feign.PageFeign;
import com.xpand.starter.canal.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@CanalEventListener
public class CanalDataEventListener {
    /***
     * 增加数据监听
     * @param eventType
     * @param rowData
     */
    @InsertListenPoint
    public void onEventInsert(CanalEntry.EventType eventType, CanalEntry.RowData rowData) {
        rowData.getAfterColumnsList().forEach((c) -> System.out.println("By--Annotation: " + c.getName() + " ::   " + c.getValue()));
    }

    /***
     * 修改数据监听
     * @param rowData
     */
    @UpdateListenPoint
    public void onEventUpdate(CanalEntry.RowData rowData) {
        System.out.println("UpdateListenPoint");
        rowData.getAfterColumnsList().forEach((c) -> System.out.println("By--Annotation: " + c.getName() + " ::   " + c.getValue()));
    }

    /***
     * 删除数据监听
     * @param eventType
     */
    @DeleteListenPoint
    public void onEventDelete(CanalEntry.EventType eventType) {
        System.out.println("DeleteListenPoint");
    }

    /***
     * 自定义数据修改监听
     * @param eventType
     * @param rowData
     */
    @ListenPoint(destination = "example", schema = "changgou_content", table = {"tb_content_category", "tb_content"}, eventType = CanalEntry.EventType.UPDATE)
    public void onEventCustomUpdate(CanalEntry.EventType eventType, CanalEntry.RowData rowData) {
        System.err.println("DeleteListenPoint");
        rowData.getAfterColumnsList().forEach((c) -> System.out.println("By--Annotation: " + c.getName() + " ::   " + c.getValue()));
    }
    @Autowired
    private PageFeign pageFeign;

    @ListenPoint(destination = "example",
            schema = "changgou_goods",
            table = {"tb_spu"},
            eventType = {CanalEntry.EventType.UPDATE,CanalEntry.EventType.INSERT,
                    CanalEntry.EventType.DELETE})

    public void onEventCustomSpu(CanalEntry.EventType eventType,CanalEntry.RowData rowData){
        // 判断操作类型
        if(eventType== CanalEntry.EventType.DELETE){
            String spuId="";
            List<CanalEntry.Column> beforeColumnsList = rowData.getBeforeColumnsList();
            for (CanalEntry.Column column : beforeColumnsList) {
                if (column.getName().equals("id")) {
                    spuId = column.getValue();//spuid
                    break;
                }
            }
            //todo 删除静态页

        }else{
            //新增 或者 更新
            List<CanalEntry.Column> afterColumnsList = rowData.getAfterColumnsList();
            String spuId = "";
            for (CanalEntry.Column column : afterColumnsList) {
                if (column.getName().equals("id")) {
                    spuId = column.getValue();
                    break;
                }
            }
            //更新 生成静态页
            pageFeign.createHtml(Long.valueOf(spuId));
        }
    }
}
