package com.lagou.interceptor;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Enumeration;

@Component
public class FeignInterceptor implements RequestInterceptor {
    @Override
    public void apply(RequestTemplate requestTemplate) {
        // 将用户请求对象中所有的请求头放入ReuestTemplate请求头中
        // 获取到用户请求的所有请求头
        ServletRequestAttributes requestAttributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        Enumeration<String> headerNames = requestAttributes.getRequest().getHeaderNames();
        // 放入
        while (headerNames.hasMoreElements()) {
            String key = headerNames.nextElement();
            String value = requestAttributes.getRequest().getHeader(key);
            requestTemplate.header(key, value);
        }
    }
}
