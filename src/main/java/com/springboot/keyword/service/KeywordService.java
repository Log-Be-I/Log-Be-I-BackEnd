package com.springboot.keyword.service;

import com.springboot.auth.utils.MemberDetails;
import com.springboot.keyword.entity.Keyword;
import com.springboot.keyword.repository.KeywordRepository;
import com.springboot.member.entity.Member;
import com.springboot.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class KeywordService {

    private final KeywordRepository keywordRepository;
    private final MemberService memberService;

    // keyword 생성
    public void createKeyword (List<String> keywordStringList, MemberDetails memberDetails) {
        //member 찾기
        Member member = memberService.validateExistingMember(memberDetails.getMemberId());
        // memberId 로 기존 키워드 리스트 찾기
        List<Keyword> keywords = keywordRepository.findAllByMember_MemberId(member.getMemberId());

        // 새로운 키워드 리스트가 비어있다면 (기존 데이터 삭제 상태로 변경해달라는거임)
        if(keywordStringList.isEmpty()){
            // 기존에 있던 키워드 리스트가 비어있지 않다면
            if(!keywords.isEmpty()) {
                // 기존 키워드 상태 전부 "삭제 상태"로 변경
                keywords.stream().forEach(keyword -> {
                    keyword.setKeywordStatus(Keyword.KeywordStatus.KEYWORD_DELETED);
                    keywordRepository.save(keyword);
                });
            }
        } else {
            // 새로운 키워드 리스트에 값이 있다면 ( 기존거 지우고 이걸로 변경해달라는거임 )
            if(!keywords.isEmpty()) {
                // 기존 키워드 상태 전부 "삭제 상태"로 변경
                keywords.stream().forEach(keyword -> {
                    keyword.setKeywordStatus(Keyword.KeywordStatus.KEYWORD_DELETED);
                    keywordRepository.save(keyword);
                });
            }
            // 키워드 객체 및 리스트 생성
            List<Keyword> keywordList = keywordStringList.stream()
                    .map(keyword -> new Keyword(keyword, member)).collect(Collectors.toList());

            // 키워드 하나 하나 저장하기
            keywordList.stream().forEach(
                    keyword -> keywordRepository.save(keyword)
            );
        }

    }

    // keyword 조회
    public List<Keyword> getKeywords (MemberDetails memberDetails) {
        //member 찾기
        Member member = memberService.validateExistingMember(memberDetails.getMemberId());

        // 키워드 찾기
        List<Keyword> keywords = keywordRepository.findAllByMember_MemberId(member.getMemberId())
                .stream().filter(keyword -> keyword.getKeywordStatus() == Keyword.KeywordStatus.KEYWORD_REGISTERED)
                .collect(Collectors.toList());
        return keywords;
    }
}
