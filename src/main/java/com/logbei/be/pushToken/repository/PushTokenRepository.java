package com.logbei.be.pushToken.repository;

import com.logbei.be.pushToken.entity.PushToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PushTokenRepository extends JpaRepository<PushToken, Long> {
    Optional<PushToken> findByTokenAndIsActive(String token, boolean isActive);
    List<PushToken> findByMember_MemberIdAndIsActiveTrue(Long memberId);
    void deleteByToken(String token);
}
