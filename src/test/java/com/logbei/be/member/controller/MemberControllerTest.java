package com.logbei.be.member.controller;

import com.logbei.be.auth.utils.CustomPrincipal;
import com.logbei.be.exception.BusinessLogicException;
import com.logbei.be.exception.ExceptionCode;
import com.logbei.be.member.dto.MemberPostDto;
import com.logbei.be.member.dto.MemberResponseDto;
import com.logbei.be.member.entity.Member;
import com.logbei.be.member.mapper.MemberMapper;
import com.logbei.be.member.service.MemberService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MemberControllerTest {

    @InjectMocks
    private MemberController memberController;

    @Mock
    private MemberService memberService;

    @Mock
    private MemberMapper memberMapper;

    @Mock
    private StringRedisTemplate stringRedisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @Test
    @DisplayName("회원가입 - 성공")
    void postMember_success() {
        // given
        MemberPostDto postDto = new MemberPostDto();
        postDto.setEmail("test@example.com");
        postDto.setName("홍길동");

        Member member = new Member();
        member.setEmail(postDto.getEmail());

        MemberResponseDto responseDto = new MemberResponseDto();

        when(memberMapper.memberPostDtoToMember(postDto)).thenReturn(member);
        when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("temp:refreshToken:" + member.getEmail())).thenReturn("mockRefreshToken");

        Map<String, String> tokens = Map.of(
                "accessToken", "access-token",
                "refreshToken", "refresh-token"
        );

        when(memberService.createMember(member)).thenReturn(tokens);
        when(memberMapper.memberToMemberResponseDto(member)).thenReturn(new MemberResponseDto());

        // when
        ResponseEntity<?> response = memberController.postMember(postDto);

        // then
        assertEquals(HttpStatus.CREATED, response.getStatusCode());

    }

    @Test
    @DisplayName("회원가입 - 실패 (refreshToken 없음)")
    void postMember_fail_whenRefreshTokenMissing() {
        // given
        MemberPostDto postDto = new MemberPostDto();
        postDto.setEmail("test@example.com");
        postDto.setName("홍길동");

        Member member = new Member();
        member.setEmail(postDto.getEmail());

        when(memberMapper.memberPostDtoToMember(postDto)).thenReturn(member);
        when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("temp:refreshToken:" + member.getEmail())).thenReturn(null); // ❌ refreshToken 없음

        // when
        ResponseEntity<?> response = memberController.postMember(postDto);

        // then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("구글 인증 정보가 만료되었거나 존재하지 않습니다..", response.getBody());
    }

    @Test
    @DisplayName("회원 삭제 - 성공")
    void deleteMember_success() {
        // given
        long memberId = 1L;
        String request = "자발적 탈퇴";
        String memberEmail = "test@example.com";

        CustomPrincipal customPrincipal = mock(CustomPrincipal.class);
        when(customPrincipal.getEmail()).thenReturn(memberEmail);

        doNothing().when(memberService).deleteMember(memberEmail, request);

        // when
        ResponseEntity<?> response = memberController.deleteMember(memberId, customPrincipal, request);

        // then
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(memberService).deleteMember(memberEmail, request);
    }

    @Test
    @DisplayName("회원 삭제 실패 - 권한 없음 (FORBIDDEN)")
    void deleteMember_forbidden() {
        // given
        long memberId = 1L;
        String request = "탈퇴 요청";
        String memberEmail = "notAllowed@example.com";

        CustomPrincipal principal = mock(CustomPrincipal.class);
        when(principal.getEmail()).thenReturn(memberEmail);

        doThrow(new BusinessLogicException(ExceptionCode.FORBIDDEN))
                .when(memberService).deleteMember(eq(memberEmail), eq(request));

        // when & then
        BusinessLogicException exception = assertThrows(BusinessLogicException.class, () ->
                memberController.deleteMember(memberId, principal, request)
        );

        assertEquals(ExceptionCode.FORBIDDEN, exception.getExceptionCode());
    }

    @Test
    @DisplayName("회원 삭제 실패 - 회원 없음 (NOT_FOUND)")
    void deleteMember_notFound() {
        // given
        long memberId = 999L;
        String request = "탈퇴 요청";
        String memberEmail = "missing@example.com";

        CustomPrincipal principal = mock(CustomPrincipal.class);
        when(principal.getEmail()).thenReturn(memberEmail);

        doThrow(new BusinessLogicException(ExceptionCode.MEMBER_NOT_FOUND))
                .when(memberService).deleteMember(eq(memberEmail), eq(request));

        // when & then
        BusinessLogicException exception = assertThrows(BusinessLogicException.class, () ->
                memberController.deleteMember(memberId, principal, request)
        );

        assertEquals(ExceptionCode.MEMBER_NOT_FOUND, exception.getExceptionCode());
    }
}
