package com.springboot.keyword.service;

import com.springboot.keyword.entity.Keyword;
import com.springboot.keyword.repository.KeywordRepository;
import com.springboot.member.TestDataFactory;
import com.springboot.member.entity.Member;
import com.springboot.member.service.MemberService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class KeywordServiceTest {

    @InjectMocks
    private KeywordService keywordService;

    @Mock
    private KeywordRepository keywordRepository;

    @Mock
    private MemberService memberService;

    @Test
    void createKeyword_shouldDeleteOldAndSaveNew() {
        // given
        Long memberId = 1L;
        Member member = TestDataFactory.createTestMember(memberId);

        Keyword existingAI = TestDataFactory.createTestKeyword("AI", member);
        Keyword existingTrip = TestDataFactory.createTestKeyword("여행", member);
        List<Keyword> existingKeywords = List.of(existingAI, existingTrip);

        Keyword newAI = TestDataFactory.createTestKeyword("AI", null); // 중복
        Keyword newHealth = TestDataFactory.createTestKeyword("건강", null); // 새로 추가
        List<Keyword> newKeywords = List.of(newAI, newHealth);

        given(memberService.findVerifiedExistsMember(memberId)).willReturn(member);
        given(keywordRepository.findAllByMember_MemberId(memberId)).willReturn(existingKeywords);

        // when
        keywordService.createKeyword(newKeywords, memberId);

        // then
        ArgumentCaptor<List<Keyword>> deleteCaptor = ArgumentCaptor.forClass(List.class);
        verify(keywordRepository).deleteAll(deleteCaptor.capture());
        List<Keyword> deleted = deleteCaptor.getValue();

        assertEquals(1, deleted.size());
        assertEquals("여행", deleted.get(0).getName());

        ArgumentCaptor<List<Keyword>> saveCaptor = ArgumentCaptor.forClass(List.class);
        verify(keywordRepository).saveAll(saveCaptor.capture());
        List<Keyword> saved = saveCaptor.getValue();

        assertEquals(1, saved.size());
        assertEquals("건강", saved.get(0).getName());
        assertEquals(memberId, saved.get(0).getMember().getMemberId());

        verify(keywordRepository, times(2)).findAllByMember_MemberId(memberId);
    }

//    @Test
//    void findKeywords_shouldReturnAllKeywordsOfMember() {
//        Long memberId = 1L;
//        Member member = TestDataFactory.createTestMember(memberId);
//        Keyword keyword = new Keyword();
//        keyword.setKeywordId(1L);
//        keyword.setMember(member);
//
//        List<Keyword> keywords = List.of(keyword);
//
//        when(memberService.findVerifiedExistsMember(1L)).thenReturn(member);
//        when(keywordRepository.findAllByMember_MemberId(1L)).thenReturn(keywords);
//
//        List<Keyword> result = keywordService.findKeywords(1L);
//
//        verify(keywordRepository).findAllByMember_MemberId(1L);
//        assertEquals(1, result.size());
//    }
    @Test
    void findKeywords_shouldReturnAllKeywordsOfMember() {
        // given
        Long memberId = 1L;
        Member member = TestDataFactory.createTestMember(memberId);
        Keyword keyword = TestDataFactory.createTestKeyword("AI", member);
        keyword.setKeywordId(1L);

        List<Keyword> keywords = List.of(keyword);

        when(memberService.findVerifiedExistsMember(memberId)).thenReturn(member);
        when(keywordRepository.findAllByMember_MemberId(memberId)).thenReturn(keywords);

        // when
        List<Keyword> result = keywordService.findKeywords(memberId);

        // then
        verify(keywordRepository).findAllByMember_MemberId(memberId);
        assertEquals(1, result.size());
        assertEquals("AI", result.get(0).getName());
        assertEquals(memberId, result.get(0).getMember().getMemberId());
    }
}