package com.lagou.system.util;

import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Date;

public class JwtUtil {
    /**
     * 有效期一个月
     */
    public static final Long JWT_TTL = 1000L * 60 * 60 * 24 * 30;
    // 密钥明文
    private static final String JWT_KEY = "lagou";

    /**
     * 生成令牌
     * @param id 令牌唯一id
     * @param subject 主题
     * @param ttlMills 有效期
     * @return
     */
    public static String createJWT(String id, String subject, Long ttlMills) {
        if (ttlMills == null) {
            ttlMills = JWT_TTL;
        }
        Date expDate = new Date(System.currentTimeMillis() + ttlMills);
        // 获得密钥的密文
        SecretKey secretKey = generalKey();
        JwtBuilder jwtBuilder = Jwts.builder().setId(id)
                .setSubject(subject)
                .setIssuer("admin")
                .setIssuedAt(new Date())
                .setExpiration(expDate)
                .signWith(SignatureAlgorithm.HS256, secretKey);
        String compact = jwtBuilder.compact();
        return compact;
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

    public static void main(String[] args) {
        System.out.println(JwtUtil.createJWT("11", "lagou", null));
    }

}
