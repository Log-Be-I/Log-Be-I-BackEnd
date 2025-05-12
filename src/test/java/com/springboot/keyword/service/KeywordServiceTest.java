package com.springboot.keyword.service;

import com.springboot.keyword.entity.Keyword;
import com.springboot.keyword.repository.KeywordRepository;
import com.springboot.member.entity.Member;
import com.springboot.member.service.MemberService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class KeywordServiceTest {

    @Mock
    private KeywordRepository keywordRepository;

    @Mock
    private MemberService memberService;

    @InjectMocks
    private KeywordService keywordService;

    private Member member;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        member = new Member();
        member.setMemberId(1L);
    }

    @Test
    void createKeyword_shouldDeleteOldAndSaveNewKeywords() {
        Keyword oldKeyword = new Keyword();
        oldKeyword.setKeywordId(1L);
        oldKeyword.setMember(member);

        Keyword newKeyword = new Keyword();
        newKeyword.setKeywordId(2L);

        List<Keyword> newKeywords = List.of(newKeyword);
        List<Keyword> oldKeywords = List.of(oldKeyword);

        when(memberService.findVerifiedExistsMember(1L)).thenReturn(member);
        when(keywordRepository.findAllByMember_MemberId(1L)).thenReturn(oldKeywords);

        List<Keyword> result = keywordService.createKeyword(newKeywords, 1L);

        verify(keywordRepository).delete(oldKeyword);
        verify(keywordRepository).save(newKeyword);
        assertEquals(1, result.size());
    }

    @Test
    void createKeyword_shouldOnlyDeleteWhenEmptyList() {
        Keyword oldKeyword = new Keyword();
        oldKeyword.setKeywordId(1L);
        oldKeyword.setMember(member);

        List<Keyword> oldKeywords = List.of(oldKeyword);

        when(memberService.findVerifiedExistsMember(1L)).thenReturn(member);
        when(keywordRepository.findAllByMember_MemberId(1L)).thenReturn(oldKeywords);

        List<Keyword> result = keywordService.createKeyword(Collections.emptyList(), 1L);

        verify(keywordRepository).delete(oldKeyword);
        verify(keywordRepository, never()).save(any());
        assertEquals(0, result.size());
    }

    @Test
    void findKeywords_shouldReturnAllKeywordsOfMember() {
        Keyword keyword = new Keyword();
        keyword.setKeywordId(1L);
        keyword.setMember(member);

        List<Keyword> keywords = List.of(keyword);

        when(memberService.findVerifiedExistsMember(1L)).thenReturn(member);
        when(keywordRepository.findAllByMember_MemberId(1L)).thenReturn(keywords);

        List<Keyword> result = keywordService.findKeywords(1L);

        verify(keywordRepository).findAllByMember_MemberId(1L);
        assertEquals(1, result.size());
    }
}