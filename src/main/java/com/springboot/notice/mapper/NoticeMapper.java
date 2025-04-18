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
   NoticeDto.Response noticeToNoticeResponse(Notice notice);
   List<NoticeDto.Response> noticesToNoticeResponses(List<Notice> notices);
}
