package com.springboot.auth.utils;

import com.springboot.member.entity.Member;
import com.springboot.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final MemberRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (userRepository.count() == 0) {
            Member member = new Member();
            member.setBirth("2000-11-11");
            member.setRegion("세종시");
            member.setName("관리자01");
            member.setNickname("관리자01");
            member.setProfile("url");
            member.setEmail("admin1@gmail.com");
            member.setRoles(List.of("ROLE_ADMIN","ROLE_USER"));
            member.setRefreshToken(passwordEncoder.encode("1234"));

            Member member01 = new Member();
            member01.setBirth("2000-12-12");
            member01.setRegion("대전시");
            member01.setNickname("관리자02");
            member01.setName("관리자02");
            member01.setProfile("url");
            member01.setEmail("admin2@gmail.com");
            member01.setRoles(List.of("ADMIN", "USER"));
            member01.setRefreshToken(passwordEncoder.encode("5678"));

            Member member03 = new Member();
            member03.setBirth("2000-01-01");
            member03.setNickname("관리자03");
            member03.setRegion("서울시");
            member03.setName("관리자03");
            member03.setProfile("url");
            member03.setEmail("admin3@gmail.com");
            member03.setRoles(List.of("ADMIN", "USER"));
            member03.setRefreshToken(passwordEncoder.encode("9012"));

            userRepository.saveAll(List.of(
               member03, member01, member
            ));
        }
    }
}
