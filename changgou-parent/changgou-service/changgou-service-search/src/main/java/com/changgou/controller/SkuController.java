package com.changgou.controller;

import com.changgou.service.SkuService;
import entity.Result;
import entity.StatusCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping(value = "/search")
@CrossOrigin
public class SkuController {

    @Autowired
    private SkuService skuService;

    /**
     * 导入数据
     * @return
     */
    @GetMapping("/import")
    public Result search(){
        skuService.importData();
        return new Result(true, StatusCode.OK,"导入数据到索引库中成功！");
    }
    /**
     * 调用搜索实现
     */
    @GetMapping
    public Map search(@RequestParam(required = false) Map<String,String> searchMap) throws Exception{
        return  skuService.search(searchMap);
    }
}
