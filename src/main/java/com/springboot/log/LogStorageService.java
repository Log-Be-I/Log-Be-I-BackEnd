package com.springboot.log;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Slf4j
@Service
@RequiredArgsConstructor
public class LogStorageService {

    private final RedisTemplate<String, String> redisTemplate;

    // Logback -> Appender 를 커스터마이징해서 로그를 Redis 로 보내는 방식
    public void storeInfoLog(String message) {
        String key = "logs:" + LocalDate.now();  // 날짜 단위로 묶음
        redisTemplate.opsForList().rightPush(key, message);
    }
}
