package com.example.restaurantservice.filter;

import com.example.restaurantservice.config.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class JwtAuthorizationFilter extends AbstractPreAuthenticatedProcessingFilter {
    public JwtAuthorizationFilter(){
        // 空的操作，什麼都不做 :D。
        setAuthenticationManager(authentication -> authentication);
    }
    @Override // 簡單取得 header jwt 就好，使用這個。 返回 代表用戶的 Principal 對象
    protected Object getPreAuthenticatedPrincipal(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            Claims claims = JwtUtil.extractClaims(token.substring(7));
            try{
                List<String> roles = claims.containsKey("roles") ?
                    (List<String>) claims.get("roles") : Collections.emptyList();

                // 假的 提供 給 spring，僅 (身份) 、 ( username == email 或稱 principal)
                UsernamePasswordAuthenticationToken authenticationToken =
                        new UsernamePasswordAuthenticationToken(
                                claims.getSubject(),
                                null, // 不須密碼
                                convertToAuthorities(roles) // 我只想要權限
                        );
                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                return  authenticationToken;

            }catch (Exception e){
                e.printStackTrace();
                System.out.println("roles 物件 無法轉換為 List<String>");
            }
        }
        return null;
    }

    @Override // 取得 證書(Oauth之類、密碼、其他種令牌，以複雜方式自訂議提取)
    protected Object getPreAuthenticatedCredentials(HttpServletRequest request) {
        return null;
    }

    private List<GrantedAuthority> convertToAuthorities(List<String> roles){
        List<GrantedAuthority> authorities = new ArrayList<>();
        for ( var role : roles){
            authorities.add(new SimpleGrantedAuthority(role));
        }
        return authorities;
    }


}