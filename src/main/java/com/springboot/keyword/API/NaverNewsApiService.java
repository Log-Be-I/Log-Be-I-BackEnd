package com.springboot.keyword.API;

import com.google.gson.*;
import com.springboot.log.service.LogStorageService;
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
        // Gson 은 inputStream 을 못읽음 정확히는 문자열만 읽을 수 있음 그래서 일단 문자열로 빼두는거임
        String responseBody = response.body().string();
        // 받은 문자열 응답 본문을 GSON 은 JsonParser 를 이용해서 다시 JSON 으로 변경함
        // 다시 이렇게 변경하는 이유는 날것의 문자열 형태는 어떠한 데이터 형태로의 전환도 불가능하기에 JSON으로 한번더 변경하는것이다.
        JsonObject jsonObject = JsonParser.parseString(responseBody).getAsJsonObject(); // JSON 파싱

        // 뉴스 항목 리스트 추출
        // items 라는 Key 를 가진 JSON 배열 = items (배열은 배열인데 items의 벨류값인 jsonList이다.
        JsonArray items = jsonObject.getAsJsonArray("items");
        // 결과값을 받을 map 형태를 받는 List 선언
        List<Map<String, String>> result = new ArrayList<>();

        // JSON 요소 item 하나로 Json List 를 순회
        for (JsonElement item : items) {
            // json 객체 하나 생성
            JsonObject obj = item.getAsJsonObject();
            // news 를 담을 키벨류 형태 자료구조 생성
            Map<String, String> newsItem = new HashMap<>();

            // 뉴스 제목, 설명(html 태그 제거), 링크, 발행일 추출
            // 하나의 아이템에 아래와 같은 하나의 뉴스 정보들을 키벨류 형태로 담음
            newsItem.put("title", cleanText(obj.get("title").getAsString()));
            newsItem.put("description", cleanText(obj.get("description").getAsString()));
            newsItem.put("link", obj.get("link").getAsString());
            newsItem.put("pubDate", obj.get("pubDate").getAsString());
            // 최종적으로 분리되어있던 하나의 뉴스 데이터들을 하나로 모아서 1개의 Map 형태로 만듬 그걸 최종 result에 add함
            result.add(newsItem);

            // 조회한 뉴스 링크와 memberId를 로그로 저장
            logStorageService.logAndStoreWithError("memberId: {} link: {}", String.valueOf(memberId), obj.get("link"), logName);
        }

        // 지금까진 JSON 형태의 자료 형태로 이젠 최종 데이터로 문자열로 변환해주는거임
        // ✅ 결과 리스트를 JSON 문자열로 변환하여 반환
        return new Gson().toJson(result);
    }
}