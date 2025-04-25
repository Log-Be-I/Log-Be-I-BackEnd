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

    // redisTemplate DI
    private final RedisTemplate<String, String> redisTemplate;

    // 로그 메시지를 날짜별로 redis 에 저장하는 역할
    public void storeInfoLog(String message) {
        try{
            // 생성되는 로그의 키값은 당일 날짜로 한정 -> 날짜별로 같은 키값 공유
            String key = "logs:" + LocalDate.now();  // 날짜 단위로 묶음
            // redis 의 list 자료구조에 접근하여
            // 해당 날짜 key (logs: yyyy-MM-dd)의 리스트 맨 끝에 메시지를 추가 저장함
            // key 는 곧 특정 리스트의 이름, message 는 단지 해당 리스트에 쌓일 뿐
            redisTemplate.opsForList().rightPush(key, message);
        } catch (Exception e) {
            logAndStoreWithError("Redis log save failed: {}", e.getMessage());
        }

    }
    // ✔️ 콘솔 + Redis 저장 한 번에
    public void logAndStoreWithError(String message, String eMessage) {
        log.info(message);
        storeInfoLog(message);
    }

    public void logAndStore(String message) {
        log.info(message);
        storeInfoLog(message);
    }
}
