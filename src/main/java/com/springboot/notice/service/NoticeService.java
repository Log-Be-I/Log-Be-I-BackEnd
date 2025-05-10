package com.springboot.notice.service;

import com.springboot.dashboard.dto.RecentNotice;
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
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NoticeService {

    private final NoticeRepository noticeRepository;
    private final MemberService memberService;
    private final S3Service s3Service;

    //notice 등록
    public Notice createNotice(Notice notice, Long adminId, List<MultipartFile> images) {

        List<String> fileUrls = new ArrayList<>();
        if(images != null && !images.isEmpty()) {
            for(MultipartFile file : images) {
                String url = s3Service.upload(file, "notice-files");
                fileUrls.add(url);
            }
        }

        memberService.findVerifiedExistsMember(adminId);
        //작성자가 관리자인지 확인하고 아니면 예외
        AuthorizationUtils.verifyAuthorIsAdmin(notice.getMember().getMemberId(), adminId);
        notice.setFileUrls(fileUrls);
        //notice 등록 후 반환
        return noticeRepository.save(notice);
    }

    //notice 수정 -> 덮어씌워 저장
    public Notice updateNotice(Notice notice, long adminId, List<MultipartFile> images) {
       //기존 등록된 데이터
        Notice findNotice = findVerifiedExistsNotice(notice.getNoticeId());
       //등록된 회원인지 확인

        memberService.findVerifiedExistsMember(adminId);

        //관리자인지 확인
       AuthorizationUtils.verifyAuthorIsAdmin(findNotice.getMember().getMemberId(), adminId);
        //변경가능한 필드 확인 후 변경
        Optional.ofNullable(notice.getNoticeType())
                .ifPresent(noticeType -> findNotice.setNoticeType(noticeType));
        Optional.ofNullable(notice.getTitle())
                .ifPresent(title ->  findNotice.setTitle(title));
        Optional.ofNullable(notice.getContent())
                .ifPresent(content ->  findNotice.setContent(content));
        Optional.ofNullable(notice.getIsPinned())
                .ifPresent(isPinned ->  findNotice.setIsPinned(isPinned));

        //이미지 병합
        List<String> mergedUrls = new ArrayList<>();

        //유지할 기존 이미지 URL
        if(notice.getFileUrls() != null) {
            mergedUrls.addAll(notice.getFileUrls());
        }
        //새로 업로드된 이미지가 있다면 S3 업로드 후 병합
        if(images != null && !images.isEmpty()) {
            for (MultipartFile file : images) {
                String url =  s3Service.upload(file, "notice-files");
                mergedUrls.add(url);
            }
        }
        findNotice.setFileUrls(mergedUrls);

        //수정 후 상태 변경
        findNotice.setNoticeStatus(Notice.NoticeStatus.NOTICE_UPDATED);
        //수정된 데이터 새로 저장 후 반환
        return noticeRepository.save(findNotice);
    }

    //notice 단일 조회
    public Notice findNotice(long noticeId) {
        Notice notice = findVerifiedExistsNotice(noticeId);
        return getDeletedNotice(notice);
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
        Notice findNotice = findVerifiedExistsNotice(noticeId);
        //삭제 상태의 경우 예외발생
        getDeletedNotice(findNotice);
        //회원인지 확인
        memberService.findVerifiedExistsMember(adminId);
        AuthorizationUtils.verifyAuthorIsAdmin(findNotice.getMember().getMemberId(), adminId);
        //삭제된 notice 첨부파일을 deletedImages/로 이동

       for(String imageUrl : findNotice.getFileUrls()) {
           String sourceKey = extractKeyFromUrl(imageUrl);
           String targetKey = sourceKey.replace("notice-files/", "deletedImages/");
           s3Service.moveDeletedFile(sourceKey, targetKey);
       }
        findNotice.setNoticeStatus(Notice.NoticeStatus.NOTICE_DELETED);
        //변경사항 저장
        noticeRepository.save(findNotice);
    }


    //검증로직 : noticeId로 DB 조회, 없으면 예외처리
    public Notice findVerifiedExistsNotice(long noticeId) {
        return noticeRepository.findById(noticeId).orElseThrow(
                () -> new BusinessLogicException(ExceptionCode.NOTICE_NOT_FOUND)
        );
    }

    //adminWeb - main page : 최신 등록된 공지글 정보 반환
    public List<RecentNotice> findTop5RecentNotices() {
        //최근 등록되 공지글 5개 내림차순으로 조회
        List<Notice> noticeList = noticeRepository.findTop5ByOrderByCreatedAtDesc();

        return noticeList.stream().map(
                notice -> new RecentNotice(notice.getTitle(), notice.getCreatedAt()))
                .collect(Collectors.toList());
    }

    //단일 조회시 삭제상태의 공지일 경우 예외발생
    public Notice getDeletedNotice(Notice notice) {
        if(notice.getNoticeStatus() == Notice.NoticeStatus.NOTICE_DELETED) {
            throw new BusinessLogicException(ExceptionCode.NOTICE_NOT_FOUND);
        }
        return notice;
    }

    //전체 조회 시, 삭제 상태의 공지는 제외
    public List<Notice> nonDeletedNoticeAndAuth (List<Notice> notices) {
        return notices.stream().filter(notice -> notice.getNoticeStatus() != Notice.NoticeStatus.NOTICE_DELETED)
                .peek(notice ->
                        // 관리자 or owner 가 아니라면 예외 처리
                        AuthorizationUtils.verifyAdmin()
                ).collect(Collectors.toList());

    }

    //S3 이미지 URL에서 key(=파일경로) 추출
    public String extractKeyFromUrl(String url) {
        // S3 도메인을 기준으로 key 추출
        // 예: https://bucket-name.s3.amazonaws.com/notice-files/img.png → notice-files/img.png
        int index = url.indexOf(".com/"); // 고정된 S3 URL 패턴 기준
        if (index == -1 || index + 5 >= url.length()) {
            throw new IllegalArgumentException("S3 URL 형식이 올바르지 않습니다: " + url);
        }
        return url.substring(index + 5); // ".com/" 다음부터 끝까지가 key
    }

}

