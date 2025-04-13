package com.springboot.notice.controller;

import com.springboot.notice.NoticeService;
import com.springboot.notice.dto.NoticeDto;
import com.springboot.notice.entity.Notice;
import com.springboot.notice.mapper.NoticeMapper;
import com.springboot.responsedto.MultiResponseDto;
import com.springboot.responsedto.SingleResponseDto;
import com.springboot.utils.UriCreator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/notices")
@Validated
@RequiredArgsConstructor
public class NoticeController {
    private final static String NOTICE_DEFAULT_URL = "/notice";
    private final NoticeService noticeService;
    private final NoticeMapper mapper;

    @PostMapping
    public ResponseEntity postNotice(@Valid @RequestBody NoticeDto.Post post){
        Notice createdNotice = noticeService.createNotice(mapper.noticePostToNotice(post));
        URI location = UriCreator.createUri(NOTICE_DEFAULT_URL, createdNotice.getNoticeId());

        return ResponseEntity.created(location).build();
    }

    @PatchMapping("/{notice-id}")
    public ResponseEntity patchNotice(@Valid @RequestBody NoticeDto.Patch patch,
                                      @PathVariable("notice-id") @Positive long noticeId) {
        //수정할 notice
        patch.setNoticeId(noticeId);
        Notice updateNotice = noticeService.updateNotice(mapper.noticePatchToNotice(patch));
        return new ResponseEntity<>(new SingleResponseDto<>(
                mapper.noticeToNoticeResponse(updateNotice)), HttpStatus.OK);
    }

    @GetMapping("/{notice-id}")
    public ResponseEntity getNotice(@PathVariable("notice-id") @Positive long noticeId) {
        Notice findNotice = noticeService.findNotice(noticeId);
        return new ResponseEntity<>(new SingleResponseDto<>(
                mapper.noticeToNoticeResponse(findNotice)), HttpStatus.OK
        );
    }

    @GetMapping
    public ResponseEntity getNotices(@RequestParam @Positive int page,
                                     @RequestParam @Positive int size) {
        Page<Notice> noticePage = noticeService.findNotices(page, size);
        List<Notice> notices = noticePage.getContent();

        return new ResponseEntity<>(new MultiResponseDto<>(mapper.noticesToNoticeResponses(notices), noticePage),
                HttpStatus.OK);
    }

    @DeleteMapping("/{notice-id}")
    public ResponseEntity deleteNotice(@PathVariable("notice-id") @Positive long noticeId){
        noticeService.deleteNotice(noticeId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
