package com.changgou.order.pojo;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;

@Table(name="tb_order_item")
@Data
public class OrderItem implements Serializable{

	@Id
    @Column(name = "id")
	private String id;//ID

    @Column(name = "category_id1")
	private Integer categoryId1;//1级分类

    @Column(name = "category_id2")
	private Integer categoryId2;//2级分类

    @Column(name = "category_id3")
	private Integer categoryId3;//3级分类

    @Column(name = "spu_id")
	private Long spuId;//SPU_ID

    @Column(name = "sku_id")
	private Long skuId;//SKU_ID

    @Column(name = "order_id")
	private String orderId;//订单ID

    @Column(name = "name")
	private String name;//商品名称

    @Column(name = "price")
	private float price;//单价

    @Column(name = "num")
	private Integer num;//数量

    @Column(name = "money")
	private float money;//总金额

    @Column(name = "pay_money")
	private float payMoney;//实付金额

    @Column(name = "image")
	private String image;//图片地址

    @Column(name = "weight")
	private Integer weight;//重量

    @Column(name = "post_fee")
	private Integer postFee;//运费

    @Column(name = "is_return")
	private String isReturn;//是否退货,0:未退货，1：已退货



}
