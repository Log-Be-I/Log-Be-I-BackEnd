package com.springboot.keyword.service;

import com.springboot.keyword.entity.Keyword;
import com.springboot.keyword.repository.KeywordRepository;
import com.springboot.member.entity.Member;
import com.springboot.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class KeywordService {

    private final KeywordRepository keywordRepository;
    private final MemberService memberService;

    @Transactional
    public List<Keyword> createKeyword(List<Keyword> keywordList, Long memberId) {
        Member member = memberService.findVerifiedExistsMember(memberId);

        // 기존 키워드 이름만 추출
        List<Keyword> existingKeywords = keywordRepository.findAllByMember_MemberId(member.getMemberId());
        Set<String> existingNames = existingKeywords.stream()
                .map(Keyword::getName)
                .collect(Collectors.toSet());

        // 새 키워드 이름 중복 제거
        Set<String> newKeywordNames = keywordList.stream()
                .map(Keyword::getName)
                .collect(Collectors.toSet());

        // 삭제 대상
        List<Keyword> keywordsToDelete = existingKeywords.stream()
                .filter(keyword -> !newKeywordNames.contains(keyword.getName()))
                .collect(Collectors.toList());
        keywordRepository.deleteAll(keywordsToDelete);

        // 추가 대상
        List<Keyword> keywordToSave = keywordList.stream()
                .filter(k -> !existingNames.contains(k.getName()))
                .peek(k -> k.setMember(member))
                .collect(Collectors.toList());
        keywordRepository.saveAll(keywordToSave);

        return keywordRepository.findAllByMember_MemberId(member.getMemberId());
    }
    // keyword 조회
    public List<Keyword> findKeywords (Long memberId) {

        Member member = memberService.findVerifiedExistsMember(memberId);

        // 키워드 찾기
        return keywordRepository.findAllByMember_MemberId(member.getMemberId());

    }
}
