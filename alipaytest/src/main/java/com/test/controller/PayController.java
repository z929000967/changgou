package com.test.controller;

import com.alibaba.fastjson.JSON;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.domain.AlipayTradeQueryModel;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.response.AlipayTradeQueryResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.AsyncRestTemplate;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

@Controller
public class PayController {



    //appid
    private final String APP_ID = "2021000120615553";
    //应用私钥
    private final String APP_PRIVATE_KEY = "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQCp2OhwdG9129oIqQMrQiZJD2lZ/Oky6C1wj8y34jWav0FmUX5vPGiaQHSSrRw0hs8lhxBCFund+QZlknRy47zim4St81fA8DZUfYTdpTI2rER/No5Gd6okUs+BNTLDkJa/B9fSbmJID+pIuhIgtiN3f7xwcuwLpKRDV5hU6qT/PPQyMRo3o3wiB6YwzdcbdF26U9OrNQ0J00y8aiJEkY+tXTEA68TZ+39hQiTXBb6d8SRGOj6UBjoEP34W+VcsGbWvEvMyIR7a4bV2e4fEnlDy+LF/I3gS3JRG1ooyhMUUmycP4oEMnB88mkmdykPjdxVG0BtwN7pJgeXmK0DSZTRbAgMBAAECggEAf05HmtKLIMny+P7Fk0z3qVdV3c9Fc5S3SmFeTo3NI/oqqOh4XiPnO4cIWVBLjctlxHkh5IeML4aSFtvZUKwGTcvHfSKiKcliz51tUzyZEIW6qadYqNTUwHkZc31OWWsssm7Mg7udCupy7qQZJk+e8djag2aAKpHzVU54rV1a4ruyGfoSCzHM3BD6ZbSTc71i4cNPapYQzaKIegUx5L06y2kN5oREMExuUxZ08ukx1GegtbiliCkjav057XVU7DVhVMFLo4upkGXVfZnhj30+mY53BbeI+eL7M2zPqmq4tI71QoCwAY7M1zxykUkJ/ia48ToMaNUotDRxknAk3RFnkQKBgQDdmxffhewFTgsz5QDYolKq/y+Q73xI9S8dIcCcJPtNq1uhYrUatkTYZXg1XepxQL6QKqQhJYdmnEQnqtkEJno54oE3kXLPlDrw9RQ+Q8kgHrcGZWhF38viaS2+bBIdL+UBitEaLuy2DRQktcnv+/gWjtZWqeMWaNDV3Qh4KhQEDQKBgQDENVRj6SL/jtyHW4N8HFdqS5/p0KIOrge0VqoA+t8F/8a/WOjyY5UjlTuJLKHUEByXL5oZt4FXBwtVCFRipgLOmVOOuIr4Rk+EWszCeeheX6NcQoDS0kRLJRFFT8zvK3mm+gO+uGst/joOnN/HyAPKAnlr30PHMbmG/TaiBf94BwKBgEvNy8LIOs4tviNISJvSGtknZYv8f9UtGaqrawl+lQAih7uS2kSC6NXlD9Ohhpqq/QSvsWktPz3YYm/3PzjuOI0ob+8kL9PV7ruR+hS6d0gneZAUjDCqt0GBnQYwTZnyUjHO3GxKdOppWuSb0rHmzRxaS/NlNarqu9UMQONp8WLZAoGAIVbugAudZs9fqaBjNzOZpvYytgg9gUERDhOrz37RZrOPkfQJoSZKsUEqXKS42s/iC86K4NPNpnwL8Ob5b8nFsFVdXWL+OjG4UUUche8xDNgzafni6e/QqvQheyWULQ+2HClfeYjcrXQFaXY+kDPscOb6utm1KqPIYqekIOPkBQECgYEAnqYiNPgR+eCojwMI79sU5ZQUPnjmIBhYbJaENu73nOss0/5+E+fH7i17IDx8k+xPYD6MT5/bzco06tCM6uedNiWZwne5HCbnM1jB3L+fWVvn/syr8LCOiyy2To5YEJuPz+0mdi7Hrz6ZE8M4pVg+NaG3vunmV6UORWXN1sA7Bkc=";
    private final String CHARSET = "UTF-8";
    // 支付宝公钥
    private final String ALIPAY_PUBLIC_KEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAta1kbgBEzOXtpdIFVslKRkn7JlswQeqjZxVAtI/916yGaANDMgyU6ypI54MKPGOsbtrW4ymhyWxTFhTy28epWTKaTBDMm75+QSk/aSyAjrzwAZrpN4w2hayN6i+tVELJJ89viV1HbOZpcVZbnKKoizT1HPvsaglv+1IrcoSJ5lzyA5Ag1/AE2057kH0Ox19MgxBHU//IFPV1Y4Eh59ulh0RJ0IMF68I3tB40nLx3nSNDjWXxGf9NasSKpUdCsSl2pwboHz243p2WgF2+GW4esK1U5tgowAu0F4iLXoc31tKd0XIlTt1UlalCdy+7D9ZeACak3RsRuEw6Pc31OMcjuQIDAQAB";
    //这是沙箱接口路径,正式路径为https://openapi.alipay.com/gateway.do
    private final String GATEWAY_URL ="https://openapi.alipaydev.com/gateway.do";
    private final String FORMAT = "JSON";
    //签名方式
    private final String SIGN_TYPE = "RSA2";
    //支付宝异步通知路径,付款完毕后会异步调用本项目的方法,必须为公网地址
    private final String NOTIFY_URL = " http://1.12.73.25:5003/GG";
    //支付宝同步通知路径,也就是当付款完毕后跳转本项目的页面,可以不是公网地址
    private final String RETURN_URL = "http://localhost:8080/returnUrl";


    AlipayClient alipayClient = new DefaultAlipayClient(GATEWAY_URL,APP_ID,APP_PRIVATE_KEY,FORMAT,CHARSET,ALIPAY_PUBLIC_KEY,SIGN_TYPE);



    //必须加ResponseBody注解，否则spring会寻找thymeleaf页面
    @ResponseBody
    @RequestMapping("/pay/alipay")
    public String alipay(HttpSession session, Model model,

                         @RequestParam("subject") String subject,
                         @RequestParam("OrderNum") String OrderNum
                         ) throws AlipayApiException {
        //把dona_id项目id 放在session中
        session.setAttribute("subject",subject);
        float dona_money =20f;

        // //生成订单号（支付宝的要求？）
        // String time = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        // String user = UUID.randomUUID().toString().replace("-","").toUpperCase();

        // String OrderNum = time+user;

        //调用封装好的方法（给支付宝接口发送请求）
        return sendRequestToAlipay(OrderNum,dona_money,subject);
    }
    /*
参数1：订单号
参数2：订单金额
参数3：订单名称
 */
    //支付宝官方提供的接口
    private String sendRequestToAlipay(String outTradeNo,Float totalAmount,String subject) throws AlipayApiException {
        //获得初始化的AlipayClient


        //设置请求参数
        AlipayTradePagePayRequest alipayRequest = new AlipayTradePagePayRequest();
        alipayRequest.setReturnUrl(RETURN_URL);
        alipayRequest.setNotifyUrl(NOTIFY_URL);

        //商品描述（可空）
        // String[] body={"ex","gg"};
        String json = "{\"name\":\"John Doe\",\"age\":34}";
        String body  = URLEncoder.encode(json, StandardCharsets.UTF_8);

        alipayRequest.setBizContent("{\"out_trade_no\":\"" + outTradeNo + "\","
                + "\"total_amount\":\"" + totalAmount + "\","
                + "\"subject\":\"" + subject + "\","
                + "\"body\":\"" + body + "\","
                + "\"product_code\":\"FAST_INSTANT_TRADE_PAY\"}");

        //请求
        String result = alipayClient.pageExecute(alipayRequest).getBody();
        return result;
    }

    @RequestMapping("/returnUrl")
    public String returnUrlMethod(HttpServletRequest request, HttpSession session, Model model) throws AlipayApiException, UnsupportedEncodingException {
        System.out.println("=================================同步回调=====================================");

        // 获取支付宝GET过来反馈信息
        Map<String, String> params = new HashMap<String, String>();
        Map<String, String[]> requestParams = request.getParameterMap();
        for (Iterator<String> iter = requestParams.keySet().iterator(); iter.hasNext();) {
            String name = (String) iter.next();
            String[] values = (String[]) requestParams.get(name);
            String valueStr = "";
            for (int i = 0; i < values.length; i++) {
                valueStr = (i == values.length - 1) ? valueStr + values[i] : valueStr + values[i] + ",";
            }
            // 乱码解决，这段代码在出现乱码时使用
            valueStr = new String(valueStr.getBytes("ISO-8859-1"), "utf-8");
            params.put(name, valueStr);
        }

        System.out.println(params);//查看参数都有哪些
        //验证签名（支付宝公钥）
        boolean signVerified = AlipaySignature.rsaCheckV2(params, ALIPAY_PUBLIC_KEY, CHARSET, SIGN_TYPE); // 调用SDK验证签名
        if(!signVerified){
            System.out.println("不懂");
        }
        //验证签名通过
        if(signVerified){
            // 商户订单号
            String out_trade_no = new String(request.getParameter("out_trade_no").getBytes("ISO-8859-1"), "UTF-8");

            // 支付宝交易流水号
            String trade_no = new String(request.getParameter("trade_no").getBytes("ISO-8859-1"), "UTF-8");

            // 付款金额
            float money = Float.parseFloat(new String(request.getParameter("total_amount").getBytes("ISO-8859-1"), "UTF-8"));

            System.out.println("商户订单号="+out_trade_no);
            System.out.println("支付宝交易号="+trade_no);
            System.out.println("付款金额="+money);

            //在这里编写自己的业务代码（对数据库的操作）
			/*
			################################
			*/
            //跳转到提示页面（成功或者失败的提示页面）
            model.addAttribute("flag",1);
            model.addAttribute("msg","支持");
            return "success";
        }else{
            //跳转到提示页面（成功或者失败的提示页面）
            model.addAttribute("flag",0);
            model.addAttribute("msg","支持");
            return "success";
        }
    }

    //异步回调
    @RequestMapping("/GG")
    public String GG(HttpServletRequest request, HttpSession session, Model model) throws AlipayApiException, UnsupportedEncodingException {
        System.out.println("=================================异步回调=====================================");

        // 获取支付宝GET过来反馈信息
        Map<String, String> params = new HashMap<String, String>();
        Map<String, String[]> requestParams = request.getParameterMap();
        for (Iterator<String> iter = requestParams.keySet().iterator(); iter.hasNext();) {
            String name = (String) iter.next();
            String[] values = (String[]) requestParams.get(name);
            String valueStr = "";
            for (int i = 0; i < values.length; i++) {
                valueStr = (i == values.length - 1) ? valueStr + values[i] : valueStr + values[i] + ",";
            }
            // 乱码解决，这段代码在出现乱码时使用
            valueStr = new String(valueStr.getBytes("ISO-8859-1"), "utf-8");
            params.put(name, valueStr);
        }

        System.out.println(params);//查看参数都有哪些
        // System.out.println(new String(request.getParameter("body").getBytes("ISO-8859-1"), "UTF-8"));
        String ex = params.get("body");
        String jsonDecoded = URLDecoder.decode(ex, StandardCharsets.UTF_8);
        System.out.println("===================================="+jsonDecoded);
        //验证签名（支付宝公钥）
        boolean signVerified = AlipaySignature.rsaCheckV1(params, ALIPAY_PUBLIC_KEY, CHARSET, SIGN_TYPE); // 调用SDK验证签名
        if(!signVerified){
            String out_trade_no = new String(request.getParameter("out_trade_no").getBytes("ISO-8859-1"), "UTF-8");
            System.out.println("商户订单号="+out_trade_no);
            System.out.println("不懂");
        }
        //验证签名通过
        if(signVerified){
            // 商户订单号
            String out_trade_no = new String(request.getParameter("out_trade_no").getBytes("ISO-8859-1"), "UTF-8");

            // 支付宝交易流水号
            String trade_no = new String(request.getParameter("trade_no").getBytes("ISO-8859-1"), "UTF-8");

            // 付款金额
            float money = Float.parseFloat(new String(request.getParameter("total_amount").getBytes("ISO-8859-1"), "UTF-8"));

            System.out.println("商户订单号="+out_trade_no);
            System.out.println("支付宝交易号="+trade_no);
            System.out.println("付款金额="+money);

            //在这里编写自己的业务代码（对数据库的操作）
			/*
			################################
			*/
            //跳转到提示页面（成功或者失败的提示页面）
            model.addAttribute("flag",1);
            model.addAttribute("msg","支持");
            return "yibu";
        }else{
            //跳转到提示页面（成功或者失败的提示页面）
            model.addAttribute("flag",0);
            model.addAttribute("msg","支持");
            return "yibu";
        }
    }

    /**
     * 交易查询接口
     * https://doc.open.alipay.com/docs/api.htm?spm=a219a.7395905.0.0.8H2JzG&docType=4&apiId=757
     * @param
     * @return
     * @throws AlipayApiException
     */
    public  boolean isTradeQuery(String out_trade_no) throws AlipayApiException{
        AlipayTradeQueryResponse response = tradeQuery(out_trade_no);
        if(response.isSuccess()){
            return true;
        }
        return false;
    }

    public  AlipayTradeQueryResponse  tradeQuery(String out_trade_no) throws AlipayApiException{
        AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
        request.setBizContent("{\"out_trade_no\":\""+out_trade_no+"\"}");

        return alipayClient.execute(request);
    }

//    必须加ResponseBody注解，否则spring会寻找thymeleaf页面
    @ResponseBody
    @RequestMapping("/pay/check")
    public void checkAlipayPayment(@RequestParam("out_trade_no") String out_trade_no)  throws AlipayApiException {
        if(isTradeQuery(out_trade_no)){
            System.out.println("成功");
        }
        else System.out.println("加油");
    }
}
