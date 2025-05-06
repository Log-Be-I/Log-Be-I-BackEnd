package com.springboot.keyword.service;

import com.springboot.auth.utils.CustomPrincipal;
import com.springboot.auth.utils.MemberDetails;
import com.springboot.keyword.entity.Keyword;
import com.springboot.keyword.repository.KeywordRepository;
import com.springboot.member.entity.Member;
import com.springboot.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;
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
        List<Keyword> keywords = keywordRepository.findAllByMember_MemberId(member.getMemberId());

        // 새로운 키워드 리스트가 비어있다면 (기존 데이터 삭제 상태로 변경해달라는거임)
        // 기존 키워드 리스트에 값이 들어있다면 ( 기존 데이터 삭제 상태로 변경 )
        if(keywordList.isEmpty() || !keywords.isEmpty()){
            // 기존 키워드 상태 전부 "삭제 상태"로 변경
            keywords.stream().forEach(keyword -> {
                keywordRepository.delete(keyword);
            });
        }

        keywordList.stream().forEach(keyword -> {
            keyword.setMember(member);
            keywordRepository.save(keyword);
        });
        return keywordList;
    }

    // keyword 조회
    public List<Keyword> findKeywords (Long memberId) {

        Member member = memberService.findVerifiedExistsMember(memberId);

        // 키워드 찾기
        return keywordRepository.findAllByMember_MemberId(member.getMemberId());

    }
}
