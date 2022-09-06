package com.changgou.service;

import java.util.Map;

public interface SkuService {

    void importData();

    /**
     * 条件搜索
     * @param searchMap
     * @return Map
     */
    Map<String,Object> search(Map<String,String> searchMap) throws Exception;
}
