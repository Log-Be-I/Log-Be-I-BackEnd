package com.springboot.keyword.API;

import com.google.gson.*;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;

@Service
public class NaverNewsApiService {

    @Value("${naver.client-id}")
    private String clientId;

    @Value("${naver.secret-key}")
    private String clientSecret;

    private final String apiUrl = "https://openapi.naver.com/v1/search/news.json";

    public String searchNews(String keyword) throws IOException {
        OkHttpClient client = new OkHttpClient();

        // 쿼리 파라미터 구성 (3개만 조회)
        HttpUrl url = HttpUrl.parse(apiUrl).newBuilder()
                .addQueryParameter("query", keyword)
                .addQueryParameter("display", "3")
                .addQueryParameter("sort", "sim")  // 정확도
                .build();

        Request request = new Request.Builder()
                .url(url)
                .get()
                .header("X-Naver-Client-Id", clientId)
                .header("X-Naver-Client-Secret", clientSecret)
                .build();

        Response response = client.newCall(request).execute();
        if (!response.isSuccessful()) {
            throw new IOException("API 요청 실패: " + response);
        }

        // ✅ 응답 JSON 파싱
        String responseBody = response.body().string();
        JsonObject jsonObject = JsonParser.parseString(responseBody).getAsJsonObject();

        JsonArray items = jsonObject.getAsJsonArray("items");
        List<Map<String, String>> result = new ArrayList<>();

        for (JsonElement item : items) {
            JsonObject obj = item.getAsJsonObject();
            Map<String, String> newsItem = new HashMap<>();
            newsItem.put("title", removeHtmlTags(obj.get("title").getAsString()));
            newsItem.put("description", obj.get("description").getAsString());
            newsItem.put("link", obj.get("link").getAsString());
            newsItem.put("pubDate", obj.get("pubDate").getAsString());
            result.add(newsItem);
        }

        return new Gson().toJson(result);
    }

    // ✅ HTML 태그 제거 메서드 추가
    private String removeHtmlTags(String input) {
        return input.replaceAll("<[^>]*>", "");
    }
}
// 조회된 API 주소를 log 찍자.