package com.springboot.notice.mapper;


import com.springboot.notice.dto.NoticeDto;
import com.springboot.notice.entity.Notice;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface NoticeMapper {
   @Mapping(target = "member.memberId", source = "memberId")
   Notice noticePostToNotice(NoticeDto.Post post);
   Notice noticePatchToNotice(NoticeDto.Patch patch);
   @Mapping(target = "memberId", source = "member.memberId")
//   NoticeDto.Response noticeToNoticeResponse(Notice notice);

   default NoticeDto.Response noticeToNoticeResponse(Notice notice){
      NoticeDto.Response response = new NoticeDto.Response(
              notice.getNoticeId(),
              notice.getTitle(),
              notice.getContent(),
              notice.getImage(),
              notice.getMember().getMemberId(),
              notice.getNoticeType(),
              notice.getNoticeStatus(),
              notice.getIsPinned(),
              notice.getCreatedAt()
      );
      return response;
   }

   List<NoticeDto.Response> noticesToNoticeResponses(List<Notice> notices);
}
