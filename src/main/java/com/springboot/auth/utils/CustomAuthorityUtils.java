package com.springboot.auth.utils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class CustomAuthorityUtils {
    // 설정파일(yml)에서 값을 주입하는 역할
    // 파라미터에는 설정파일 내부의 주소값이다.
    @Value("${mail.address.admin}")
    // 설정파일에서 주입받은 값을 받는 객체 생성
    private String adminMailAddress;

    // ADMIN 과 USER 의 권한을 담은 리스트 생성
    private final List<String> ADMIN_ROLES_STRING = List.of("ADMIN", "USER");
    // USER 의 권한을 담은 리스트 생성
    private final List<String> USER_ROLES_STRING = List.of("USER");

    // 권한 생성 메서드
    public List<String> createRoles(String email) {
        // 파라미터로 받은 이메일이 설정파일의 admin 계정과 같다면 ADMIN 권한 부여
        if(email.equals(adminMailAddress)) {
            return ADMIN_ROLES_STRING;
        } else {
            // 만약 다르다면 일반 USER 권한 부여
            return USER_ROLES_STRING;
        }
    }

    // 부여된 역할(role)리스트를 기반으로 Spring Security 에서 사용할 권한 객체로 변환하는 역할
    public List<GrantedAuthority> createAuthorities(List<String> roles) {
        // 권한 리스트를 스트림으로 변환
        return roles.stream()
                // map 으로 순회하며 스프링 시큐리티가 사용가능한 형태로 "ROLE_" 붙여줌
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                .collect(Collectors.toList());
    }
}
