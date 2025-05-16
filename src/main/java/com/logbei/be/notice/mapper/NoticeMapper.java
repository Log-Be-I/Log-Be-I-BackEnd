package com.logbei.be.notice.mapper;


<<<<<<< HEAD:src/main/java/com/springboot/notice/mapper/NoticeMapper.java
import com.springboot.notice.dto.NoticePatchDto;
import com.springboot.notice.dto.NoticePostDto;
import com.springboot.notice.dto.NoticeResponseDto;
import com.springboot.notice.entity.Notice;
=======
import com.logbei.be.notice.dto.NoticeDto;
import com.logbei.be.notice.entity.Notice;
>>>>>>> 3cfffea (패키지명 변경):src/main/java/com/logbei/be/notice/mapper/NoticeMapper.java
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface NoticeMapper {
  @Mapping(target = "member.memberId", source = "memberId")
   Notice noticePostToNotice(NoticePostDto post);
   Notice noticePatchToNotice(NoticePatchDto patch);
//   @Mapping(target = "memberId", source = "member.memberId")
//   NoticeDto.Response noticeToNoticeResponse(Notice notice);

   default NoticeResponseDto noticeToNoticeResponse(Notice notice){
      NoticeResponseDto noticeResponseDto = new NoticeResponseDto(
              notice.getNoticeId(),
              notice.getTitle(),
              notice.getContent(),
              notice.getFileUrls(),
              notice.getMember().getMemberId(),
              notice.getNoticeType(),
              notice.getNoticeStatus(),
              notice.getIsPinned(),
              notice.getCreatedAt(),
              notice.getModifiedAt()
      );
      return noticeResponseDto;
   }

   default List<NoticeResponseDto> noticesToNoticeResponses(List<Notice> notices) {
      return notices.stream().filter(notice -> notice.getNoticeStatus() != Notice.NoticeStatus.NOTICE_DELETED)
              .map(notice -> noticeToNoticeResponse(notice))
              .collect(Collectors.toList());
   }
}
