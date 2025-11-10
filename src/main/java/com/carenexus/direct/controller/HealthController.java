package com.carenexus.direct.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class HealthController {
    @GetMapping("/api/health")
    public Map<String, String> health() {
        return Map.of("status", "UP");
    }

    @GetMapping("/api/test")
    public Map<String, String> test() {
        return Map.of("message", "CareNexus Direct API is running!");
    }
}
