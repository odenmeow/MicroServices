package com.example.restaurantservice.filter;

import com.example.restaurantservice.config.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;

import java.io.IOException;
import java.util.*;


public class JwtAuthorizationFilter extends AbstractPreAuthenticatedProcessingFilter {

    private JwtUtil jwtService = new JwtUtil();

    public JwtAuthorizationFilter() {
        // 空的操作，什麼都不做 :D。
        setAuthenticationManager(authentication -> authentication);
    }

    @Override
    protected Object getPreAuthenticatedPrincipal(HttpServletRequest request) {
        String token = request.getHeader("Authorization");

        if (token != null && token.startsWith("Bearer ")) {
            System.out.println("JWTFILTER:印出....有jwt" +
                    new Date() +
                    request.getRequestURI());
            System.out.flush();
            Claims claims = jwtService.extractClaims(token.substring(7));
            return claims.getSubject(); // 返回主體，即用戶名或用戶ID
        }

        return null;
    }

    @Override
    protected Object getPreAuthenticatedCredentials(HttpServletRequest request) {
        return null; // 在預認證場景中，通常不需要額外的憑證
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, Authentication authResult) throws IOException, ServletException {
        // 從 Authentication 提取訊息 (使用預設<如果沒token>)。
        PreAuthenticatedAuthenticationToken authenticationToken = (PreAuthenticatedAuthenticationToken) authResult;
        String token = request.getHeader("Authorization");

        System.out.println("印出"+request.getRemoteHost());
        if (token != null && token.startsWith("Bearer ")) {
            Claims claims = jwtService.extractClaims(token.substring(7));
            List<GrantedAuthority> authorities = convertToAuthorities(claims);
            authenticationToken = new PreAuthenticatedAuthenticationToken(claims.getSubject(), null, authorities);
            authenticationToken.setAuthenticated(true);
            SecurityContextHolder.getContext().setAuthentication(authenticationToken);
        }

        super.successfulAuthentication(request, response, authenticationToken);
    }

    private List<GrantedAuthority> convertToAuthorities(Claims claims) {
        Object rolesObject = claims.getOrDefault("roles", Collections.emptyList());
        List<Map<String, String>> roles = rolesObject instanceof List ?
                (List<Map<String, String>>) rolesObject : Collections.emptyList();

        List<GrantedAuthority> authorities = new ArrayList<>();
        for (Map<String, String> role : roles) {
            authorities.add(new SimpleGrantedAuthority(role.get("authority")));
        }
        return authorities;
    }
}
