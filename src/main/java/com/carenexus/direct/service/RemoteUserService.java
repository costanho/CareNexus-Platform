package com.carenexus.direct.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class RemoteUserService {

    private final RestTemplate restTemplate;

    public Long getUserIdByEmail(String email) {
        String url = "http://auth-service:8081/api/auth/user-id?email=" + email;

        return restTemplate.getForObject(url, Long.class);
    }
}
