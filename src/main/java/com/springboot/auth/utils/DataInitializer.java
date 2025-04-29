package com.springboot.auth.utils;

import com.springboot.category.entity.Category;
import com.springboot.category.mapper.CategoryMapper;
import com.springboot.category.repository.CategoryRepository;
import com.springboot.member.entity.Member;
import com.springboot.member.repository.MemberRepository;
import com.springboot.member.service.MemberService;
import com.springboot.record.entity.Record;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final MemberRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final CategoryRepository categoryRepository;

    
    
    @Override
    public void run(String... args) {
         List<String> categoryNames = List.of("일상", "소비", "건강", "할 일", "기타");
        
        
        if (userRepository.count() == 0) {
            Member member = new Member();
            member.setBirth("2006-11-11");
            member.setRegion("세종시");
            member.setName("관리자01");
            member.setNickname("관리자01");
            member.setProfile("url");
            member.setEmail("admin1@gmail.com");
            member.setRoles(List.of("ADMIN","USER"));
            member.setRefreshToken(passwordEncoder.encode("1234"));
             List<Category> categoryList = categoryNames.stream()
                    .map(categoryName -> new Category(categoryName, "url", member, true))
                    .collect(Collectors.toList());
            categoryList.stream().map(category -> categoryRepository.save(category));
            member.setCategories(categoryList);
            
            Member member01 = new Member();
            member01.setBirth("2008-12-12");
            member01.setRegion("대전시");
            member01.setNickname("관리자02");
            member01.setName("관리자02");
            member01.setProfile("url");
            member01.setEmail("admin2@gmail.com");
            member01.setRoles(List.of("ADMIN", "USER"));
            member01.setRefreshToken(passwordEncoder.encode("5678"));

            Member member03 = new Member();
            member03.setBirth("2004-01-01");
            member03.setNickname("관리자03");
            member03.setRegion("서울시");
            member03.setName("관리자03");
            member03.setProfile("url");
            member03.setEmail("admin3@gmail.com");
            member03.setRoles(List.of("ADMIN", "USER"));
            member03.setRefreshToken(passwordEncoder.encode("9012"));

            Member member04 = new Member();
            member04.setBirth("1995-10-24");
            member04.setNickname("미나리");
            member04.setRegion("서울시");
            member04.setName("나리나리");
            member04.setProfile("url");
            member04.setEmail("menari@gmail.com");
            member04.setRoles(List.of("USER"));
            member04.setRefreshToken(passwordEncoder.encode("alalskflskfl"));
            List<Record> records = new ArrayList<>();
            List<Category> categoryList01 = categoryNames.stream()
                    .map(categoryName -> new Category(categoryName, "url", member, true))
                    .collect(Collectors.toList());
            categoryList.stream().map(category -> categoryRepository.save(category));
            member04.setCategories(categoryList01);


            userRepository.saveAll(List.of(
               member04, member03, member01, member
            ));
        }
    }
}
