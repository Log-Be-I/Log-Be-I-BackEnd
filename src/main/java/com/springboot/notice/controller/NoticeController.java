package com.springboot.notice.controller;

import com.springboot.auth.utils.CustomPrincipal;
import com.springboot.notice.service.NoticeService;
import com.springboot.notice.dto.NoticeDto;
import com.springboot.notice.entity.Notice;
import com.springboot.notice.mapper.NoticeMapper;
import com.springboot.responsedto.MultiResponseDto;
import com.springboot.responsedto.SingleResponseDto;
import com.springboot.utils.UriCreator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
@Tag(name = "Notice API", description = "공지사항 관련 API")
public class NoticeController {
    private final static String NOTICE_DEFAULT_URL = "/notice";
    private final NoticeService noticeService;
    private final NoticeMapper mapper;

    //swagger API - 등록
    @Operation(summary = "공지사항 등록", description = "공지사항을 새로 등록합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "새로운 공지 등록"),
            @ApiResponse(responseCode = "403", description = "잘못된 권한 접근",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"error\": \"Forbidden\", \"message\": \"작성 권한이 없습니다.\"}")))
    })

    @PostMapping
    public ResponseEntity postNotice(@Valid @RequestBody NoticeDto.Post post,
                                     @AuthenticationPrincipal CustomPrincipal customPrincipal){
        long memberId = customPrincipal.getMemberId();
        Notice createdNotice = noticeService.createNotice(mapper.noticePostToNotice(post), memberId);
        URI location = UriCreator.createUri(NOTICE_DEFAULT_URL, createdNotice.getNoticeId());

        return ResponseEntity.created(location).build();
    }

    //swagger API - 수정
    @Operation(summary = "공지사항 수정", description = "기존에 등록된 공지 글을 수정합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "기존에 등록된 공지 글 수정 성공",
                    content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation =  NoticeDto.Response.class))),
            @ApiResponse(responseCode = "403", description = "잘못된 권한 접근",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"error\": \"Forbidden\", \"message\": \"작성 권한이 없습니다.\"}"))),
            @ApiResponse(responseCode = "404", description = "찾을 수 없는 공지",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"error\": \"Not Found\", \"message\": \"NOTICE_NOT_FOUND.\"}")))
    })

    @PatchMapping("/{notice-id}")
    public ResponseEntity patchNotice(@Valid @RequestBody NoticeDto.Patch patch,
                                      @PathVariable("notice-id") @Positive long noticeId,
                                      @AuthenticationPrincipal CustomPrincipal customPrincipal) {
        long memberId = customPrincipal.getMemberId();
        //수정할 notice
        patch.setNoticeId(noticeId);
        Notice updateNotice = noticeService.updateNotice(mapper.noticePatchToNotice(patch), memberId);
        return new ResponseEntity<>(new SingleResponseDto<>(
                mapper.noticeToNoticeResponse(updateNotice)), HttpStatus.OK);
    }
    //swagger API - 상세 조회
    @Operation(summary = "공지사항 상세 조회", description = "등록된 공지 글을 상세 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "공지 글 상세 조회",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation =  NoticeDto.Response.class))),
            @ApiResponse(responseCode = "401", description = "유효한 인증 자격 증명이 없습니다",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"error\": \"Unauthorized\", \"message\": \"Your session has expired. Please log in again to continue.\"}"))),
            @ApiResponse(responseCode = "404", description = "찾을 수 없는 공지",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"error\": \"Not Found\", \"message\": \"NOTICE_NOT_FOUND.\"}")))
    })

    @GetMapping("/{notice-id}")
    public ResponseEntity getNotice(@PathVariable("notice-id") @Positive long noticeId) {
        Notice findNotice = noticeService.findNotice(noticeId);
        return new ResponseEntity<>(new SingleResponseDto<>(
                mapper.noticeToNoticeResponse(findNotice)), HttpStatus.OK
        );
    }

    //swagger API - 상세 조회
    @Operation(summary = "공지사항 전체 목록 조회", description = "등록된 공지 목록을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "공지 글 전체 조회",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation =  NoticeDto.Response.class))),
            @ApiResponse(responseCode = "401", description =  "유효한 인증 자격 증명이 없습니다",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"error\": \"Unauthorized\", \"message\": \"Your session has expired. Please log in again to continue.\"}"))),
            @ApiResponse(responseCode = "404", description = "찾을 수 없는 공지",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"error\": \"Not Found\", \"message\": \"NOTICE_NOT_FOUND.\"}")))
    })
    @GetMapping
    public ResponseEntity getNotices(@RequestParam @Positive int page,
                                     @RequestParam @Positive int size) {
        Page<Notice> noticePage = noticeService.findNotices(page, size);
        List<Notice> notices = noticePage.getContent();

        return new ResponseEntity<>(new MultiResponseDto<>(mapper.noticesToNoticeResponses(notices), noticePage),
                HttpStatus.OK);
    }


    //swagger API - 삭제
    @Operation(summary = "공지사항 삭제", description = "등록된 공지 글을 삭제 합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "공지 글 삭제 성공"),
            @ApiResponse(responseCode = "403", description = "잘못된 권한 접근",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"error\": \"Forbidden\", \"message\": \"삭제 권한이 없습니다.\"}"))),
            @ApiResponse(responseCode = "404", description = "찾을 수 없는 공지",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"error\": \"Not Found\", \"message\": \"NOTICE_NOT_FOUND.\"}")))
    })

    @DeleteMapping("/{notice-id}")
    public ResponseEntity deleteNotice(@PathVariable("notice-id") @Positive long noticeId,
                                       @AuthenticationPrincipal CustomPrincipal customPrincipal){

        long memberId = customPrincipal.getMemberId();
        noticeService.deleteNotice(noticeId, memberId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
