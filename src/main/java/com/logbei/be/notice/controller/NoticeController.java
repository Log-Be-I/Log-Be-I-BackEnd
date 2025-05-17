package com.logbei.be.notice.controller;

import com.logbei.be.auth.utils.CustomPrincipal;
import com.logbei.be.notice.dto.NoticePatchDto;
import com.logbei.be.notice.dto.NoticePostDto;
import com.logbei.be.notice.dto.NoticeResponseDto;
import com.logbei.be.notice.service.NoticeService;
import com.logbei.be.notice.dto.NoticeDto;
import com.logbei.be.notice.entity.Notice;
import com.logbei.be.notice.mapper.NoticeMapper;
import com.logbei.be.responsedto.MultiResponseDto;
import com.logbei.be.responsedto.SingleResponseDto;
import com.logbei.be.swagger.SwaggerErrorResponse;
import com.logbei.be.utils.UriCreator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/notices")
@Validated
@RequiredArgsConstructor
@Tag(name = "Notice API", description = "공지사항 관련 API")
public class NoticeController {
    private final static String NOTICE_DEFAULT_URL = "/notices";
    private final NoticeService noticeService;
    private final NoticeMapper mapper;

    //swagger API - 등록
    @Operation(summary = "공지사항 등록", description = "공지사항을 새로 등록합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "새로운 공지 등록",
                    content = @Content(schema = @Schema(implementation = NoticeResponseDto.class))),
            @ApiResponse(responseCode = "401", description = "유효한 인증 자격 증명이 없습니다.",
                    content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class),
                            examples = @ExampleObject(value = "{\"error\": \"UNAUTHORIZED\", \"message\": \"Unauthorized\"}"))),
            @ApiResponse(responseCode = "403", description = "잘못된 권한 접근",
                    content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class),
                            examples = @ExampleObject(value = "{\"error\": \"FORBIDDEN\", \"message\": \"작성 권한이 없습니다.\"}")))
    })

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity postNotice(@RequestPart("noticePostDto") NoticePostDto noticePostDto,
        // @Valid @RequestBody NoticePostDto noticePostDto,
                                     @RequestPart(value = "images", required = false) List<MultipartFile> images,
                                     @Parameter(hidden = true) @AuthenticationPrincipal CustomPrincipal customPrincipal){

        noticePostDto.setMemberId(customPrincipal.getMemberId());
        Notice createdNotice = noticeService.createNotice(mapper.noticePostToNotice(noticePostDto), customPrincipal.getMemberId(), images);
        URI location = UriCreator.createUri(NOTICE_DEFAULT_URL, createdNotice.getNoticeId());

        return ResponseEntity.created(location).body(new SingleResponseDto<>(mapper.noticeToNoticeResponse(createdNotice)));
    }

    //swagger API - 수정
    @Operation(summary = "공지사항 수정", description = "기존에 등록된 공지 글을 수정합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "기존에 등록된 공지 글 수정 성공",
                    content = @Content(schema = @Schema(implementation = NoticeResponseDto.class))),
            @ApiResponse(responseCode = "401", description = "유효한 인증 자격 증명이 없습니다.",
                    content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class),
                            examples = @ExampleObject(value = "{\"error\": \"UNAUTHORIZED\", \"message\": \"Unauthorized\"}"))),
            @ApiResponse(responseCode = "404", description = "찾을 수 없는 공지",
                    content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class),
                            examples = @ExampleObject(value = "{\"error\": \"NOT_FOUND\", \"message\": \"Not Found\"}")))
    })

    @PatchMapping(value = "/{notice-id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity patchNotice(@RequestPart("noticePatchDto") NoticePatchDto noticePatchDto,
                                      // @Valid @RequestBody NoticePatchDto noticePatchDto,
                                      @PathVariable("notice-id") @Positive long noticeId,
                                      @RequestPart(value = "images", required = false) List<MultipartFile> images,
                                      @AuthenticationPrincipal CustomPrincipal customPrincipal) {
        //수정할 notice
        noticePatchDto.setNoticeId(noticeId);
        Notice notice =  noticeService.updateNotice(mapper.noticePatchToNotice(noticePatchDto), customPrincipal.getMemberId(), images);
        return new ResponseEntity<>(new SingleResponseDto<>(
                mapper.noticeToNoticeResponse(notice)), HttpStatus.OK);
    }

    //swagger API - 상세 조회
    @Operation(summary = "공지사항 상세 조회", description = "등록된 공지 글을 상세 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "공지 글 상세 조회",
                    content = @Content(schema = @Schema(implementation = NoticeResponseDto.class))),
            @ApiResponse(responseCode = "401", description = "유효한 인증 자격 증명이 없습니다.",
                    content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class),
                            examples = @ExampleObject(value = "{\"error\": \"UNAUTHORIZED\", \"message\": \"Unauthorized\"}"))),
            @ApiResponse(responseCode = "404", description = "찾을 수 없는 공지",
                    content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class),
                            examples = @ExampleObject(value = "{\"error\": \"NOT_FOUND\", \"message\": \"Not Found\"}")))
    })
    @GetMapping("/{notice-id}")
    public ResponseEntity getNotice(@Parameter(description = "notice-id", example = "1")
                                        @PathVariable("notice-id") @Positive long noticeId) {

        Notice notice = noticeService.findNotice(noticeId);
        return new ResponseEntity<>(new SingleResponseDto<>(
                mapper.noticeToNoticeResponse(notice)), HttpStatus.OK
        );
    }

    //swagger API - 전체 조회
    @Operation(summary = "공지사항 전체 목록 조회", description = "등록된 공지 목록을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "공지 글 전체 조회",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = NoticeResponseDto.class)))),
            @ApiResponse(responseCode = "401", description = "유효한 인증 자격 증명이 없습니다.",
                    content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class),
                            examples = @ExampleObject(value = "{\"error\": \"UNAUTHORIZED\", \"message\": \"Unauthorized\"}"))),
            @ApiResponse(responseCode = "404", description = "찾을 수 없는 공지",
                    content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class),
                            examples = @ExampleObject(value = "{\"error\": \"NOT_FOUND\", \"message\": \"Not Found\"}")))
    })
    @GetMapping
    public ResponseEntity getNotices(@Parameter(description = "page", example = "1")
                                         @RequestParam @Positive int page,
                                     @Parameter(description = "size", example = "10")
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
            @ApiResponse(responseCode = "204", description = "공지 글 삭제 성공",
                    content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class),
                            examples = @ExampleObject(value = "{\"status\": \"NO_CONTENT\", \"message\": \"DELETED_DONE\"}"))),
            @ApiResponse(responseCode = "401", description = "유효한 인증 자격 증명이 없습니다.",
                    content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class),
                            examples = @ExampleObject(value = "{\"error\": \"UNAUTHORIZED\", \"message\": \"Unauthorized\"}"))),
            @ApiResponse(responseCode = "404", description = "찾을 수 없는 공지",
                    content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class),
                            examples = @ExampleObject(value = "{\"error\": \"NOT_FOUND\", \"message\": \"Not Found\"}")))
    })
    @DeleteMapping("/{notice-id}")
    public ResponseEntity deleteNotice(@PathVariable("notice-id") @Positive long noticeId,
                                       @Parameter(hidden = true) @AuthenticationPrincipal CustomPrincipal customPrincipal){

        noticeService.deleteNotice(noticeId, customPrincipal.getMemberId());
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
