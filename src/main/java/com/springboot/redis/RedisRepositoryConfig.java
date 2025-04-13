package com.springboot.redis;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
//@EnableRedisRepositories
public class RedisRepositoryConfig {

    // application.yml 에서 redis 호스트와 포트 정보를 가져와야한다.
    @Value("${spring.data.redis.host}")
    private String host;
    @Value("${spring.data.redis.port}")
    private int port;

    @Bean
    // Spring Data Redis 에서 Redis 서버와 연결을 생성하고 관리하는 인터페이스
    // ( Redis 연결 생성, RedisConnection 제공, 트랜잭션 지원 )
    public RedisConnectionFactory redisConnectionFactory() {
        // redis 서버에 대한 설정 정보를 담는 객체 생성
        RedisStandaloneConfiguration redisStandaloneConfiguration = new RedisStandaloneConfiguration();
        // 설정 정보의 hostName 에 yml 에 있는 redis 호스트 정보 셋팅
        redisStandaloneConfiguration.setHostName(host);
        // 설정 정보의 port 에 yml 에 있는 redis 포트 번호 셋팅
        redisStandaloneConfiguration.setPort(port);

        // Redis 서버와의 연결을 관리하는 ConnectionFactory 의 구현체
        // 해당 구현체에 redis 설정 정보를 담아서 반환
        LettuceConnectionFactory lettuceConnectionFactory = new LettuceConnectionFactory(redisStandaloneConfiguration);
        return lettuceConnectionFactory;
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate() {
        // redisTemplate 객체 생성
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        // redis 연결을 위한 Connection Factory 설정
        redisTemplate.setConnectionFactory(redisConnectionFactory());
        // redis 의 key 를 직렬화할 때 String 으로 변환
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        // redis 의 value 를 직렬화할 때 String 으로 변환
        redisTemplate.setValueSerializer(new StringRedisSerializer());

        // 설정이 적용된 redisTemplate 반환
        return redisTemplate;
    }
}
