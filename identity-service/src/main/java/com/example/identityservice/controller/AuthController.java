package com.example.identityservice.controller;

import com.example.identityservice.dto.AuthRequest;
import com.example.identityservice.entity.UserCredential;
import com.example.identityservice.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {
    @Autowired
    private AuthService service;

    @Autowired
    private AuthenticationManager authenticationManager;

    @PostMapping("/register")
    public String addNewUser(@RequestBody UserCredential user){
        return service.saveUser(user);
    }

    @PostMapping("/token") // 透過帳密 取得 token
    public String getToken(@RequestBody AuthRequest authRequest){

        try {
            Authentication authentication = authenticationManager
                    .authenticate(new UsernamePasswordAuthenticationToken(
                            authRequest.getUsername(), authRequest.getPassword()));

            if (authentication.isAuthenticated()) {
                System.out.println("印出:::"+ authentication.getPrincipal());
                System.out.println("印出:::"+ authentication.getName());
                System.out.println("印出:::"+ authentication.getAuthorities());
                System.out.println("印出:::"+ authentication.getCredentials());
                return service.generateToken(authRequest.getUsername());
            } else {
                throw new RuntimeException("Invalid access");
            }
        } catch (AuthenticationException e) {
            e.printStackTrace();
            throw new RuntimeException("Invalid access", e);
        }
    }

    @GetMapping("/validate") // 主動驗證看看 JWT 有效性:D
    public String validateToken(@RequestParam("token") String token){
        service.validateToken(token);
        return "Token is valid";
    }

}
