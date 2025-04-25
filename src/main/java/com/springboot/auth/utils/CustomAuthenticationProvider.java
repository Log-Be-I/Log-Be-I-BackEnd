package com.springboot.auth.utils;

import com.springboot.member.entity.Member;
import com.springboot.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Component
public class CustomAuthenticationProvider implements AuthenticationProvider {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String email = (String) authentication.getPrincipal();
        String rawPassword = (String) authentication.getCredentials();
//        Member member = null;
//        log.info("DB password: {}", );
//        log.info("입력 password: {}", rawPassword.);
//        log.info("✅ FormLoginAuthenticationProvider 호출됨 - 이메일: {}", email);

        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("해당 이메일 없음: " + email));

        if (!passwordEncoder.matches(rawPassword, member.getRefreshToken())) {
            throw new BadCredentialsException("❌ 비밀번호가 일치하지 않음");
        }

        return new UsernamePasswordAuthenticationToken(
                email,
                null,
                member.getRoles().stream()
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toList())
        );
    }


//    private String extractId(String prefixEncodedPassword) {
//        if (prefixEncodedPassword == null) {
//            return null;
//        }
//        // 생략
//    }
    @Override
    public boolean supports(Class<?> authentication) {
        boolean result = UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
        log.info("🧪 supports() 결과: {}, 타입: {}", result, authentication.getName());
        return result;
    }
}
