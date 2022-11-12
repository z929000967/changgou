package com.changgou.order.service;

import com.changgou.order.pojo.OrderItem;

import java.util.List;

public interface CartService {
    /**
     * 添加购物车
     * @param id  sku的ID
     * @param num 购买的数量
     * @param username  购买的商品的用户名
     */
    void add(Long id, Integer num, String username);

    /**
     * 从redis中获取当前的用户的购物车的列表数据
     * @param username
     * @return
     */
    List<OrderItem> list(String username);


}
