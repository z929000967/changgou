package com.changgou.goods.dao;

import com.changgou.goods.pojo.Brand;
import tk.mybatis.mapper.common.Mapper;


/**
 * Mapper<Brand>  指定通用的mapper的父接口:封装了所有的CRUD的操作
 * T  指定操作的表对应的POJO
 */
public interface BrandMapper extends Mapper<Brand> {
}
