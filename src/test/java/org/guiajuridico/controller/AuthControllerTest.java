package org.guiajuridico.controller;

import org.guiajuridico.dto.LoginRequestDto;
import org.guiajuridico.dto.LoginResponseDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.ResponseEntity;
import org.springframework.boot.test.web.client.TestRestTemplate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AuthControllerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void login_withSeededAdmin_returnsToken() {
        String url = "http://localhost:" + port + "/api/auth/login";

        LoginRequestDto request = new LoginRequestDto();
        request.setEmail("admin@guiajuridico.com");
        request.setSenha("admin123");

        ResponseEntity<LoginResponseDto> response = restTemplate.postForEntity(url, request, LoginResponseDto.class);

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().getToken());
        assertFalse(response.getBody().getToken().isEmpty());
    }
}