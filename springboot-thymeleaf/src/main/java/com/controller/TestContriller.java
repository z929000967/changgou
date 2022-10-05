package com.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping(value = "/test")
public class TestContriller {

    @GetMapping(value = "/hello")
    public String hello(Model model){
        model.addAttribute("message","tttsgh");
        return "search";
    }
}
