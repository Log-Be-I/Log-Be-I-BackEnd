package com.springboot.aws;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CertificateController {

    @GetMapping("/hello")
    public String forAwsCertificate () {
        return "hello";
    }
}
