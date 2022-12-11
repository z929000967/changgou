package com.changgou.seckill.mq.Listener;

import com.alibaba.fastjson.JSON;
import com.changgou.seckill.pojo.SeckillOrder;
import com.changgou.seckill.service.SeckillOrderService;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Component
@RabbitListener(queues = "${mq.pay.queue.seckillorder}")
public class SpeckillMessageListener {

    @Autowired
    private SeckillOrderService seckillOrderService;

    @RabbitHandler
    public void getMessage(Map<String, String> params) {
        //业务结果
        String trade_status = params.get("trade_status");
        //out_trade_no->订单号
        String out_trade_no = params.get("out_trade_no");
        //自定义数据
        String body = params.get("body");
        String jsonDecoded = URLDecoder.decode(body, StandardCharsets.UTF_8);
        Map<String,String> bodyMap = JSON.parseObject(jsonDecoded,Map.class);

        System.out.println(trade_status);
        if (trade_status.equals("TRADE_SUCCESS")) {
            //商户订单
            // String out_trade_no = params.get("out_trade_no");
            if (trade_status.equals("TRADE_SUCCESS")) {
                seckillOrderService.updatePayStatus(params.get("notify_time"),params.get("trade_no"),bodyMap.get("username"));
            }else {
                seckillOrderService.deleteOrder(bodyMap.get("username"));
            }



        }
    }
}