package com.changgou.service;


import com.alipay.api.AlipayApiException;
import com.alipay.api.response.AlipayTradeCloseResponse;
import com.alipay.api.response.AlipayTradeQueryResponse;
import org.springframework.ui.Model;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.UnsupportedEncodingException;
import java.util.Map;

public interface AliPayService {

    String sendRequestToAlipay(String outTradeNo,Float totalAmount,String subject,String body) throws AlipayApiException;

    String returnUrlMethod(HttpServletRequest request, HttpSession session, Model model) throws AlipayApiException, UnsupportedEncodingException;

    String returnNotyfyUrl(HttpServletRequest request, HttpSession session, Model model) throws AlipayApiException, UnsupportedEncodingException;

    boolean isTradeQuery(String out_trade_no) throws AlipayApiException;

    AlipayTradeQueryResponse tradeQuery(String out_trade_no) throws AlipayApiException;

    AlipayTradeCloseResponse closRequest(String out_trade_no) throws AlipayApiException;


}
