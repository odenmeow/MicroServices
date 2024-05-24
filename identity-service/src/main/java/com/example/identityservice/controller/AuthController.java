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

import java.util.Collections;

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

    @PostMapping("/addRole/{role}/{vipCode}")
    public String addRole(@PathVariable("role") String role,
                            @PathVariable("vipCode") String vipCode,
                            Authentication authentication){
        if ("vip777".equals(vipCode)){
            authentication.getName();
            return "Success";
        } else {
            return "Failed";
        }
    }

    @PostMapping("/token") // 透過帳密 取得 token
    public String getToken(@RequestBody AuthRequest authRequest){

        try {
            // 不需要真的驗證帳密 ( token 是 公開訪問的 ， 也不會真的驗證 )
            // 亂打，也會成功。  【Ref:Restaurant > JwtFilter】
            // Authentication authentication = new UsernamePasswordAuthenticationToken(
            //        authRequest.getUsername(),authRequest.getPassword(), Collections.emptyList()
            // ) ;

            // ---------------------------------
            // 真的有驗證 ( 以下為手動驗證並生成的方式 )
            Authentication authentication = authenticationManager
                    .authenticate(new UsernamePasswordAuthenticationToken(
                            authRequest.getUsername(), authRequest.getPassword()));

            if (authentication.isAuthenticated()) {
                System.out.println("印出:::"+ authentication.getPrincipal());
                System.out.println("印出:::"+ authentication.getName()); // 改為是 oni@gmail.com 了
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
