package com.changgou.service.impl;

import com.alibaba.fastjson.JSON;
import com.changgou.dao.SkuEsMapper;
import com.changgou.goods.feign.SkuFeign;
import com.changgou.goods.pojo.Sku;
import com.changgou.search.pojo.SkuInfo;
import com.changgou.service.SkuService;
import entity.Result;
import org.apache.lucene.queryparser.classic.QueryParserBase;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.StringTerms;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.SearchResultMapper;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.aggregation.impl.AggregatedPageImpl;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;

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
    public Map<String, Object> search(Map<String, String> searchMap) throws Exception{

        /**
         * 条件搜索封装
         */
        NativeSearchQueryBuilder builder = buildBasicQuery(searchMap);

        //集合搜索
        Map<String, Object> resultMap = searchList(builder);

//        //当用户选择了分类,将分类作为搜索条件，则不需要对分类进行分组搜索,因为分组搜索的数据是用于显示搜索条件的
//        //分类->searchMap->category
//        if(searchMap == null || StringUtils.isEmpty((searchMap.get("category")))){
//            //分类分组查询
//            List<String> categoryList = SearchCategoryList(builder);
//            resultMap.put("categoryList",categoryList);
//        }
//
//        //当用户选择了分类,将分类作为搜索条件，则不需要对分类进行分组搜索,因为分组搜索的数据是用于显示搜索条件的
//        //品牌->searchMap->brand
//        if(searchMap == null || StringUtils.isEmpty((searchMap.get("brand")))){
//            //品牌分组查询
//            List<String> brandList = SearchBrandList(builder);
//            resultMap.put("brandList",brandList);
//        }
//
//
//        //规格查询
//        Map<String,Set<String>> specList = SearchSpecList(builder);
//        resultMap.put("specList",specList);

        Map<String,Object> groupMap = SearchGroudList(builder,searchMap);
        resultMap.putAll(groupMap);
        return resultMap;
    }

    /***
     * 分组查询->分类分组、品牌分组
     * @param builder
     * @return
     */
    private Map<String,Object> SearchGroudList(NativeSearchQueryBuilder builder,Map<String,String> searchMap) {
        /**
         * 分组查询分类集合
         * addAggregation()添加一个聚合操作
         * 1)取别名
         * 2）表示根据哪个域进行分组查询
         */
        if(searchMap == null || StringUtils.isEmpty((searchMap.get("category")))){
            builder.addAggregation(AggregationBuilders.terms("skuCategory").field("categoryName"));
        }
        if(searchMap == null || StringUtils.isEmpty((searchMap.get("brand")))){
            builder.addAggregation(AggregationBuilders.terms("skubrand").field("brandName"));
        }
        builder.addAggregation(AggregationBuilders.terms("skuSpec").field("spec.keyword"));
        AggregatedPage<SkuInfo> aggregatedPage = elasticsearchTemplate.queryForPage(builder.build(), SkuInfo.class);

        //定义应该Map,存储所有结果
        Map<String,Object> groupMapResult = new HashMap<String, Object>();
        /***
         * 获取分组数据
         * aggregatedPage.getAggregations():获取的是集合，可以根据多个域进行搜索
         * .get("skuCategory")：获取指定域的集合数
         */
        if(searchMap == null || StringUtils.isEmpty((searchMap.get("category")))){
            StringTerms categoryTerms = aggregatedPage.getAggregations().get("skuCategory");
            List<String> categoryList = getGroupList(categoryTerms);
            groupMapResult.put("categoryList",categoryList);
        }
        if(searchMap == null || StringUtils.isEmpty((searchMap.get("brand")))){
            StringTerms brandTerms = aggregatedPage.getAggregations().get("skubrand");
            List<String> brandList = getGroupList(brandTerms);
            groupMapResult.put("brandList",brandList);
        }
        StringTerms specTerms = aggregatedPage.getAggregations().get("skuSpec");

        List<String> specList = getGroupList(specTerms);
        Map<String,Set<String>> specMap = putAllSpec(specList);
        groupMapResult.put("specList",specMap);
        return  groupMapResult;

    }

    /**
     * 获取分组集合数据
     * @param stringTerms
     * @return
     */
    public  List<String> getGroupList(StringTerms stringTerms){
        List<String> groupList = new ArrayList<String>();
        for (StringTerms.Bucket bucket : stringTerms.getBuckets()) {
            String feildName = bucket.getKeyAsString();//其中一个分类的名字
            groupList.add(feildName);
        }
        return groupList;
    }

    private NativeSearchQueryBuilder buildBasicQuery(Map<String, String> searchMap) {
        //NativeSearchQueryBuilder：搜索条件构建对象，用于i封装各种搜索条件
        NativeSearchQueryBuilder builder = new NativeSearchQueryBuilder();

        //BoolQueryBuilder must,must_not,should
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        if (searchMap!=null && searchMap.size()>0){
            //关键词搜索
            String keywords = searchMap.get("keywords");
            //如果关键词不为空，则搜索关键词数据
            if(!StringUtils.isEmpty(keywords)){
                //builder.withQuery(QueryBuilders.queryStringQuery(keywords).field("name"));
                boolQueryBuilder.must(QueryBuilders.queryStringQuery(keywords).field("name"));
            }

            //输入了分类
            if(!StringUtils.isEmpty(searchMap.get("category"))){
                boolQueryBuilder.must(QueryBuilders.termQuery("categoryName",searchMap.get("category")));
            }

            //输入了品牌
            if(!StringUtils.isEmpty(searchMap.get("brand"))){
                boolQueryBuilder.must(QueryBuilders.termQuery("brandName",searchMap.get("brand")));
            }

            //规格过滤实现
            for (Map.Entry<String,String> entry : searchMap.entrySet()) {
                String key = entry.getKey();
                //如果key以spec_开始，则表示规格筛选查询
                if(key.startsWith("spec_")){
                    //规格条件的值
                    String value = entry.getValue();
                    boolQueryBuilder.must(QueryBuilders.termQuery("specMap."+key.substring(5)+".keyword",value));
                }
                
            }
            //价格区间过滤
            String price = searchMap.get("price");
            if(!StringUtils.isEmpty(price)){
                price = price.replace("元","").replace("以上","");
                String[] prices = price.split("-");
                if (price!=null && price.length()>0){
                    if(prices.length >= 1){
                        boolQueryBuilder.must(QueryBuilders.rangeQuery("price").gt(prices[0]));
                    }
                    if(prices.length == 2){
                        boolQueryBuilder.must(QueryBuilders.rangeQuery("price").lte(prices[1]));
                    }
                }
            }

            //排序实现
            String sortField = searchMap.get("sortField");
            String sortRule = searchMap.get("sortRule");
            if(!StringUtils.isEmpty(sortField)&& !StringUtils.isEmpty(sortRule)){
                builder.withSort(new FieldSortBuilder(sortField).order(SortOrder.valueOf(sortRule)));
            }
        }

        //分页，用户如果不传分页参数，则默认第一页
        Integer size = 20;    //查询默认的数据条数
        Integer pageNum = coverterPage(searchMap); //默认第一页
        builder.withPageable(PageRequest.of(pageNum-1,size));

        //将boolQueryBuilder填充给NativeSearchQueryBuilder
        builder.withQuery(boolQueryBuilder);
        return builder;
    }

    /**
     * 接收前端传入的分页参数
     * @param searchMap
     * @return
     */
    public  Integer coverterPage(Map<String,String> searchMap){
        if (searchMap != null){
            String pageNum = searchMap.get("pageNum");
            try {
                return Integer.parseInt(pageNum);
            }
            catch (NumberFormatException e){
            }

        }
        return 1;
    }

    private Map<String, Object> searchList(NativeSearchQueryBuilder builder) {

        //高亮配置
        HighlightBuilder.Field field = new HighlightBuilder.Field("name");//指定高定域
        //前缀
        field.preTags("<em style=\"color:red;\"");
        //后缀
        field.postTags("</em>");
        //碎片长度
        field.fragmentSize(100);
        //添加高亮
        builder.withHighlightFields(field);




        /**
         * 执行搜索，响应结果
         * 1)搜索条件封装对象
         * 2）搜索的结果集（集合数据）需要转换的类型
         * 3)AggregatedPage<SkuInfo>：搜索结果集的封装
         */
        //AggregatedPage<SkuInfo> page = elasticsearchTemplate.queryForPage(builder.build(),SkuInfo.class);
        AggregatedPage<SkuInfo> page = elasticsearchTemplate
                .queryForPage(
                        builder.build(),    //搜索条件封装
                        SkuInfo.class,
                        new SearchResultMapper() {
                            @Override
                            public <T> AggregatedPage<T> mapResults(SearchResponse searchResponse, Class<T> aClass, Pageable pageable) {
                                //存储转换后的高亮数据
                                List<T> list = new ArrayList<T>();
                                //执行查询，获取所有数据->结果集[非高亮数据|高亮数据]
                                for (SearchHit hit : searchResponse.getHits()) {
                                    //分析结果集数据，获取非高亮数据
                                    SkuInfo skuInfo = JSON.parseObject(hit.getSourceAsString(),SkuInfo.class);
                                    //分析结果集数据，获取高亮数据->只有某个域的高亮数据
                                    HighlightField highlightField = hit.getHighlightFields().get("name");

                                    if (highlightField != null && highlightField.getFragments() != null){
                                        //高亮数据读取出来
                                        Text[] framents = highlightField.getFragments();
                                        StringBuffer buffer = new StringBuffer();
                                        for (Text frament : framents) {
                                            buffer.append(frament.toString());
                                        }
                                        //非高亮数据中指定的域替换成高亮数据
                                        skuInfo.setName(buffer.toString());
                                    }
                                    //将高亮数据添加导集合list中
                                    list.add((T) skuInfo);
                                }
                                //将数据返回
                                /**
                                 * 1)搜索的集合的数据:(携带高亮)List<T> content
                                 * 2)分页对象信息
                                 * 3)搜索记录的总条数
                                 */
                                return new AggregatedPageImpl<T>(list,pageable,searchResponse.getHits().getTotalHits());
                            }
                        });

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
        builder.addAggregation(AggregationBuilders.terms("skuCategory").field("categoryName"));
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

    /***
     * 品牌分组查询
     * @param builder
     * @return
     */
    private List<String> SearchBrandList(NativeSearchQueryBuilder builder) {
        /**
         * 分组查询分类集合
         * addAggregation()添加一个聚合操作
         * 1)取别名
         * 2）表示根据哪个域进行分组查询
         */
        builder.addAggregation(AggregationBuilders.terms("skubrand").field("brandName"));
        AggregatedPage<SkuInfo> aggregatedPage = elasticsearchTemplate.queryForPage(builder.build(), SkuInfo.class);

        /***
         * 获取分组数据
         * aggregatedPage.getAggregations():获取的是集合，可以根据多个域进行搜索
         * .get("skubrand")：获取指定域的集合数
         */
        StringTerms stringTerms = aggregatedPage.getAggregations().get("skubrand");
        List<String> brandList = new ArrayList<String>();
        for (StringTerms.Bucket bucket : stringTerms.getBuckets()) {
            String brandName = bucket.getKeyAsString();//其中一个分类的名字
            brandList.add(brandName);
        }
        return brandList;
    }

    /***
     * 规格分组查询
     * @param builder
     * @return
     */
    private Map<String, Set<String>> SearchSpecList(NativeSearchQueryBuilder builder) {
        /**
         * 规格查询分类集合
         * addAggregation()添加一个聚合操作
         * 1)取别名
         * 2）表示根据哪个域进行分组查询
         */
        builder.addAggregation(AggregationBuilders.terms("skuspec").field("spec.keyword").size(10000));
        AggregatedPage<SkuInfo> aggregatedPage = elasticsearchTemplate.queryForPage(builder.build(), SkuInfo.class);

        /***
         * 获取分组数据
         * aggregatedPage.getAggregations():获取的是集合，可以根据多个域进行搜索
         * .get("skubrand")：获取指定域的集合数
         */
        StringTerms stringTerms = aggregatedPage.getAggregations().get("skuspec");
        List<String> specList = new ArrayList<String>();
        for (StringTerms.Bucket bucket : stringTerms.getBuckets()) {
            String specName = bucket.getKeyAsString();//其中一个规格的名字
            specList.add(specName);
        }
        //规格汇总合并
        Map<String, Set<String>> allSpec = putAllSpec(specList);

        return allSpec;



    }

    /**
     * 规格汇总合并
     * @param specList
     * @return
     */
    private Map<String, Set<String>> putAllSpec(List<String> specList) {
        //合并后的Map对象:将每个Map对象合成成一个Map<String,Set<String>>
        Map<String, Set<String>> allSpec = new HashMap<String, Set<String>>();

        //1.循环specList
        for (String spec : specList) {
            //2.将每个JSON字符串转成Map
            Map<String,String> specMap = JSON.parseObject(spec,Map.class);

            //3.合并流程
            //3.1循环所有Map
            for (Map.Entry<String, String> entry : specMap.entrySet()) {
                //3.2取出当前Map，并且获取对应的Key以及对应的value
                String key = entry.getKey();    //规格名字
                String value = entry.getValue();//规格值

                //3.2将当前循环的数据合并到一个Map<String,Set<String>>中
                //从allSpec中获取当前规格对应的Set集合数据
                Set<String> specSet = allSpec.get(key);
                if (specSet == null){
                    specSet = new HashSet<String>();
                }
                specSet.add(value);
                allSpec.put(key,specSet);
            }





        }
        return allSpec;
    }
}
