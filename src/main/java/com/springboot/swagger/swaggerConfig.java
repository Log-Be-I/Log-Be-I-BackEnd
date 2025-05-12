package com.springboot.swagger;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class swaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        Server server = new Server();
        server.setUrl("https://logbe-i.com");

        Info info = new Info()
                .version("v1.0.0") // API 버전 정보
                .title("Log Be I") // API 제목
                .description("Log Be I API 입니다."); // API 설명

        // 이 보안 설정이 swagger 문서상 Authorize 버튼을 만들어줌
        SecurityScheme securityScheme = new SecurityScheme()
                .type(SecurityScheme.Type.HTTP) // HTTP 방식 사용 (Bearer 토큰)
                .scheme("bearer") // 인증 방식은 bearer
                .bearerFormat("JWT") // 토큰 포맷은 JWT
                .in(SecurityScheme.In.HEADER) // 인증 정보는 HTTP 헤더에 포함
                .name("Authorization"); // 헤더의 키 이름은 Authorization

        SecurityRequirement securityRequirement = new SecurityRequirement()
                .addList("BearerAuth"); // 위에서 정의한 SecurityScheme 이름과 일치해야 함

        return new OpenAPI()
                .info(info) // API 기본 정보 설정
                // 위에서 설정한 securityScheme 를 Swagger 문서에 추가
                // .addSecuritySchemes => Swagger 에서 모든 api 요청 헤더에 토큰을 붙여주는 전역 설정
                .components(new Components().addSecuritySchemes("BearerAuth", securityScheme))
                .servers(List.of(server))
                .addSecurityItem(securityRequirement); // 모든 API에 보안 설정 적용
    }
}
