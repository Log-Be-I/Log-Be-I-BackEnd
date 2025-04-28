package com.springboot.log.service;

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

    private boolean isHandlingError = false;

    // 로그 메시지를 날짜별로 redis 에 저장하는 역할
    public void storeInfoLog(String message, String partName) {
        try{
            // 생성되는 로그의 키값은 당일 날짜로 한정 -> 날짜별로 같은 키값 공유
            String key = "logs:" + LocalDate.now() + "." + partName;  // 날짜 단위로 묶음
            // redis 의 list 자료구조에 접근하여
            // 해당 날짜 key (logs: yyyy-MM-dd)의 리스트 맨 끝에 메시지를 추가 저장함
            // key 는 곧 특정 리스트의 이름, message 는 단지 해당 리스트에 쌓일 뿐
            redisTemplate.opsForList().rightPush(key, message);

        } catch (Exception e) {
            if (!isHandlingError) {
                isHandlingError = true;
                logAndStoreWithError("Redis log save failed: {}", e.getMessage(), partName);
                isHandlingError = false;
            } else {
                log.error("Redis log save failed during error handling: {}", e.getMessage());
            }
        }
    }
    // 콘솔 + Redis 저장 한 번에
    public void logAndStoreWithError(String message, String partName, Object... args) {
        log.info(message, args); // "Redis log save failed: Not found"
        storeInfoLog(String.format(message.replace("{}", "%s"), args), partName);
    }
    public void logAndStore(String message, String partName) {
        log.info(message);
        storeInfoLog(message, partName);
    }

    //  에러용 콘솔 + Redis 저장 한 번에
    public void logErrorAndStore(String message, String partName, Exception e) {
        log.error(message + " - reason: {}", e.getMessage(), e);
        storeInfoLog(String.format(message + " - reason: %s", e.getMessage()), partName);
    }
}
