package com.springboot.notice.noticeService;

import com.springboot.dashboard.dto.RecentNotice;
import com.springboot.exception.BusinessLogicException;
import com.springboot.exception.ExceptionCode;
import com.springboot.member.TestDataFactory;
import com.springboot.member.entity.Member;
import com.springboot.member.service.MemberService;
import com.springboot.notice.entity.Notice;
import com.springboot.notice.repository.NoticeRepository;
import com.springboot.notice.service.NoticeService;
import com.springboot.notice.service.S3Service;
import com.logbei.be.pushToken.service.PushTokenService;
import com.springboot.utils.AuthorizationUtils;
import org.mockito.MockedStatic;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NoticeServiceTest {

    @InjectMocks
    private NoticeService noticeService;

    @Mock
    private NoticeRepository noticeRepository;
    @Mock
    private MemberService memberService;
    @Mock
    private S3Service s3Service;
    @Mock
    private PushTokenService pushTokenService;

    private Member admin;
    private Notice sampleNotice;

    @BeforeEach
    void setUp() {
        admin = new Member();
        admin.setMemberId(100L);

        sampleNotice = new Notice();
        sampleNotice.setNoticeId(1L);
        sampleNotice.setMember(admin);
        sampleNotice.setFileUrls(new ArrayList<>());
        sampleNotice.setNoticeStatus(Notice.NoticeStatus.NOTICE_REGISTERED);
    }


    @Test
    // 이미지를 첨부한 공지사항 생성 성공 케이스
    void createNotice_withImages_success() {
        // given
        // 테스트용 관리자 객체 생성 및 설정
        Member admin = TestDataFactory.createTestMember(1L);
        admin.setRoles(List.of("USER", "ADMIN"));// 예: roles 포함된 admin 리턴

        // 새로운 공지 추가
        Notice input = new Notice();
        // notice 에 회원 셋팅
        input.setMember(admin);

        // multipartFile_01 목객체 생성
        MultipartFile file1 = mock(MultipartFile.class);
        // multipartFile_02 목객체 생성
        MultipartFile file2 = mock(MultipartFile.class);
        // List<multipartFile> 목객체 생성
        List<MultipartFile> imgs = List.of(file1, file2);

        // 검증된 회원 id 넣으면 admin 리턴
        when(memberService.findVerifiedExistsMember(admin.getMemberId())).thenReturn(admin);
        // multipartFile_01 목객체 넣으면 "url1" 리턴
        when(s3Service.upload(file1, "notice-files")).thenReturn("url1");
        // multipartFile_02 목객체 넣으면 "url2" 리턴
        when(s3Service.upload(file2, "notice-files")).thenReturn("url2");

        when(noticeRepository.save(any())).thenAnswer(i -> {
            Notice n = i.getArgument(0);
            n.setNoticeId(2L);
            return n;
        });

        when(memberService.findMembersToList(admin.getMemberId())).thenReturn(Collections.singletonList(admin));

        // when
        // AuthorizationUtils.verifyAuthorIsAdmin 이 void 메서드이므로 doNothing() 처리
        try (MockedStatic<AuthorizationUtils> utilities = mockStatic(AuthorizationUtils.class)) {
            utilities.when(() -> AuthorizationUtils.verifyAuthorIsAdmin(anyLong(), anyLong()))
                     .thenAnswer(invocation -> null); // do nothing

            Notice created = noticeService.createNotice(input, admin.getMemberId(), imgs);

            // then
            // 생성된 공지의 Id 는 1L
            assertEquals(2L, created.getNoticeId());
            // 문자열 리스트로 받기
            assertEquals(List.of("url1", "url2"), created.getFileUrls());
            // s3에 notice - files 업로드 테스트
            verify(s3Service).upload(file1, "notice-files");
            verify(s3Service).upload(file2, "notice-files");
            verify(pushTokenService, times(1))
                    .sendNoticeNotification(admin.getMemberId(), created.getTitle(), created.getContent());
        }
    }

    // 관리자가 아닌 사용자가 공지사항 생성 시 예외 발생 케이스
    @Test
    void createNotice_notAdmin_throwsException() {
        // given
        Notice input = new Notice();
        input.setTitle("제목");
        input.setContent("내용");

        Member writer = new Member();
        writer.setMemberId(1L); // 관리자가 아닌 사용자 ID
        input.setMember(writer); // 공지 객체에 사용자 삽입

        // 존재하는지 검증해서 회원찾기
        when(memberService.findVerifiedExistsMember(2L)).thenReturn(new Member());

        try (MockedStatic<AuthorizationUtils> utilities = mockStatic(AuthorizationUtils.class)) {
            // 관리자가 아닌 경우 예외 발생하도록 설정
            utilities.when(() -> AuthorizationUtils.verifyAuthorIsAdmin(1L, 2L))
                    .thenThrow(new BusinessLogicException(ExceptionCode.FORBIDDEN));

            // when / then
            assertThrows(BusinessLogicException.class,
                    () -> noticeService.createNotice(input, 2L, Collections.emptyList()));
        }
    }


    @Test
    // 기존 이미지 유지 + 새로운 이미지 병합하여 공지사항 수정 성공 케이스
    void updateNotice_mergeImages_success() {
        // given
        MultipartFile newFile = mock(MultipartFile.class);
        List<MultipartFile> newImgs = List.of(newFile);

        sampleNotice.setFileUrls(List.of("old1", "old2"));
        when(noticeRepository.findById(1L)).thenReturn(Optional.of(sampleNotice));
        when(memberService.findVerifiedExistsMember(admin.getMemberId())).thenReturn(admin);
        when(s3Service.upload(newFile, "notice-files")).thenReturn("newUrl");
        when(noticeRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        // prepare patch dto notice
        Member writer = new Member();
        writer.setMemberId(1L);
        Notice patch = new Notice();
        patch.setNoticeId(1L);
        patch.setFileUrls(List.of("old1")); // 유지할 URL
        patch.setTitle("Updated");
        patch.setMember(writer);

        // when
        try (MockedStatic<AuthorizationUtils> utilities = mockStatic(AuthorizationUtils.class)) {
            utilities.when(() -> AuthorizationUtils.verifyAuthorIsAdmin(anyLong(), anyLong()))
                     .thenAnswer(invocation -> null); // do nothing

            Notice updated = noticeService.updateNotice(patch, admin.getMemberId(), newImgs);

            // then
            assertEquals("Updated", updated.getTitle());
            assertEquals(Notice.NoticeStatus.NOTICE_UPDATED, updated.getNoticeStatus());
            assertEquals(List.of("old1", "newUrl"), updated.getFileUrls());
        }
    }

    // 삭제된 공지사항을 조회하려고 할 때 예외 발생
    @Test
    void findNotice_deleted_throwsException() {
        // given
        Notice deleted = new Notice();
        deleted.setNoticeId(5L);
        deleted.setNoticeStatus(Notice.NoticeStatus.NOTICE_DELETED);
        when(noticeRepository.findById(5L)).thenReturn(Optional.of(deleted));

        // when / then
        assertThrows(BusinessLogicException.class,
                () -> noticeService.findNotice(5L));
    }

    // 존재하는 공지사항 정상 조회 성공 케이스
    @Test
    void findNotice_success() {
        // given
        when(noticeRepository.findById(1L)).thenReturn(Optional.of(sampleNotice));

        // when
        Notice found = noticeService.findNotice(1L);

        // then
        assertSame(sampleNotice, found);
    }

    // 올바른 페이지 번호로 전체 공지 목록 조회 성공
    @Test
    void findNotices_validPage_success() {
        // given
        Page<Notice> page = new PageImpl<>(List.of(sampleNotice));
        when(noticeRepository.findAllByNoticeStatusNot(
                eq(Notice.NoticeStatus.NOTICE_DELETED),
                any(PageRequest.class)))
                .thenReturn(page);

        // when
        Page<Notice> result = noticeService.findNotices(1, 5);

        // then
        assertEquals(1, result.getTotalElements());
    }

    // 공지사항 삭제 시 S3 파일 이동 및 상태 변경 확인
    @Test
    void deleteNotice_success() {
        // given
        String url = "https://bucket.s3.amazonaws.com/notice-files/img.png";
        sampleNotice.setFileUrls(List.of(url));
        when(noticeRepository.findById(1L)).thenReturn(Optional.of(sampleNotice));
        when(memberService.findVerifiedExistsMember(admin.getMemberId())).thenReturn(admin);
        when(noticeRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        // when
        try (MockedStatic<AuthorizationUtils> utilities = mockStatic(AuthorizationUtils.class)) {
            utilities.when(() -> AuthorizationUtils.verifyAuthorIsAdmin(anyLong(), anyLong()))
                     .thenAnswer(invocation -> null); // do nothing

            noticeService.deleteNotice(1L, admin.getMemberId());
        }

        // then
        // key 추출 및 이동 호출 검증
        verify(s3Service).moveDeletedFile("notice-files/img.png", "deletedImages/img.png");
        // 최종 상태가 DELETED
        assertEquals(Notice.NoticeStatus.NOTICE_DELETED, sampleNotice.getNoticeStatus());
    }

    // 최근 등록된 공지사항 5개 목록 정상 반환
    @Test
    void findTop5RecentNotices_success() {
        // given
        Notice n1 = new Notice(); n1.setTitle("A"); n1.setCreatedAt(LocalDateTime.of(2025,5,1,0,0));
        Notice n2 = new Notice(); n2.setTitle("B"); n2.setCreatedAt(LocalDateTime.of(2025,5,2,0,0));
        when(noticeRepository.findTop5ByOrderByCreatedAtDesc()).thenReturn(List.of(n2, n1));

        // when
        List<RecentNotice> list = noticeService.findTop5RecentNotices();

        // then
        assertEquals(2, list.size());
        assertEquals("B", list.get(0).getTitle());
        assertEquals(LocalDateTime.of(2025,5,2,0,0), list.get(0).getCreatedAt());
    }

    // 삭제 상태 공지사항에 접근 시 예외 발생 확인
    @Test
    void getDeletedNotice_throwsException() {
        Notice del = new Notice();
        del.setNoticeStatus(Notice.NoticeStatus.NOTICE_DELETED);
        assertThrows(BusinessLogicException.class,
                () -> noticeService.getDeletedNotice(del));
    }

    // 존재하는 공지 ID 정상 반환
    @Test
    void findVerifiedExistsNotice_success() {
        when(noticeRepository.findById(1L)).thenReturn(Optional.of(sampleNotice));
        Notice n = noticeService.findVerifiedExistsNotice(1L);
        assertSame(sampleNotice, n);
    }
}
