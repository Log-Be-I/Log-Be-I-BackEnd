package com.springboot.config;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class SpaController {

    @RequestMapping(value = {
            "/",
            "/{path:^(?!api|v3|swagger-ui|swagger-resources|webjars|assets|favicon\\.png|.*\\..*).*$}/**"
    })
    public String forward() {
        return "forward:/index.html";
    }
}
