package com.springboot.dashboard.controller;

import com.springboot.dashboard.service.DashBoardService;
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
@RequiredArgsConstructor
public class DashBoardController {
        private final DashBoardService dashBoardService;


    @GetMapping
    public ResponseEntity getDashBoard() {

        return new ResponseEntity<>(dashBoardService.findVerifiedExistsDashBoard(), HttpStatus.OK);
    }
}
