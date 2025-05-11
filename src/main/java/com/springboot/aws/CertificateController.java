package com.springboot.aws;

import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
@Hidden
@RestController
public class CertificateController {

    @GetMapping("/hello")
    public ResponseEntity forAwsCertificate () {
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
