package com.logbei.be.dashboard.controller;

import com.logbei.be.swagger.SwaggerErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.logbei.be.auth.utils.CustomPrincipal;
import com.logbei.be.dashboard.dto.DashBoardResponseDto;
import com.logbei.be.dashboard.service.DashBoardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/offices")
@Validated
@Tag(name = "대시보드 api", description = "관리자 페이지 메인 화면 작성")
@RequiredArgsConstructor
public class DashBoardController {
        private final DashBoardService dashBoardService;
//    INVALID_SERVER_ERROR
    @Operation(summary = "대시보드", description = "대시보드 기본 정보 조회")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "대시보드 기본 정보 조회",
                    content = @Content(schema = @Schema(implementation = DashBoardResponseDto.class))),
            @ApiResponse(responseCode = "401", description = "유효한 인증 자격 증명이 없습니다.",
                    content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class),
                            examples = @ExampleObject(value = "{\"error\": \"UNAUTHORIZED\", \"message\": \"Unauthorized\"}"))),
    })
    @GetMapping
    public ResponseEntity getDashBoard() {

        return new ResponseEntity<>(dashBoardService.findVerifiedExistsDashBoard(), HttpStatus.OK);
    }
}
