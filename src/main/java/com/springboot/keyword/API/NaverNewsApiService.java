package com.springboot.keyword.API;

import com.google.gson.*;
import com.springboot.log.LogStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.apache.commons.lang3.StringEscapeUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;
@Slf4j
@Service
@RequiredArgsConstructor
public class NaverNewsApiService {

    private final LogStorageService logStorageService;

    @Value("${naver.client-id}")
    private String clientId;

    @Value("${naver.secret-key}")
    private String clientSecret;

    String logName = "news";

    private final String apiUrl = "https://openapi.naver.com/v1/search/news.json";

    private String cleanText(String text) {
        String noHtml = text.replaceAll("<[^>]*>", "");
        return StringEscapeUtils.unescapeHtml4(noHtml);
    }

    public String searchNews(String keyword, Long memberId) throws IOException {
        // HTTP 요청을 보내기 위한 OkHttp 클라이언트 생성
        OkHttpClient client = new OkHttpClient();

        // 쿼리 파라미터 구성 (키워드 검색, 3개 결과, 정확도순 정렬)
        HttpUrl url = HttpUrl.parse(apiUrl).newBuilder()
                .addQueryParameter("query", keyword)
                .addQueryParameter("display", "3")
                .addQueryParameter("sort", "sim")  // 정확도순 정렬
                .build();

        // HTTP GET 요청 생성 (헤더에 클라이언트 ID, 시크릿 포함)
        Request request = new Request.Builder()
                .url(url)
                .get()
                .header("X-Naver-Client-Id", clientId)
                .header("X-Naver-Client-Secret", clientSecret)
                .build();

        // 요청 실행 및 응답 수신
        Response response = client.newCall(request).execute();
        if (!response.isSuccessful()) {
            // 실패 로그찍기
            logStorageService.logAndStoreWithError("API 요청 실패: {}", String.valueOf(response), logName);
            // 실패 시 예외 발생
            throw new IOException("API 요청 실패: " + response);
        }

        // 응답 본문(body)을 문자열로 읽기
        String responseBody = response.body().string();
        JsonObject jsonObject = JsonParser.parseString(responseBody).getAsJsonObject(); // JSON 파싱

        // 뉴스 항목 리스트 추출
        JsonArray items = jsonObject.getAsJsonArray("items");
        List<Map<String, String>> result = new ArrayList<>();

        for (JsonElement item : items) {
            JsonObject obj = item.getAsJsonObject();
            Map<String, String> newsItem = new HashMap<>();

            // 뉴스 제목, 설명(html 태그 제거), 링크, 발행일 추출
            newsItem.put("title", cleanText(obj.get("title").getAsString()));
            newsItem.put("description", cleanText(obj.get("description").getAsString()));
            newsItem.put("link", obj.get("link").getAsString());
            newsItem.put("pubDate", obj.get("pubDate").getAsString());
            result.add(newsItem);

            // 조회한 뉴스 링크와 memberId를 로그로 저장
            logStorageService.logAndStoreWithError("memberId: {} link: {}", String.valueOf(memberId), obj.get("link"), logName);
        }

        // ✅ 결과 리스트를 JSON 문자열로 변환하여 반환
        return new Gson().toJson(result);
    }
}