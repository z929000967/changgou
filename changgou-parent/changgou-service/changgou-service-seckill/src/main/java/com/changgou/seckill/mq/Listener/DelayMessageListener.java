package com.changgou.seckill.mq.Listener;

import com.alibaba.fastjson.JSON;
import com.changgou.seckill.service.SeckillOrderService;
import com.changgou.service.AliPayService;
import entity.SeckillStatus;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Date;

@Component
@RabbitListener(queues = "seckilldeadQueue")
public class DelayMessageListener {

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private SeckillOrderService seckillOrderService;

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
            //获取用户排队信息
            SeckillStatus seckillStatus = JSON.parseObject(message,SeckillStatus.class);

            //如果此时Redis中没有用户排队信息，则表明该订单已处理，如果有用户排队信息，则表示用户尚未完成支付，关闭订单
            Object userQueueStatus = redisTemplate.boundHashOps("UserQueueStatus").get(seckillStatus.getUsername());
            if (userQueueStatus!=null){
                //关闭支付宝支付
                aliPayService.closRequest(seckillStatus.getOrderId().toString());
                //删除订单
                seckillOrderService.deleteOrder(seckillStatus.getUsername());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
