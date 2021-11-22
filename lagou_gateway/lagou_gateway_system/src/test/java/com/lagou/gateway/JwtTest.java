package com.lagou.gateway;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.junit.Test;

import java.util.Date;

public class JwtTest {

    @Test
    public void test1() {
        JwtBuilder jwtBuilder = Jwts.builder().setId("9527")
                .setSubject("lagou_shop")
                .setIssuedAt(new Date())
                .signWith(SignatureAlgorithm.HS256, "lagou");
        String token = jwtBuilder.compact();
        System.out.println(token);
    }


    /**
     * 验证令牌
     */
    @Test
    public void test2() {
//        String token = "eyJhbGciOiJIUzI1NiJ9.eyJqdGkiOiI5NTI3Iiwic3ViIjoibGFnb3Vfc2hvcCIsImlhdCI6MTYzNzQ3Njg1MH0.Nw1CX_aAlVZgZKZcpv-McrAdhaYPSiD4BfzcJsMY7sA";
        String token = "eyJhbGciOiJIUzI1NiJ9.eyJqdGkiOiI5NTI3Iiwic3ViIjoibGFnb3Vfc2hvcCIsImlhdCI6MTYzNzQ3NzMwOCwiZXhwIjoxNjQwMDY5MzA3fQ.wFz3UQnddR-IZpu0JIaCO6JznoNPuji45SYFZ6onT4A";
        Claims claims = Jwts.parser().setSigningKey("lagou").parseClaimsJws(token).getBody();
        System.out.println(claims);
    }

    @Test
    public void test3() {
        long time = System.currentTimeMillis() + 1000L * 60 * 60 * 24 * 30;
        Date expirationDate = new Date(time);
        JwtBuilder jwtBuilder = Jwts.builder()
                .setId("9527")
                .setSubject("lagou_shop")
                .setIssuedAt(new Date())
                .setExpiration(expirationDate)
                .signWith(SignatureAlgorithm.HS256, "lagou");
        String compact = jwtBuilder.compact();
        System.out.println(compact);
    }
}
