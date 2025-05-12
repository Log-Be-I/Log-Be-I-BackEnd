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

    // keyword 생성
    @Transactional
    public List<Keyword> createKeyword (List<Keyword> keywordList, Long memberId) {
        //member 찾기
        Member member = memberService.findVerifiedExistsMember(memberId);
      
        // memberId 로 기존 키워드 리스트 찾기
        List<Keyword> existingKeywords = keywordRepository.findAllByMember_MemberId(member.getMemberId());
        Map<String, Keyword> keywordMap = existingKeywords.stream()
                .collect(Collectors.toMap(keyword -> keyword.getName(), keyword -> keyword ));

        //새로 등록한 키워드들의 이름 중복을 제거
        Set<String> newKeywordNames = keywordList.stream()
                .map(keyword -> keyword.getName())
                .collect(Collectors.toSet());

        //삭제 대상 : 기존에는 있었는데 새 요청에는 없는 경우
        List<Keyword> keywordsToDelete = existingKeywords.stream()
                .filter(keyword -> !newKeywordNames.contains(keyword))
                .collect(Collectors.toList());

        keywordRepository.deleteAll(keywordsToDelete);

        //추가 대상 : 새 요청에는 있는데 기존에는 없는 경우
        List<Keyword> keywordToSave = keywordList.stream()
                .filter(k -> !keywordMap.containsKey(k.getName()))
                .peek(keyword -> keyword.setMember(member))
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
