package com.changgou.controller;

import entity.Result;
import entity.StatusCode;
import entity.TokenDecode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/checkuser")
@CrossOrigin
public class GetUserNameController {

    @Autowired
    private TokenDecode tokenDecode;

    //获取购物车列表
    @RequestMapping("/check")
    public Result<String> name() {
        Map<String, String> userInfo = tokenDecode.getUserInfo();
        String username = userInfo.get("username");
        return new Result<String>(true, StatusCode.OK, "用户名查询成功", username);


    }

}
