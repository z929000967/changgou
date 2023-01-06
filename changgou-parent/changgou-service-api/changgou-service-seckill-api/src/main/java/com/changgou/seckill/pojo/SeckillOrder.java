package com.changgou.seckill.pojo;
import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;
import java.lang.Long;
import java.util.Date;
import java.lang.String;

@Table(name="tb_seckill_order")
@Data
public class SeckillOrder implements Serializable{

	@Id
    @Column(name = "id")
	private String id;//主键

    @Column(name = "seckill_id")
	private Long seckillId;//秒杀商品ID

    @Column(name = "money")
	private Float money;//支付金额

    @Column(name = "user_id")
	private String userId;//用户

    @Column(name = "create_time")
	private Date createTime;//创建时间

    @Column(name = "pay_time")
	private Date payTime;//支付时间

    @Column(name = "status")
	private String status;//状态，0未支付，1已支付

    @Column(name = "receiver_address")
	private String receiverAddress;//收货人地址

    @Column(name = "receiver_mobile")
	private String receiverMobile;//收货人电话

    @Column(name = "receiver")
	private String receiver;//收货人

    @Column(name = "transaction_id")
	private String transactionId;//交易流水





}
