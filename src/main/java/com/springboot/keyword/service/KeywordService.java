package com.springboot.keyword.service;

import com.springboot.auth.utils.CustomPrincipal;
import com.springboot.auth.utils.MemberDetails;
import com.springboot.keyword.entity.Keyword;
import com.springboot.keyword.repository.KeywordRepository;
import com.springboot.member.entity.Member;
import com.springboot.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class KeywordService {

    private final KeywordRepository keywordRepository;
    private final MemberService memberService;

    // keyword 생성
    public void createKeyword (List<Keyword> keywordList, CustomPrincipal customPrincipal) {
        //member 찾기
        Member member = memberService.validateExistingMember(customPrincipal.getMemberId());
        // memberId 로 기존 키워드 리스트 찾기
        List<Keyword> keywords = keywordRepository.findAllByMember_MemberId(member.getMemberId());

        // 새로운 키워드 리스트가 비어있다면 (기존 데이터 삭제 상태로 변경해달라는거임)
        // 기존 키워드 리스트에 값이 들어있다면 ( 기존 데이터 삭제 상태로 변경 )
        if(keywordList.isEmpty() || !keywords.isEmpty()){
            // 기존 키워드 상태 전부 "삭제 상태"로 변경
            keywords.stream().forEach(keyword -> {
                keyword.setKeywordStatus(Keyword.KeywordStatus.KEYWORD_DELETED);
                keywordRepository.save(keyword);
            });
        }

        // 키워드 객체 및 리스트 생성
        List<Keyword> newKeywordList = keywordList.stream()
                .map(keyword -> new Keyword(keyword.getName(), member)).collect(Collectors.toList());

        // 키워드 하나 하나 저장하기
        keywordList.stream().forEach(
                keyword -> keywordRepository.save(keyword)
        );
    }

    // keyword 조회
    public List<Keyword> getKeywords (CustomPrincipal customPrincipal) {
        //member 찾기
        Member member = memberService.validateExistingMember(customPrincipal.getMemberId());

        // 키워드 찾기
        List<Keyword> keywords = keywordRepository.findAllByMember_MemberId(member.getMemberId())
                .stream().filter(keyword -> keyword.getKeywordStatus() == Keyword.KeywordStatus.KEYWORD_REGISTERED)
                .collect(Collectors.toList());
        return keywords;
    }
}
