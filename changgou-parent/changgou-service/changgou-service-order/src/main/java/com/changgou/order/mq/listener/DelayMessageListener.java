package com.changgou.order.mq.listener;

import com.alibaba.fastjson.JSON;
import com.changgou.order.service.OrderService;
import com.changgou.service.AliPayService;
import entity.SeckillStatus;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Date;

@Component
@RabbitListener(queues = "deadQueue")
public class DelayMessageListener {

    @Autowired
    private OrderService orderService;

    @Autowired
    private AliPayService aliPayService;
    /**
     * 延时队列监听
     * @param message
     */
    // @RabbitHandler
    // public void getDelayMessage(String message){
    //     SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    //     System.out.println("监听消息的时间"+simpleDateFormat.format(new Date()));
    //     System.out.println("监听到的消息"+message);
    // }
    @RabbitHandler
    public void process(String message){
        try {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            System.out.println("监听消息的时间"+simpleDateFormat.format(new Date()));
            if (message!=null){
                //关闭支付宝支付
                aliPayService.closRequest(message);
                //删除订单
                orderService.deleteOrder(message);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }



    }

}
