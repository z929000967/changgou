package com.changgou.controller;

import com.alibaba.fastjson.JSON;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.changgou.service.AliPayService;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

@RestController
@CrossOrigin
public class PayController {


    @Autowired
    private AliPayService aliPayService;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    //必须加ResponseBody注解，否则spring会寻找thymeleaf页面

    /**
     * 普通订单：
     *          exchange:exchange.order
     *          routingkey:queue.order
     * 秒杀订单：
     *          exchange:exchange.seckillorder
     *          routingkey:queue.seckillorder
     * exchange+routingkey->subject
     * @param session
     * @param model
     * @param dona_money
     * @param OrderNum
     * @return
     * @throws AlipayApiException
     */
    @ResponseBody
    @RequestMapping("/pay/alipay")
    public String alipay(HttpSession session, Model model,
                         @RequestParam("dona_money") float dona_money,
                         // @RequestParam("subject") String subject,
                         @RequestParam("exchange") String exchange,
                         @RequestParam("routingkey") String routingkey,
                         @RequestParam("username") String username,
                         @RequestParam("OrderNum") String OrderNum
                         ) throws AlipayApiException {
        Map<String,String> subjectMap = new HashMap<String, String>();
        subjectMap.put("exchange",exchange);
        subjectMap.put("routingkey",routingkey);
        subjectMap.put("username",username);
        String json = JSON.toJSONString(subjectMap);
        String body  = URLEncoder.encode(json, StandardCharsets.UTF_8);

        // String body = "秒杀订单";

        //把dona_id项目id 放在session中
        // session.setAttribute("subject",subject);

        //生成订单号（支付宝的要求？）
        // String time = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        // String user = UUID.randomUUID().toString().replace("-","").toUpperCase();
        //
        // String OrderNum = time+user;

        //调用封装好的方法（给支付宝接口发送请求）
        return aliPayService.sendRequestToAlipay(OrderNum,dona_money,"订单号"+OrderNum,body);
    }


    @RequestMapping("/callback/returnUrl")
    public String returnUrlMethod(HttpServletRequest request, HttpSession session, Model model) throws AlipayApiException, UnsupportedEncodingException {
        return  aliPayService.returnUrlMethod(request,session,model);
    }

    //异步回调
    @RequestMapping("/callback/notyfyUrl")
    public String returncallback(HttpServletRequest request, HttpSession session, Model model) throws AlipayApiException, UnsupportedEncodingException {
        // 发生支付结果给MQ
        Map<String, String> params = new HashMap<String, String>();
        Map<String, String[]> requestParams = request.getParameterMap();
        for (Iterator<String> iter = requestParams.keySet().iterator(); iter.hasNext();) {
            String name = (String) iter.next();
            String[] values = (String[]) requestParams.get(name);
            String valueStr = "";
            for (int i = 0; i < values.length; i++) {
                valueStr = (i == values.length - 1) ? valueStr + values[i] : valueStr + values[i] + ",";
                System.out.println(name+"==========="+valueStr);
            }

            // 乱码解决，这段代码在出现乱码时使用
            // valueStr = new String(valueStr.getBytes("ISO-8859-1"), "utf-8");
            params.put(name, valueStr);
        }

        System.out.println("=================================="+requestParams.get("body")[0]);
        //获取自定义参数
        String body = params.get("body");
        String jsonDecoded = URLDecoder.decode(body, StandardCharsets.UTF_8);
        Map<String,String> bodyMap = JSON.parseObject(jsonDecoded,Map.class);
        // System.out.println(queueMap.get("exchange"));
        // System.out.println(queueMap.get("routingkey"));
        // String exchange = "exchange.order";
        // String routingkey = "queue.order";
        //
        // if(queue.equals("seckill")){
        //     exchange = "exchange.seckillorder";
        //     routingkey = "queue.seckillorder";
        // }
        // String paramsStr = params.toString();
        // System.out.println(bodyMap.get("exchange"));
        rabbitTemplate.convertAndSend(bodyMap.get("exchange"),bodyMap.get("routingkey"),params);

        return aliPayService.returnNotyfyUrl(request,session,model);
    }
    //    必须加ResponseBody注解，否则spring会寻找thymeleaf页面
    @ResponseBody
    @RequestMapping("/pay/check")
    public void checkAlipayPayment(@RequestParam("out_trade_no") String out_trade_no)  throws AlipayApiException {

        if(aliPayService.isTradeQuery(out_trade_no)){
            System.out.println("成功");
        }
        else System.out.println("加油");
    }

}
