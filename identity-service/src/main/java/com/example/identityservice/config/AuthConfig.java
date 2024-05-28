package com.example.identityservice.config;

import com.example.identityservice.filter.AuthenticationLoggerFilter;
import com.example.identityservice.filter.JwtAuthorizationFilter;
import com.example.identityservice.filter.MyFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.servletapi.SecurityContextHolderAwareRequestFilter;

@Configuration
@EnableWebSecurity
public class AuthConfig {

//    作者似乎 重複註冊了 不知道在幹嘛= =
//    類別 CustomUserDetailsService 本身早已使用 @Component 向 spring容器 登記自己了。

//    後續 :  因為 credential.map(CustomUserDetails::new) 想要 new 使用自訂建構子，需要兩參數  帳 + 密。
//          如果使用 @Component 則會去替 建構子尋找 UserCredential 是否為 bean 想注入，卻找不到。
//    乾脆都不要使用 反正它也是使用 new 來做。  好像沒必要做成bean 。
//    @Bean
//    public UserDetailsService userDetailsService(){
//        return new CustomUserDetailsService();
//    }

    @Autowired
    private CustomUserDetailsService customUserDetailsService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        return httpSecurity
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // 禁用會話管理
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/auth/register", "/auth/token", "/auth/validate").permitAll()
                        // 像我打錯了 auth 打成 atuh 就得到403
                        // 下面這個是 for Consul 的健康檢查使用，否則全部403都不能訪問= =
                        .requestMatchers("/actuator/health").permitAll()
//                        .requestMatchers("/auth/check/roles/**").permitAll()
                        .requestMatchers("/auth/addRole/**").permitAll()
                        // 讓錯誤印出 而不是僅僅403
                        .requestMatchers("/error").permitAll()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(new MyFilter(), UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(new JwtAuthorizationFilter(), UsernamePasswordAuthenticationFilter.class)
//                .addFilterAfter(new AuthenticationLoggerFilter(), SecurityContextHolderAwareRequestFilter.class)
                // 下面這個是帳號密碼的驗證，如果 authenticated就要認帳密，JWT 會被禁止，無法通過
                .userDetailsService(customUserDetailsService).build();

    }

    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }

    // 為了更靈活的使用 Authentication，有些地方可能需要 AuthenticationManager，所以才做。
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public AuthenticationProvider authenticationProvider(){
        DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider();
        authenticationProvider.setUserDetailsService(customUserDetailsService);
        authenticationProvider.setPasswordEncoder(passwordEncoder());
        return authenticationProvider;
    }

}
