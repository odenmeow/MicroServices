package com.example.identityservice.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Date;
import java.util.regex.Pattern;

public class MyFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        Pattern PATTERN = Pattern.compile("/actuator/health");

        if( PATTERN.matcher(request.getRequestURI()).find()) {
            filterChain.doFilter(request, response);
            // doFilter 是把請求傳給別人，但後續指令還是會 muti-thread 執行完畢 !
            // 條件要寫清楚!!!
            return;
        }
        System.out.println("MyFilter:印出:"+ new Date().toString() + request.getRequestURI());
        System.out.flush();
        filterChain.doFilter(request,response);
    }

}
