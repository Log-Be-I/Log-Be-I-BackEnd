package com.springboot.keyword.controller;

import com.google.gson.Gson;
import com.springboot.auth.utils.CustomPrincipal;
import com.springboot.keyword.API.NaverNewsApiService;
import com.springboot.keyword.dto.KeywordPostDto;
import com.springboot.keyword.dto.KeywordResponseDto;
import com.springboot.keyword.entity.Keyword;
import com.springboot.keyword.mapper.KeywordMapper;
import com.springboot.keyword.service.KeywordService;
import com.springboot.responsedto.ListResponseDto;
import com.springboot.utils.UriCreator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.io.IOException;
import java.net.URI;
import java.util.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping
@Validated
@Tag(name = "Keyword API", description = "Keyword API")
public class KeywordController {

    private final static String KEYWORD_DEFAULT_URL = "/keywords";
        private final KeywordMapper keywordMapper;
        private final KeywordService keywordService;
        private final NaverNewsApiService naverNewsApiService;

    //swagger API - 등록
    @Operation(summary = "keyword 등록", description = "회원이 새로운 keyword를 등록합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "새로운 keyword 등록"),
            @ApiResponse(responseCode = "401", description = "유효한 인증 자격 증명이 없습니다",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"error\": \"Unauthorized\", \"message\": \"Your session has expired. Please log in again to continue.\"}")))
    })

    // 키워드 등록
    @PostMapping
    public ResponseEntity postKeyword(@Valid @RequestBody List<KeywordPostDto> keywordPostDtoList,
                                      @Parameter(hidden = true) @AuthenticationPrincipal CustomPrincipal customPrincipal) {

        List<Keyword> keywordList = keywordService.createKeyword(
                keywordMapper.KeywordPostDtoListToKeywordList(keywordPostDtoList), customPrincipal.getMemberId());
        URI location = UriCreator.createUri(KEYWORD_DEFAULT_URL);
        return ResponseEntity.created(location).body(new ListResponseDto<>(keywordMapper.keywordListToKeywordResponseDtoList(keywordList)));
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
    @GetMapping
    public ResponseEntity getKeywords(@Parameter(hidden = true) @AuthenticationPrincipal CustomPrincipal customPrincipal) throws IOException {

        // 회원이 설정한 키워드 리스트 받아오기
        List<Keyword> keywordList = keywordService.findKeywords(customPrincipal.getMemberId());
        // 앞단에 보내줄 반환값 리스트임 여러개의 키벨류를 리스트로 받음
        List<Map<String, Object>> response = new ArrayList<>();

        // 회원이 설정한 키워드 리스트 for 문으로 돌거야
        for (Keyword keyword : keywordList) {
            // keyword 의 이름과 회원의 id 로
            String newsJson = naverNewsApiService.searchNews(keyword.getName(), customPrincipal.getMemberId());
            log.info("Request{}:{}", customPrincipal.getEmail(), keyword.getName());
            // JSON 문자열을 리스트로 파싱
            // 이젠 JSON으로 변환된 데이터를 GSON이 읽을 수 있음 GSON에 결과 리스트를 넣어서 최종 데이터 폼으로 변환
            List<Map<String, String>> newsList = new Gson().fromJson(newsJson, List.class);

            // map 형태로 키워드와 뉴스 리스트를 한번에 받는 이유는 하나의 데이터셋으로 전부 관리 가능하기 때문이다.
            Map<String, Object> keywordWithNews = new HashMap<>();
            keywordWithNews.put("keyword", keyword.getName());
            keywordWithNews.put("news", newsList);

            response.add(keywordWithNews);
        }
//        System.out.println("최종 Response 데이터: " + response);

        log.info("Response{}", response);
        return new ResponseEntity<>(new ListResponseDto<>(response), HttpStatus.OK);
    }
}
