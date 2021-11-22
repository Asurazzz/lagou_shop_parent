package com.lagou.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Date;

/**
 * @author yemingjie
 */
public class JwtUtil {

    /**
     * 有效期一个月
     */
    public static final Long JWT_TTL = 1000L * 60 * 60 * 24 * 30;
    /**
     * 密钥明文
     */
    private static final String JWT_KEY = "lagou";


    /**
     * 解析token
     * 解析不合法会出现异常，这个时候要抛出
     * @param token
     * @return
     */
    public static Claims parseJWT(String token) throws Exception{
        SecretKey secretKey = generalKey();
        return Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token).getBody();
    }


    /**
     * 生成密钥密文
     * @return
     */
    public static SecretKey generalKey() {
        byte[] bytes = Base64.getEncoder().encode(JwtUtil.JWT_KEY.getBytes(StandardCharsets.UTF_8));
        SecretKey secretKey = new SecretKeySpec(bytes, 0, bytes.length, "AES");
        return secretKey;
    }



}
