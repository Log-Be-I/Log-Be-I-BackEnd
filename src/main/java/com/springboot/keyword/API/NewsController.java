package com.springboot.keyword.API;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
public class NewsController {

    private final NaverNewsApiService naverNewsApiService;

    public NewsController(NaverNewsApiService naverNewsApiService) {
        this.naverNewsApiService = naverNewsApiService;
    }

    @GetMapping("/search/news")
    public String searchNews(@RequestParam String keyword) throws IOException {
        return naverNewsApiService.searchNews(keyword);
    }
}
