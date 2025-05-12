package com.springboot.pushToken.repository;

import com.springboot.pushToken.entity.PushToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PushTokenRepository extends JpaRepository<PushToken, Long> {
    Optional<PushToken> findByToken(String token);
    List<PushToken> findByMemberIdAndIsActiveTrue(Long memberId);
    void deleteByToken(String token);
}
