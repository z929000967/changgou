package com.changgou.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@Controller
@RequestMapping(value = "/seckillweb")
public class SeckillController {

    @GetMapping(value = "/seckill")
    public String seckillhtml(Model model) throws Exception {

        return "seckill-index";
    }

    @GetMapping(value = "/seckillitem")
    public String seckillitem(Long id,String time) throws Exception {

        return "seckill-item";
    }
}
