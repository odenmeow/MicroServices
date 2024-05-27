package com.example.identityservice.config;

import com.example.identityservice.entity.Role;
import com.example.identityservice.entity.UserCredential;
import com.example.identityservice.repository.UserCredentialRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;


@Component
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserCredentialRepository repository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        System.out.println("印出測試看看有沒有loadUser");
        Optional<UserCredential> credential = repository.findByEmail(username);
        UserCredential member = credential
                .orElseThrow(
                        () -> new UsernameNotFoundException("user not found with name :" + username));
        List<GrantedAuthority> authorityList = Collections.emptyList();
        // CustomUserDetails 內，有做自動轉換 roles 為 目標格式。
        // return new User(member.getName(), member.getPassword(), authorityList);

        // 但這個作者選擇 implements 。所以看起來不一樣 :D
        return credential.map(CustomUserDetails::new)
                .orElseThrow(()->new UsernameNotFoundException("user not found with name :" + username));
    }


}
