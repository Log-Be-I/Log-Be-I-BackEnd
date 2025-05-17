package com.logbei.be.keyword.controller;

import com.google.gson.Gson;

import com.logbei.be.auth.utils.CustomPrincipal;
import com.logbei.be.keyword.API.NaverNewsApiService;
import com.logbei.be.keyword.entity.Keyword;
import com.logbei.be.keyword.service.KeywordService;
import com.logbei.be.responsedto.ListResponseDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class KeywordControllerTest {

    @InjectMocks
    private KeywordController keywordController;

    @Mock
    private KeywordService keywordService;

    @Mock
    private NaverNewsApiService naverNewsApiService;

    @Test
    void getKeywords_success() throws IOException {
        // given
        Long memberId = 1L;
        String keywordName = "인공지능";

        Keyword keyword = new Keyword();
        keyword.setName(keywordName);

        List<Keyword> mockKeywords = List.of(keyword);

        List<Map<String, String>> fakeNewsList = List.of(
                Map.of("title", "뉴스1", "description", "설명1", "link", "https://link1", "pubDate", "2025-05-12")
        );
        String fakeNewsJson = new Gson().toJson(fakeNewsList);

        given(keywordService.findKeywords(memberId)).willReturn(mockKeywords);
        given(naverNewsApiService.searchNews(keywordName, memberId)).willReturn(fakeNewsJson);

        CustomPrincipal principal = new CustomPrincipal("email", memberId);

        // when
        ResponseEntity<?> response = keywordController.getKeywords(principal);

        // then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        Object body = response.getBody();
        assertTrue(body instanceof ListResponseDto);

        ListResponseDto<?> listResponse = (ListResponseDto<?>) body;
        List<?> dataList = listResponse.getData();

        assertEquals(1, dataList.size());
        Map<?, ?> resultItem = (Map<?, ?>) dataList.get(0);

        assertEquals("인공지능", resultItem.get("keyword"));

        List<?> news = (List<?>) resultItem.get("news");
        assertEquals("뉴스1", ((Map<?, ?>) news.get(0)).get("title"));
    }
}