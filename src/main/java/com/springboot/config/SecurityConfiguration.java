package com.springboot.config;

import com.springboot.auth.filter.JwtAuthenticationFilter;
import com.springboot.auth.filter.JwtVerificationFilter;
import com.springboot.auth.handler.*;
import com.springboot.auth.jwt.JwtTokenizer;
import com.springboot.auth.utils.CustomAuthorityUtils;
import com.springboot.auth.utils.MemberDetailService;
import com.springboot.member.repository.MemberRepository;
import com.springboot.oauth.OAuthAuthenticationFilter;
import com.springboot.oauth.OAuthAuthenticationProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;


@Slf4j
@EnableMethodSecurity
@Configuration
public class SecurityConfiguration {
    private final JwtTokenizer jwtTokenizer;
    private final CustomAuthorityUtils authorityUtils;
    private final MemberDetailService memberDetailService;
    private final RedisTemplate<String, Object> redisTemplate;
    private String clientId;
    private final MemberRepository memberRepository;



    public SecurityConfiguration(JwtTokenizer jwtTokenizer, CustomAuthorityUtils authorityUtils,
                                 MemberDetailService memberDetailService, RedisTemplate<String, Object> redisTemplate, MemberRepository memberRepository) {
        this.jwtTokenizer = jwtTokenizer;
        this.authorityUtils = authorityUtils;
        this.memberDetailService = memberDetailService;
        this.redisTemplate = redisTemplate;
        this.memberRepository = memberRepository;
    }

    @Bean
    // SecurityFilterChain = HTTP 요청을 필터링하는 보안 체인
    // HttpSecurity = 보안 설정을 정의하는 객체
    // throws Exception = 보안 설정을 적용하는 과정에서 예외 가능성이 있으므로 선언
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // h2 화면 자체가 내부적으로 <frame> 태그를 사용하고 있어 맞춰 사용함
                .headers().frameOptions().sameOrigin()
                .and()
                // csrf 공격방어 비활성화 ( 로컬환경에서 진행함으로 불필요 & 비활성화 안하면 403에러 발생 )
                .csrf().disable()
                // CORS 설정 추가 withDefaults() 라면 corsConfigurationSource 이름으로 등록된 bean 사용
                .cors(Customizer.withDefaults())
                // 세션을 생성하지 않도록 설정 / 시큐리티 자체에서 세션을 자동으로 생성하기때문에 JWT 를 사용한다면 설정을 잡아줘야한다
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                // 폼로그인 비활성화
                .formLogin().disable()
                // HTTP Basic = 로그인창 팝업을 띄우는 방식
                .httpBasic().disable()
                // 예외 핸들링
                .exceptionHandling()
                // 인증 예외 포인트 설정
                .authenticationEntryPoint(new MemberAuthenticationEntryPoint())
                // 접근 거부 예외 설정
                .accessDeniedHandler(new MemberAccessDeniedHandler())
                .and()
                .apply(new CustomFilterConfigurer())
                .and()
                // 모든 요청에 대해 인증 없이 접근 가능
                // 여러개의 요청에 대한 권한 정의가 가능하다
                .authorizeHttpRequests(authorize -> authorize
                        .antMatchers("/auth/login", "/oauth/login").permitAll()
                        .antMatchers("/logout").permitAll()
                        // 접근 권한과 상관없이 post 요청이라면 허용한다
                        .antMatchers(HttpMethod.POST, "/*/members").permitAll()
                        .antMatchers(HttpMethod.POST, "/*/questions").hasRole("USER")
                        .antMatchers(HttpMethod.POST, "/*/answers").hasRole("ADMIN")
                        // 정보 수정 요청은 USER 권한만 가능하다
                        .antMatchers(HttpMethod.PATCH,"/*/members/**").hasRole("USER")
                        .antMatchers(HttpMethod.PATCH, "/*/questions/**").hasRole("USER")
                        .antMatchers(HttpMethod.PATCH, "/*/answers/**").hasRole("ADMIN")
                        // 회원 전체 조회 요청은 ADMIN 만 가능하다
                        .antMatchers(HttpMethod.GET, "/*/members").hasRole("ADMIN")
                        .antMatchers(HttpMethod.GET, "/*/questions").hasAnyRole("USER", "ADMIN")
                        // 회원 단일 조회 요청은 USER ADMIN 만 가능
                        .antMatchers(HttpMethod.GET,"/*/members/**").hasAnyRole("USER", "ADMIN")
                        .antMatchers(HttpMethod.GET, "/*/questions/**").permitAll()
                        // 회원 삭제 요청은 USER 만 가능
                        .antMatchers(HttpMethod.DELETE, "/*/members/**").hasRole("USER")
                        .antMatchers(HttpMethod.DELETE, "/*/questions/**").hasRole("USER")
                        .antMatchers(HttpMethod.DELETE, "/*/answers/**").hasRole("ADMIN")
                        .antMatchers("/api/auth/google").permitAll()
                        .anyRequest().permitAll()
                )
                // OAuth2 기본 설정
                .exceptionHandling()
                .authenticationEntryPoint(((request, response, authException) -> {
                    log.warn("인증실패: {}", authException.getMessage());
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
                }))
                .and()
                .logout().disable();

        // http 객체를 SecurityFilterChain 으로 변환하여 반환
        return http.build();
    }

    @Bean
    // 비밀번호 암호화 역할을 하는 bean 을 생성하는 메서드
    public PasswordEncoder passwordEncoder() {
        // 자동으로 적절한 암호화 알고리즘을 선택하는 PasswordEncoder 생성
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
        // 구체적인 CORS 정책을 설정
    CorsConfigurationSource corsConfigurationSource() {
        // CORS 설정 정보 담는 객체 생성
        CorsConfiguration configuration = new CorsConfiguration();
        // 스크립트 기반의 HTTP 통신 = 클라이언트측 언어가 브라우저에서 실행되면서 발생하는 HTTP 요청을 의미
        // ex) AJAX 요청, Fetch API 등
        // 모든 출처(Origin)에 대해 스크립트 기반의 HTTP 통신을 허용하도록 설정
//        configuration.setAllowedOrigins(Arrays.asList("*"));
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:3000"));

        // 파라미터로 지정한 HTTP Method 에 대한 HTTP 통신을 허용
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PATCH", "DELETE"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        configuration.addExposedHeader("Authorization");
//        configuration.setExposedHeaders(List.of("Authorization", "RefreshToken"));

        // CORS 정책을 URL 패턴별로 설정하는 클래스, 특정 URL(endPoint) 에 대해 CORS 정책을 다르게 적용할때 사용
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // 위에서 설정한 CORS 정책(configuration)을 특정 URL 경로에 대해서만 적용
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    // JwtAuthenticationFilter 등록
    public class CustomFilterConfigurer extends AbstractHttpConfigurer<CustomFilterConfigurer, HttpSecurity> {

        @Override
        public void configure(HttpSecurity builder) throws Exception {
            AuthenticationManager authenticationManager = builder.getSharedObject(AuthenticationManager.class);
            // 인증 과정에서 RedisTemplate을 사용하기 위해 RedisTemplate 생성자에 전달
            JwtVerificationFilter jwtVerificationFilter = new JwtVerificationFilter(jwtTokenizer, authorityUtils, redisTemplate, memberDetailService);
            JwtAuthenticationFilter jwtAuthenticationFilter = new JwtAuthenticationFilter(authenticationManager, jwtTokenizer, redisTemplate, memberRepository);
            jwtAuthenticationFilter.setFilterProcessesUrl("/auth/login");

            // OAuthAuthenticationFilter 등록
            OAuthAuthenticationFilter oAuthAuthenticationFilter = new OAuthAuthenticationFilter("/oauth/login", authenticationManager);

//            builder.addFilter(oAuthAuthenticationFilter);
            builder.addFilterBefore(oAuthAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
            builder.addFilterAfter(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
            builder.addFilterAfter(jwtVerificationFilter, UsernamePasswordAuthenticationFilter.class); // (1)

        }
    }
//    @Bean
//    public OAuthAuthenticationProvider oAuthAuthenticationProvider(MemberRepository memberRepository) {
//        return new OAuthAuthenticationProvider(memberRepository);
//    }

//    @Bean
//    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
//        AuthenticationManagerBuilder builder = http.getSharedObject(AuthenticationManagerBuilder.class);
//        builder.authenticationProvider(new OAuthAuthenticationProvider(memberRepository));
//        return builder.build();
//    }
//    @Bean
//    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
//        return config.getAuthenticationManager();
//    }

}
