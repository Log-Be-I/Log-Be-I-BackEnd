package com.springboot.auth.handler;

import com.google.gson.Gson;
import com.springboot.response.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Slf4j
// 인증 실패 핸들러
public class MemberAuthenticationFailureHandler implements AuthenticationFailureHandler {

    @Override
    // 인증 실패시 실행될 메서드
    public void onAuthenticationFailure(HttpServletRequest request,
                                        HttpServletResponse response,
                                        AuthenticationException exception) throws IOException, ServletException {
        // 인증 실패시 해당 로그 출력
        log.error("# Authentication failed: {}", exception.getMessage());
        sendErrorResponse(response);
    }
    // errorResponse 를 Json 으로 변환하여 전달해주는 메서드
    private void sendErrorResponse(HttpServletResponse response) throws IOException {
        Gson gson = new Gson();
        // Http 상태코드( UNAUTHORIZED )를 담은 errorResponse 생성
        ErrorResponse errorResponse = ErrorResponse.of(HttpStatus.UNAUTHORIZED);
        // response 의 content 타입을 클라이언트에게 전달하기 위해 HTTP header 에 추가
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        // response 의 상태가 401 임을 클라이언트에게 전달하기 위해 HTTP Header 에 추가
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        // Gson 을 이용해 ErrorResponse 객체를 JSON 포맷 문자열로 변환후 출력 스트림 생성
        response.getWriter().write(gson.toJson(errorResponse, ErrorResponse.class));
    }
}
