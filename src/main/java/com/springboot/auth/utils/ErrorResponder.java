package com.springboot.auth.utils;

import com.google.gson.Gson;
import com.springboot.response.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class ErrorResponder {
    // 응답 데이터 response 에 컨텐츠 타입과 상태를 변경해주는 메서드
    // 최종적으로 JSON 형식의 오류 응답을 클라이언트에게 반환해준다
    public static void sendErrorResponse(HttpServletResponse response, HttpStatus status) throws IOException {

        Gson gson = new Gson();
        // HttpStatus 를 ErrorResponse 에 담는다
        ErrorResponse errorResponse = ErrorResponse.of(status);
        // response 의 contentType 을 JSON 으로 설정
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        // response 의 상태를 HttpStatus 의 상태 값으로 설정
        response.setStatus(status.value());
        // response 의 getWriter(HTTP 응답 바디) 에 ErrorResponse 타입의 errorResponse 를 Json 으로 변경하여 담는다
        response.getWriter().write(gson.toJson(errorResponse, ErrorResponse.class));
    }
}
