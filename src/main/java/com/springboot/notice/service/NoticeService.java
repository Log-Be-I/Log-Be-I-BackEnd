package com.springboot.notice.service;

import com.springboot.exception.BusinessLogicException;
import com.springboot.exception.ExceptionCode;
import com.springboot.member.service.MemberService;
import com.springboot.notice.entity.Notice;
import com.springboot.notice.repository.NoticeRepository;
import com.springboot.utils.AuthorizationUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class NoticeService {

    private final NoticeRepository noticeRepository;
    private final MemberService memberService;

    //notice 등록
    public Notice createNotice(Notice notice, Long adminId) {
        memberService.validateExistingMember(adminId);
        //작성자가 관리자인지 확인하고 아니면 예외
        AuthorizationUtils.verifyAuthorIsAdmin(notice.getMember().getMemberId(), adminId);
        //notice 등록 후 반환
        return noticeRepository.save(notice);
    }
    //notice 수정 -> 덮어씌워 저장
    public Notice updateNotice(Notice notice, long adminId) {
       //기존 등록된 데이터
        Notice findNotice = findVerifiedNotice(notice.getNoticeId());
       //등록된 회원인지 확인
        memberService.validateExistingMember(adminId);
        //관리자인지 확인
       AuthorizationUtils.verifyAuthorIsAdmin(findNotice.getMember().getMemberId(), adminId);
        //변경가능한 필드 확인 후 변경
        Optional.ofNullable(notice.getNoticeType())
                .ifPresent(noticeType -> findNotice.setNoticeType(noticeType));
        Optional.ofNullable(notice.getTitle())
                .ifPresent(title ->  findNotice.setTitle(title));
        Optional.ofNullable(notice.getContent())
                .ifPresent(content ->  findNotice.setContent(content));
        Optional.ofNullable(notice.getImage())
                .ifPresent(image ->  findNotice.setImage(image));
        Optional.ofNullable(notice.getIsPinned())
                .ifPresent(isPinned ->  findNotice.setIsPinned(isPinned));

        //수정 후 상태 변경
        findNotice.setNoticeStatus(Notice.NoticeStatus.NOTICE_UPDATED);
        //수정된 데이터 새로 저장 후 반환
        return noticeRepository.save(findNotice);
    }

    //notice 단일 조회
    public Notice findNotice(long noticeId) {
        return findVerifiedNotice(noticeId);
    }

    //notice 전체 목록 조회
    public Page<Notice> findNotices(int page, int size) {
        //page 번호검증
        if (page < 1) {
            throw new IllegalArgumentException("페이지의 번호는 1 이상이어야 합니다.");
        }
        return noticeRepository.findAll(
                PageRequest.of(page - 1, size, Sort.by("createdAt").descending()));
    }

    //notice 삭제: 상태변경
    public void deleteNotice(long noticeId, long adminId) {
        Notice findNotice = findVerifiedNotice(noticeId);
        //회원인지 확인
        memberService.validateExistingMember(adminId);
        AuthorizationUtils.verifyAuthorIsAdmin(findNotice.getMember().getMemberId(), adminId);
        findNotice.setNoticeStatus(Notice.NoticeStatus.NOTICE_DELTETED);
        //변경사항 저장
        noticeRepository.save(findNotice);
    }

    //검증로직 : noticeId로 DB 조회, 없으면 예외처리
    public Notice findVerifiedNotice(long noticeId) {
        return noticeRepository.findById(noticeId).orElseThrow(
                () -> new BusinessLogicException(ExceptionCode.NOTICE_NOT_EXIST)
        );
    }



}

