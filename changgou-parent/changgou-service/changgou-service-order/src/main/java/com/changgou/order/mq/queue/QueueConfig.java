package com.changgou.order.mq.queue;


import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;


/**
 * 延时队列过程
 */
@Configuration
public class QueueConfig {

    /**
     * 正常队列
     * @return
     */
    @Bean
    public Queue normalQueue(){
        Map<String,Object> map=new HashMap();
        // map.put("x-message-ttl", 5000);//message在该队列queue的存活时间最大为10秒
        map.put("x-dead-letter-exchange", "deadExchange");//x-dead-letter-exchange参数
        //是设置该队列的死信交换器（DLX）
        map.put("x-dead-letter-routing-key","DD");//x-dead-letter-routing-key
        //参数是给这个DLX指定路由键
        return new Queue("normalQueue",true,false,false,map);
    }

    /**
     * 死信队列
     * @return
     */
    @Bean
    public Queue deadQueue(){
        return new Queue("deadQueue",true);
    }

    /**
     * 直连交换机
     * @return
     */
    @Bean
    public DirectExchange normalExchange(){
        return new DirectExchange("normalExchange");
    }

    /**
     * 死信交换机
     * @return
     */
    @Bean
    public DirectExchange deadExchange(){
        return new DirectExchange("deadExchange");
    }

    /**
     * 普通队列与交换机绑定
     * @return
     */
    @Bean
    public Binding binding(){
        return BindingBuilder.bind(normalQueue()).to(normalExchange()).with("CC");
    }
    /**
     * 死信队列与死信机绑定
     * @return
     */
    @Bean
    public Binding deadbinding(){
        return BindingBuilder.bind(deadQueue()).to(deadExchange()).with("DD");
    }


}
