package com.changgou.seckill.service;
import com.changgou.seckill.pojo.SeckillOrder;
import com.github.pagehelper.PageInfo;
import entity.SeckillStatus;

import java.util.List;

public interface SeckillOrderService {

    /***
     * SeckillOrder多条件分页查询
     * @param seckillOrder
     * @param page
     * @param size
     * @return
     */
    PageInfo<SeckillOrder> findPage(SeckillOrder seckillOrder, int page, int size);

    /***
     * SeckillOrder分页查询
     * @param page
     * @param size
     * @return
     */
    PageInfo<SeckillOrder> findPage(int page, int size);

    /***
     * SeckillOrder多条件搜索方法
     * @param seckillOrder
     * @return
     */
    List<SeckillOrder> findList(SeckillOrder seckillOrder);

    /***
     * 删除SeckillOrder
     * @param id
     */
    void delete(Long id);

    /***
     * 修改SeckillOrder数据
     * @param seckillOrder
     */
    void update(SeckillOrder seckillOrder);



    /**
     * 根据ID查询SeckillOrder
     * @param id
     * @return
     */
     SeckillOrder findById(Long id);

    /***
     * 查询所有SeckillOrder
     * @return
     */
    List<SeckillOrder> findAll();

    /***
     * 秒杀下单
     * @param time
     * @param id
     * @param username
     */
    Boolean add(String time, Long id, String username);

    /**
     * 状态查询
     * @param username
     */
    SeckillStatus queryStatus(String username);

    /***
     * 更新订单状态
     * @param out_trade_no
     * @param transaction_id
     * @param username
     */
    void updatePayStatus(String out_trade_no, String transaction_id,String username);

    /**
     * 删除订单
     * @param username
     */
    void deleteOrder(String username);


}
