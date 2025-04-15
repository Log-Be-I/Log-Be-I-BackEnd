package com.springboot.keyword.controller;

import com.springboot.auth.utils.MemberDetails;
import com.springboot.keyword.dto.KeywordPostDto;
import com.springboot.keyword.dto.KeywordResponseDto;
import com.springboot.keyword.entity.Keyword;
import com.springboot.keyword.mapper.KeywordMapper;
import com.springboot.keyword.repository.KeywordRepository;
import com.springboot.keyword.service.KeywordService;
import com.springboot.member.entity.Member;
import com.springboot.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/keywords")
@Validated
public class KeywordController {

        private final MemberService memberService;
        private final KeywordMapper keywordMapper;
        private final KeywordService keywordService;

    // 키워드 등록
    @PostMapping
    public ResponseEntity postKeyword(@Valid @RequestBody List<String> keywordStringList,
                                      @AuthenticationPrincipal MemberDetails memberDetails) {
        // 키워드 생성
        keywordService.createKeyword(keywordStringList, memberDetails);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    // 키워드 조회
    @GetMapping
    public ResponseEntity getKeyword(@AuthenticationPrincipal MemberDetails memberDetails) {

        List<Keyword> keywordList = keywordService.getKeywords(memberDetails);

        return new ResponseEntity<>(keywordMapper.keywordListToKeywordResponseDtoList(keywordList), HttpStatus.OK);
    }
}
