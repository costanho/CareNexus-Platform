package com.carenexus.direct.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/direct")
public class TestController {

    @GetMapping("/secure")
    public String secureEndpoint() {
        return "Secure Direct-Service endpoint accessed!";
    }
}

