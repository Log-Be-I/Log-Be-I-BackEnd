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
        jwtTokenizer.deleteRegisterToken("google:" + username);
        return jwtTokenizer.deleteRegisterToken(username);
    }
}
