package com.lagou.order.service;

import java.util.Map;

public interface CartService {
    void add(String id, Integer num, String userName);

    Map list(String userName);
}
