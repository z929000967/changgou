package com.changgou.order.mq.listener;

import com.changgou.order.service.OrderService;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RabbitListener(queues = "${mq.pay.queue.order}")
public class OrderMessageListener {

    @Autowired
    private OrderService orderService;

    @RabbitHandler
    public void getMessage(Map<String, String> params) throws Exception {


        //业务结果
        String trade_status = params.get("trade_status");

        System.out.println(trade_status);
        if(trade_status.equals("TRADE_SUCCESS")){
            //商户订单
            String out_trade_no = params.get("out_trade_no");

            //支付时间
            String gmt_payment = params.get("gmt_payment");

            //交易流水号
            String trade_no = params.get("trade_no");

            orderService.updateStatus(out_trade_no,gmt_payment,trade_no);
        }



    }
}
