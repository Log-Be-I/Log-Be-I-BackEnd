package com.logbei.be.keyword.service;

import com.logbei.be.keyword.entity.Keyword;
import com.logbei.be.keyword.repository.KeywordRepository;
import com.logbei.be.member.TestDataFactory;
import com.logbei.be.member.entity.Member;
import com.logbei.be.member.service.MemberService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
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
    @DisplayName("createKeyword - 기존 키워드 삭제 전 연관관계 끊고 새 키워드 등록")
    void createKeyword_shouldReplaceAllKeywords() {
        // given
        Long memberId = 1L;
        Member member = TestDataFactory.createTestMember(memberId);

        // 기존 키워드 2개
        Keyword old1 = TestDataFactory.createTestKeyword("AI", member);
        Keyword old2 = TestDataFactory.createTestKeyword("웰빙", member);
        List<Keyword> existingKeywords = List.of(old1, old2);

        // 새 키워드 2개
        Keyword new1 = TestDataFactory.createTestKeyword("여름", null);
        Keyword new2 = TestDataFactory.createTestKeyword("휴가", null);
        List<Keyword> newKeywords = List.of(new1, new2);

        given(memberService.findVerifiedExistsMember(memberId)).willReturn(member);
        given(keywordRepository.findAllByMember_MemberId(memberId)).willReturn(existingKeywords);

        // when
        keywordService.createKeyword(newKeywords, memberId);

        // then
        // 기존 키워드 연관관계 해제 확인
        assertNull(old1.getMember());
        assertNull(old2.getMember());

        // 삭제 대상 확인
        ArgumentCaptor<List<Keyword>> deleteCaptor = ArgumentCaptor.forClass(List.class);
        verify(keywordRepository).deleteAll(deleteCaptor.capture());
        List<Keyword> deleted = deleteCaptor.getValue();
        assertEquals(2, deleted.size());

        // 저장 대상 확인
        ArgumentCaptor<List<Keyword>> saveCaptor = ArgumentCaptor.forClass(List.class);
        verify(keywordRepository).saveAll(saveCaptor.capture());
        List<Keyword> saved = saveCaptor.getValue();
        assertEquals(2, saved.size());
        assertEquals("여름", saved.get(0).getName());
        assertEquals(memberId, saved.get(0).getMember().getMemberId());
    }

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