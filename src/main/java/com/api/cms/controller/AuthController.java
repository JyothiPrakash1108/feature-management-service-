package com.api.cms.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.api.cms.dto.auth.LoginRequestDTO;
import com.api.cms.exception.UserDoesNotExistException;
import com.api.cms.service.AuthService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private static final Logger log = LoggerFactory.getLogger(AuthController.class);
    private AuthenticationManager authenticationManager;
    private AuthService authService;
    public AuthController(AuthenticationManager authenticationManager, AuthService authService) {
        this.authenticationManager = authenticationManager;
        this.authService = authService;
    }
    @PostMapping("/login")
    public String authenticate(@Valid @RequestBody LoginRequestDTO requestDTO) throws UserDoesNotExistException {

        String username = requestDTO.getEmail();
        String password = requestDTO.getPassword();
        log.info("Login request username:{}",username);
        Authentication auth = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(username, password)
        );
        if(auth.isAuthenticated()) {
            log.info("Login successfull for the user userName:{}",username);
            return authService.generateJwtToken(username);
        }
        return "signin failed";
    }
}
