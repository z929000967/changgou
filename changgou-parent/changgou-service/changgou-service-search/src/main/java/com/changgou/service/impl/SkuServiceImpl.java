package com.changgou.service.impl;

import com.alibaba.fastjson.JSON;
import com.changgou.dao.SkuEsMapper;
import com.changgou.goods.feign.SkuFeign;
import com.changgou.goods.pojo.Sku;
import com.changgou.search.pojo.SkuInfo;
import com.changgou.service.SkuService;
import entity.Result;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.StringTerms;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SkuServiceImpl implements SkuService {

    @Autowired
    private SkuFeign skuFeign;

    @Autowired
    private SkuEsMapper skuEsMapper;

    /**
     *ElasticsearchTemplate：实现索引库的增删改查
     */
    @Autowired
    private ElasticsearchTemplate elasticsearchTemplate;


    /**
     * 导入索引库
     */
    @Override
    public void importData() {
        //Feign调用，查询List<Sku>
        Result<List<Sku>> skuResult = skuFeign.findAll();

        /**
         * 将List<Sku>转成List<SkuInfo>
         *     List<Sku>-> [{skuJSON]->List<SkuInfo>
         */
        List<SkuInfo> skuInfoList = JSON.parseArray(JSON.toJSONString(skuResult.getData()),SkuInfo.class);

        //循环当前SkuInfoList
        for (SkuInfo skuInfo : skuInfoList) {
            //获取spec->Map(String) -> Map类型
            Map<String,Object> specMap = JSON.parseObject(skuInfo.getSpec(),Map.class);
            //如果需要生成动态域，只需要将该域存入到一个Map<String,Object>对象中即可，该Map<String,Object>的key会生成一个域，域的名字为该Map的key
            //当前Map<String,Object>后面的Object的值会作为当前Sku对象该域(key)对应的值
            skuInfo.setSpecMap(specMap);
        }

        //调用Dao实现数据批量导入
        skuEsMapper.saveAll(skuInfoList);
    }

    /**
     * 多条件搜索
     * @param searchMap
     * @return Map
     */
    @Override
    public Map<String, Object> search(Map<String, String> searchMap) {

        /**
         * 条件搜索封装
         */
        NativeSearchQueryBuilder builder = buildBasicQuery(searchMap);

        //集合搜索
        Map<String, Object> resulMap = searchList(builder);


        //分类分组查询
        List<String> categoryList = SearchCategoryList(builder);
        resulMap.put("categoryList",categoryList);

        return resulMap;
    }

    private NativeSearchQueryBuilder buildBasicQuery(Map<String, String> searchMap) {
        //NativeSearchQueryBuilder：搜索条件构建对象，用于i封装各种搜索条件
        NativeSearchQueryBuilder builder = new NativeSearchQueryBuilder();

        if (searchMap!=null && searchMap.size()>0){
            //关键词搜索
            String keywords = searchMap.get("keywords");
            //如果关键词不为空，则搜索关键词数据
            builder.withQuery(QueryBuilders.queryStringQuery(keywords).field("name"));

        }
        return builder;
    }

    private Map<String, Object> searchList(NativeSearchQueryBuilder builder) {
        /**
         * 执行搜索，响应结果
         * 1)搜索条件封装对象
         * 2）搜索的结果集（集合数据）需要转换的类型
         * 3)AggregatedPage<SkuInfo>：搜索结果集的封装
         */
        AggregatedPage<SkuInfo> page = elasticsearchTemplate.queryForPage(builder.build(),SkuInfo.class);


        //分析数据-总记录数
        long totaElements = page.getTotalElements();

        //总页数
        int totaPages = page.getTotalPages();


        //获取数据结果集
        List<SkuInfo> contents = page.getContent();

        //封装一个Map存储所有数据，并返回
        Map<String,Object> resulMap = new HashMap<String, Object>();
        resulMap.put("rows",contents);
        resulMap.put("total",totaElements);
        resulMap.put("totaPages",totaPages);
        return resulMap;
    }

    /***
     * 分类分组查询
     * @param builder
     * @return
     */
    private List<String> SearchCategoryList(NativeSearchQueryBuilder builder) {
        /**
         * 分组查询分类集合
         * addAggregation()添加一个聚合操作
         * 1)取别名
         * 2）表示根据哪个域进行分组查询
         */
        builder.addAggregation(AggregationBuilders.terms("categoryName").field("categoryName"));
        AggregatedPage<SkuInfo> aggregatedPage = elasticsearchTemplate.queryForPage(builder.build(), SkuInfo.class);

        /***
         * 获取分组数据
         * aggregatedPage.getAggregations():获取的是集合，可以根据多个域进行搜索
         * .get("skuCategory")：获取指定域的集合数
         */
        StringTerms stringTerms = aggregatedPage.getAggregations().get("skuCategory");
        List<String> categoryList = new ArrayList<String>();
        for (StringTerms.Bucket bucket : stringTerms.getBuckets()) {
            String categorName = bucket.getKeyAsString();//其中一个分类的名字
            categoryList.add(categorName);
        }
        return categoryList;
    }
}
