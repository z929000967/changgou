package com.changgou.goods.feign;

import com.changgou.goods.pojo.Sku;
import entity.Result;
import entity.StatusCode;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@FeignClient(value="goods")
@RequestMapping("/sku")
public interface SkuFeign {

    /**
     * 商品信息递减
     * Mao<key,value key:要递减的商品id
     *               value：要递减的数量
     */
    @GetMapping(value = "/decr/count")
    Result decrCont(@RequestParam Map<String,Integer> decrmap);

    /**
     * 查询符合条件的状态的SKU的列表
     * @param status
     * @return
     */
    @GetMapping("/status/{status}")
    Result<List<Sku>> findByStatus(@PathVariable(name = "status") String status);


    /**
     * 根据条件搜索的SKU的列表
     * @param sku
     * @return
     */
    @PostMapping(value = "/search" )
    Result<List<Sku>> findList(@RequestBody(required = false) Sku sku);

    @GetMapping("/{id}")
    Result<Sku> findById(@PathVariable(name="id") Long id);



}
