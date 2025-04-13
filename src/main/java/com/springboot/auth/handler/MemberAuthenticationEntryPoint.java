package com.springboot.auth.handler;

import com.springboot.auth.utils.ErrorResponder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Slf4j
@Component
public class MemberAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException, ServletException {
        // request 의 속성중 "exception" 이라는 속성을 추출
        // 이때 Object 속성으로 담겨있음으로 Exception 으로 형변환 해줘야함
        Exception exception = (Exception) request.getAttribute("exception");

        // 응답 데이터에 HttpStatus 를 담는 메서드 호출
        ErrorResponder.sendErrorResponse(response, HttpStatus.UNAUTHORIZED);

        // 에러에 따른 에러 메세지 발생
        logExceptionMessage(authException, exception);
    }

    private void logExceptionMessage(AuthenticationException authenticationException, Exception exception) {
        // 예외 객체가 존재한다면 exception 의 메세지를 주고 그렇지 않다면 인증에러를 던져준다
        String message = exception != null ? exception.getMessage() : authenticationException.getMessage();
        log.warn("Unauthorized error happened: {}", message);
    }
}
