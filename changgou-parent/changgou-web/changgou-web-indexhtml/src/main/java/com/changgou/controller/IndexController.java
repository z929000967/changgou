package com.changgou.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping(value = "/web")
public class IndexController {

    @GetMapping(value = "/index")
    public String hello(Model model){
        model.addAttribute("message","你好小超");
        return "index";
    }

}
