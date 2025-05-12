package com.springboot.notice.controller;

import com.springboot.auth.utils.CustomPrincipal;
import com.springboot.notice.dto.NoticePatchDto;
import com.springboot.notice.dto.NoticePostDto;
import com.springboot.notice.dto.NoticeResponseDto;
import com.springboot.notice.entity.Notice;
import com.springboot.notice.mapper.NoticeMapper;
import com.springboot.notice.service.NoticeService;
import com.springboot.responsedto.MultiResponseDto;
import com.springboot.responsedto.SingleResponseDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class NoticeControllerTest {

    @InjectMocks
    private NoticeController noticeController;

    @Mock
    private NoticeService noticeService;

    @Mock
    private NoticeMapper noticeMapper;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // 공지 등록 테스트
    @Test
    void postNotice_shouldReturnCreatedResponse() {
        NoticePostDto postDto = new NoticePostDto();
        postDto.setMemberId(1L);
        CustomPrincipal principal = mock(CustomPrincipal.class);
        when(principal.getMemberId()).thenReturn(1L);

        Notice notice = new Notice();
        notice.setNoticeId(100L);

        NoticeResponseDto responseDto = new NoticeResponseDto();

        when(noticeService.createNotice(any(Notice.class), eq(1L), any())).thenReturn(notice);
        when(noticeMapper.noticePostToNotice(postDto)).thenReturn(notice);
        when(noticeMapper.noticeToNoticeResponse(notice)).thenReturn(responseDto);

        ResponseEntity<?> response = noticeController.postNotice(postDto, null, principal);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(((SingleResponseDto<?>) response.getBody()).getData()).isEqualTo(responseDto);
    }

    // 공지 수정 테스트
    @Test
    void patchNotice_shouldReturnUpdatedNotice() {
        NoticePatchDto patchDto = new NoticePatchDto();
        patchDto.setNoticeId(100L);
        CustomPrincipal principal = mock(CustomPrincipal.class);
        when(principal.getMemberId()).thenReturn(1L);

        Notice notice = new Notice();
        NoticeResponseDto responseDto = new NoticeResponseDto();

        when(noticeMapper.noticePatchToNotice(patchDto)).thenReturn(notice);
        when(noticeService.updateNotice(notice, 1L, null)).thenReturn(notice);
        when(noticeMapper.noticeToNoticeResponse(notice)).thenReturn(responseDto);

        ResponseEntity<?> response = noticeController.patchNotice(patchDto, 100L, null, principal);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(((SingleResponseDto<?>) response.getBody()).getData()).isEqualTo(responseDto);
    }

    // 공지 상세 조회 테스트
    @Test
    void getNotice_shouldReturnNotice() {
        long noticeId = 1L;
        Notice notice = new Notice();
        NoticeResponseDto responseDto = new NoticeResponseDto();

        when(noticeService.findNotice(noticeId)).thenReturn(notice);
        when(noticeMapper.noticeToNoticeResponse(notice)).thenReturn(responseDto);

        ResponseEntity<?> response = noticeController.getNotice(noticeId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(((SingleResponseDto<?>) response.getBody()).getData()).isEqualTo(responseDto);
    }

    // 공지 목록 조회 테스트
    @Test
    void getNotices_shouldReturnNoticeList() {
        int page = 1, size = 10;
        Notice notice = new Notice();
        List<Notice> noticeList = List.of(notice);
        Page<Notice> pageResult = new PageImpl<>(noticeList);
        NoticeResponseDto responseDto = new NoticeResponseDto();

        when(noticeService.findNotices(page, size)).thenReturn(pageResult);
        when(noticeMapper.noticesToNoticeResponses(noticeList)).thenReturn(List.of(responseDto));

        ResponseEntity<?> response = noticeController.getNotices(page, size);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        MultiResponseDto<?> body = (MultiResponseDto<?>) response.getBody();
        assertThat((List<NoticeResponseDto>) body.getData()).containsExactly(responseDto);
    }

    // 공지 삭제 테스트
    @Test
    void deleteNotice_shouldReturnNoContent() {
        long noticeId = 1L;
        CustomPrincipal principal = mock(CustomPrincipal.class);
        when(principal.getMemberId()).thenReturn(1L);

        doNothing().when(noticeService).deleteNotice(noticeId, 1L);

        ResponseEntity<?> response = noticeController.deleteNotice(noticeId, principal);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    // 공지 상세 조회 실패 테스트 - 존재하지 않는 ID
    @Test
    void getNotice_whenNotFound_shouldThrowException() {
        long noticeId = 999L;

        when(noticeService.findNotice(noticeId))
                .thenThrow(new RuntimeException("공지사항을 찾을 수 없습니다."));

        try {
            noticeController.getNotice(noticeId);
        } catch (RuntimeException e) {
            assertThat(e.getMessage()).isEqualTo("공지사항을 찾을 수 없습니다.");
        }
    }

    // 공지 수정 실패 테스트 - 권한 없음
    @Test
    void patchNotice_whenNotAuthorized_shouldThrowException() {
        NoticePatchDto patchDto = new NoticePatchDto();
        patchDto.setNoticeId(100L);
        CustomPrincipal principal = mock(CustomPrincipal.class);
        when(principal.getMemberId()).thenReturn(1L);

        Notice notice = new Notice();
        when(noticeMapper.noticePatchToNotice(patchDto)).thenReturn(notice);
        when(noticeService.updateNotice(notice, 1L, null))
                .thenThrow(new SecurityException("수정 권한이 없습니다."));

        try {
            noticeController.patchNotice(patchDto, 100L, null, principal);
        } catch (SecurityException e) {
            assertThat(e.getMessage()).isEqualTo("수정 권한이 없습니다.");
        }
    }

    // 공지 삭제 실패 테스트 - 존재하지 않음
    @Test
    void deleteNotice_whenNotFound_shouldThrowException() {
        long noticeId = 999L;
        CustomPrincipal principal = mock(CustomPrincipal.class);
        when(principal.getMemberId()).thenReturn(1L);

        doThrow(new IllegalArgumentException("삭제할 공지사항이 존재하지 않습니다."))
                .when(noticeService).deleteNotice(noticeId, 1L);

        try {
            noticeController.deleteNotice(noticeId, principal);
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage()).isEqualTo("삭제할 공지사항이 존재하지 않습니다.");
        }
    }
}

