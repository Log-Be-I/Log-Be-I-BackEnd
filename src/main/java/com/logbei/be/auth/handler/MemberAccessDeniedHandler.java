package com.logbei.be.auth.handler;

import com.logbei.be.auth.utils.ErrorResponder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Slf4j
@Component
// 접근 거부 예외 처리 핸들러
public class MemberAccessDeniedHandler implements AccessDeniedHandler {

    @Override
    public void handle(HttpServletRequest request,
                       HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException, ServletException {
        response.setHeader("Access-Control-Allow-Origin", "https://web.logbe-i.com");
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, PATCH, DELETE, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization");
        response.setHeader("Access-Control-Allow-Credentials", "true");
        // 에러 상태에 따라 에러 메세지 발생
        ErrorResponder.sendErrorResponse(response, HttpStatus.FORBIDDEN);

        // 경고 로그 발생
        log.warn("Forbidden error happened: {}", accessDeniedException.getMessage());
    }
}
