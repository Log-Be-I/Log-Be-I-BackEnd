package com.springboot.notice.service;

import com.springboot.exception.BusinessLogicException;
import com.springboot.exception.ExceptionCode;
import com.springboot.notice.entity.Notice;
import com.springboot.notice.repository.NoticeRepository;
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

    //notice 등록
    public Notice createNotice(Notice notice) {
        //notice 등록 후 반환
        return noticeRepository.save(notice);
    }
    //notice 수정 -> 수정 전 원본데이터 저장X
    public Notice updateNotice(Notice notice) {
       //기존 등록된 데이터
        Notice findNotice = findVerifiedNotice(notice.getNoticeId());

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
    public void deleteNotice(long noticeId) {
        Notice findNotice = findVerifiedNotice(noticeId);
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

