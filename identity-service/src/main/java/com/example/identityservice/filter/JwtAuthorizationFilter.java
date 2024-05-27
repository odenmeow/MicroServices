package com.example.identityservice.filter;

import com.example.identityservice.service.JwtService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;


// 此抽象方法 如果有會話 (持有JSESSION 就不會執行了) 請禁用session
public class JwtAuthorizationFilter extends OncePerRequestFilter {
    private JwtService jwtService = new JwtService();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String token = request.getHeader("Authorization");

        if (token != null && token.startsWith("Bearer ")) {
            System.out.println("JWTFILTER:印出....有jwt " + new Date().toString() + request.getRequestURI());
            System.out.flush();
            Claims claims = jwtService.extractClaims(token.substring(7));
            try {
                Object rolesObject = claims.getOrDefault("roles", Collections.emptyList());
                List<Map<String, String>> roles = rolesObject instanceof List ?
                        (List<Map<String, String>>) rolesObject : Collections.emptyList();

                System.out.println("印出角色" + roles);
                System.out.println(claims.getSubject());

                // 轉換 roles 為 GrantedAuthority
                List<GrantedAuthority> authorities = convertToAuthorities(roles);

                // 創建 PreAuthenticatedAuthenticationToken 對象
                PreAuthenticatedAuthenticationToken authenticationToken =
                        new PreAuthenticatedAuthenticationToken(claims.getSubject(), null, authorities);
                authenticationToken.setAuthenticated(true);  // 確保設置為已認證
                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                System.out.println("成功設定虛假認證");
                System.out.println("SecurityContextHolder 中的認證：" + SecurityContextHolder.getContext().getAuthentication());
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("roles 物件 無法轉換為 List<GrantedAuthority>");
                System.out.flush();
            }
        }

        filterChain.doFilter(request, response);
    }

    private List<GrantedAuthority> convertToAuthorities(List<Map<String, String>> roles){
        List<GrantedAuthority> authorities = new ArrayList<>();
        for ( var role : roles){
            authorities.add(new SimpleGrantedAuthority(role.get("authority")));
        }
        return authorities;
    }

}