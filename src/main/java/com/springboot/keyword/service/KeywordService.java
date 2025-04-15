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

        List<Keyword> keywordList = keywordStringList.stream()
                .map(keyword -> new Keyword(keyword, member)).collect(Collectors.toList());

        keywordList.stream().forEach(
                keyword -> keywordRepository.save(keyword)
        );
    }

    // keyword 조회
    public List<Keyword> getKeywords (MemberDetails memberDetails) {
        //member 찾기
        Member member = memberService.validateExistingMember(memberDetails.getMemberId());

        // 키워드 찾기
        List<Keyword> keywords = keywordRepository.findAllByMember_MemberId(member.getMemberId());
        return keywords;
    }
}
