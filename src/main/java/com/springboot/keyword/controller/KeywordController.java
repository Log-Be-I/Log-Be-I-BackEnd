package com.springboot.keyword.controller;

import com.springboot.auth.utils.CustomPrincipal;
import com.springboot.auth.utils.MemberDetails;
import com.springboot.keyword.dto.KeywordPostDto;
import com.springboot.keyword.dto.KeywordResponseDto;
import com.springboot.keyword.entity.Keyword;
import com.springboot.keyword.mapper.KeywordMapper;
import com.springboot.keyword.repository.KeywordRepository;
import com.springboot.keyword.service.KeywordService;
import com.springboot.member.entity.Member;
import com.springboot.member.service.MemberService;
import com.springboot.question.dto.QuestionDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
//@RequestMapping("/keywords")
@Validated
@Tag(name = "Keyword API", description = "Keyword API")
public class KeywordController {

        private final KeywordMapper keywordMapper;
        private final KeywordService keywordService;

    //swagger API - 등록
    @Operation(summary = "keyword 등록", description = "회원이 새로운 keyword를 등록합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "새로운 keyword 등록"),
            @ApiResponse(responseCode = "401", description = "유효한 인증 자격 증명이 없습니다",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"error\": \"Unauthorized\", \"message\": \"Your session has expired. Please log in again to continue.\"}")))
    })

    // 키워드 등록
    @PostMapping("/keywords")
    public ResponseEntity postKeyword(@Valid @RequestBody List<KeywordPostDto> keywordPostDtoList,
                                      @Parameter(hidden = true) @AuthenticationPrincipal CustomPrincipal customPrincipal) {

        // 키워드 생성
        keywordService.createKeyword(keywordMapper.KeywordPostDtoListToKeywordList(keywordPostDtoList), customPrincipal);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    //swagger API - 조회
    @Operation(summary = "keyword 조회", description = "키워드를 조회합니다..")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "keyword 조회",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = KeywordResponseDto.class))),
            @ApiResponse(responseCode = "401", description = "유효한 인증 자격 증명이 없습니다",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"error\": \"Unauthorized\", \"message\": \"Your session has expired. Please log in again to continue.\"}")))
    })
    // 키워드 조회
    @GetMapping("/keywords")
    public ResponseEntity getKeyword(@Parameter(hidden = true) @AuthenticationPrincipal CustomPrincipal customPrincipal) {

        List<Keyword> keywordList = keywordService.getKeywords(customPrincipal);

        return new ResponseEntity<>(keywordMapper.keywordListToKeywordResponseDtoList(keywordList), HttpStatus.OK);
    }
}
