package com.springboot.dashboard.controller;

import com.springboot.auth.utils.CustomPrincipal;
import com.springboot.dashboard.dto.DashBoardResponseDto;
import com.springboot.dashboard.service.DashBoardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/offices")
@Validated
@RequiredArgsConstructor
public class DashBoardController {
        private final DashBoardService dashBoardService;


    @GetMapping
    public ResponseEntity getDashBoard(@AuthenticationPrincipal CustomPrincipal customPrincipal) {

        DashBoardResponseDto dto = dashBoardService.findDashBoard();
        return new ResponseEntity<>(dto, HttpStatus.OK);
    }
}
