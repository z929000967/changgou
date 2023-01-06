package com.changgou.seckill.controller;
import com.changgou.seckill.pojo.SeckillOrder;
import com.changgou.seckill.service.SeckillOrderService;
import com.github.pagehelper.PageInfo;
import entity.Result;
import entity.SeckillStatus;
import entity.StatusCode;

import entity.TokenDecode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/seckillOrder")
@CrossOrigin
@ResponseBody
public class SeckillOrderController {

    @Autowired
    private SeckillOrderService seckillOrderService;

    @Autowired
    private TokenDecode tokenDecode;

    /***
     * 添加秒杀订单
     * @param time
     * @param id
     * 用户的登录名
     */
    @RequestMapping(value = "/add")
    public Result add(String time,Long id){

        Map<String, String> userInfo = tokenDecode.getUserInfo();
        String username = userInfo.get("username");

        // String username = "linxiaohei";

        //2.调用service的方法创建订单
        boolean flag = seckillOrderService.add(time,id,username);
        if(flag){
            return new Result(true,StatusCode.OK,"正在排队中.....");
        }

        return new Result(false,StatusCode.ERROR,"抢单失败");
    }

    /**
     * 抢单状态查询
     */
    @GetMapping(value = "/query")
    public Result queryStatus(){
        Map<String, String> userInfo = tokenDecode.getUserInfo();
        String username = userInfo.get("username");
        // String username = "linxiaohei";
        SeckillStatus seckillStatus = seckillOrderService.queryStatus(username);

        //查询成功
        if (seckillStatus!=null){
            return new Result(true,StatusCode.OK,"查询状态成功!",seckillStatus);
        }

        return new Result(false,StatusCode.NOTFOUNDERROR,"抢单失败!");
    }

}
