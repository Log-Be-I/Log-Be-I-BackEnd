package com.springboot.redis;

import com.springboot.auth.jwt.JwtTokenizer;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.concurrent.TimeUnit;

@Service
public class RedisService {
    private final RedisTemplate<String, Object> redisTemplate;
    private final JwtTokenizer jwtTokenizer;

    public RedisService(RedisTemplate<String, Object> redisTemplate, JwtTokenizer jwtTokenizer) {
        this.redisTemplate = redisTemplate;
        this.jwtTokenizer = jwtTokenizer;
    }


     // 로그아웃 처리 - 토큰을 Redis 블랙리스트에 추가
    public boolean logout(String username) {
        jwtTokenizer.deleteRegisterToken("ACCESS_TOKEN:" + username);
        return jwtTokenizer.deleteRegisterToken(username);
    }

    public String getAccessToken(String email) {
        // 예: "ACCESS_TOKEN:{email}" 같은 key에 저장되어 있다고 가정
        String key = "ACCESS_TOKEN:" + email;
        Object tokenObj = redisTemplate.opsForValue().get(key);
        return tokenObj != null ? tokenObj.toString() : null;
    }

    // 구글 redis getAccessToken
    public String getGoogleAccessToken(String email) {
        // 예: "ACCESS_TOKEN:{email}" 같은 key에 저장되어 있다고 가정
        String key = "google:" + email;
        Object tokenObj = redisTemplate.opsForValue().get(key);
        return tokenObj != null ? tokenObj.toString() : null;
    }
}
