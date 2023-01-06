package com.changgou.item.controller;

import com.alibaba.fastjson.JSON;
import com.changgou.goods.feign.CategoryFeign;
import com.changgou.goods.feign.SkuFeign;
import com.changgou.goods.feign.SpuFeign;
import com.changgou.goods.pojo.Sku;
import com.changgou.goods.pojo.Spu;
import entity.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.thymeleaf.TemplateEngine;

import java.util.Map;

@Controller
@RequestMapping(value = "/itemweb")
public class testController {

    @Autowired
    private SpuFeign spuFeign;

    @Autowired
    private CategoryFeign categoryFeign;

    @Autowired
    private SkuFeign skuFeign;

    @Autowired
    private TemplateEngine templateEngine;


    @GetMapping(value = "/list")
    public String test(@RequestParam(required = false) Map<String,String> searchMap, Model model) throws Exception {

        return "/items/1148473786484400128";
    }
    @GetMapping(value = "/hello")
    public String hello(@RequestParam(value = "skuid",required = false) String skuid,Model model){
        //获取spu 和SKU列表
        Result<Sku> SkuResult = skuFeign.findById(Long.parseLong(skuid));
        model.addAttribute("skuList",SkuResult.getData());
        Long spuid = SkuResult.getData().getSpuId();
        Result<Spu> SpuResult = spuFeign.findById(spuid);
        model.addAttribute("spulist",SpuResult.getData());
        Sku sku = SkuResult.getData();
        if(sku.getImages()!=null) {
            model.addAttribute("skuimageList", sku.getImages().split(","));
        }
        Spu spu = SpuResult.getData();
        if(spu.getImages()!=null) {
            model.addAttribute("spuimageList", spu.getImages().split(","));
        }
        model.addAttribute("message","你好小超");
        model.addAttribute("specificationList", JSON.parseObject(sku.getSn(),Map.class));
        model.addAttribute("skuificationList", JSON.parse(sku.getSpec()));
        // Map<String,String> specificationList = JSON.parseObject(sku.getSn(),Map.class);
        return "test";
    }
}
