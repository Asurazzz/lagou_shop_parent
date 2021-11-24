package com.lagou.search.service;

public interface SearchService {

    void createIndexAndMapping();

    void importAll();

    void importDataToES(String spuId);
}
