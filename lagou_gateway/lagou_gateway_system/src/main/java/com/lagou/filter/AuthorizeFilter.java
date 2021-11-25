package com.lagou.filter;

import com.lagou.util.JwtUtil;
import org.apache.commons.lang.StringUtils;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;


@Component
public class AuthorizeFilter implements GlobalFilter, Ordered {

    private static final String AUTHORIZE_TOKEN = "Authorization";

    /**
     * 请求是会将令牌放入请求头中
     * @param exchange
     * @param chain
     * @return
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();
        // 如果用户访问的是登录就放行
        if (request.getURI().getPath().contains("/admin/login") ||
            request.getURI().getPath().contains("/user/login")) {
            return chain.filter(exchange);
        }
        // 获取请求头中的令牌
        String token = request.getHeaders().getFirst(AUTHORIZE_TOKEN);
        // 如果没有携带token 放错误提示
        if (StringUtils.isBlank(token)) {
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return response.setComplete();
        }
        // 解析失败，错误提示
//        try {
//            JwtUtil.parseJWT(token);
//        } catch (Exception e) {
//            e.printStackTrace();
//            response.setStatusCode(HttpStatus.UNAUTHORIZED);
//            return response.setComplete();
//        }
        // 没有问题就放行
        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
