package com.logbei.be.keyword.service;

<<<<<<< HEAD:src/main/java/com/springboot/keyword/service/KeywordService.java
import com.springboot.keyword.entity.Keyword;
import com.springboot.keyword.repository.KeywordRepository;
import com.springboot.member.entity.Member;
import com.springboot.member.service.MemberService;
=======
import com.logbei.be.auth.utils.CustomPrincipal;
import com.logbei.be.keyword.entity.Keyword;
import com.logbei.be.keyword.repository.KeywordRepository;
import com.logbei.be.member.entity.Member;
import com.logbei.be.member.service.MemberService;
>>>>>>> 3cfffea (패키지명 변경):src/main/java/com/logbei/be/keyword/service/KeywordService.java
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;

@RequiredArgsConstructor
@Service
public class KeywordService {

    private final KeywordRepository keywordRepository;
    private final MemberService memberService;

    @Transactional
    public List<Keyword> createKeyword(List<Keyword> keywordList, Long memberId) {
        Member member = memberService.findVerifiedExistsMember(memberId);
        // 1. 기존 키워드 전부 삭제
        List<Keyword> existingKeywords = keywordRepository.findAllByMember_MemberId(member.getMemberId());
        for (Keyword keyword : existingKeywords) {
            keyword.setMember(null); // 연관관계 끊기
        }
        keywordRepository.deleteAll(existingKeywords);

        // 2. 새로운 키워드에 member 설정 후 저장
        List<Keyword> keywordsToSave = keywordList.stream()
                .peek(keyword -> keyword.setMember(member)) //새로 등록할 키워드 member와 연결
                .collect(Collectors.toList());
        keywordRepository.saveAll(keywordsToSave);

        return keywordRepository.findAllByMember_MemberId(member.getMemberId());

    }

    // keyword 조회
    public List<Keyword> findKeywords (Long memberId) {

        Member member = memberService.findVerifiedExistsMember(memberId);

        // 키워드 찾기
        return keywordRepository.findAllByMember_MemberId(member.getMemberId());

    }
}
