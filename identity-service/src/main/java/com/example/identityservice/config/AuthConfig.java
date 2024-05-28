package com.example.identityservice.config;

import com.example.identityservice.filter.AuthenticationLoggerFilter;
import com.example.identityservice.filter.JwtAuthorizationFilter;
import com.example.identityservice.filter.MyFilter;
import org.apache.catalina.security.SecurityConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
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

import java.sql.SQLOutput;
import java.util.Arrays;

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


    private final SecurityConfigProperties securityConfigProperties;

    private final CustomUserDetailsService customUserDetailsService;


    public AuthConfig(SecurityConfigProperties securityConfigProperties,
                          CustomUserDetailsService customUserDetailsService){
        this.securityConfigProperties = securityConfigProperties;
        this.customUserDetailsService = customUserDetailsService;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        return httpSecurity
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // 禁用會話管理
                .authorizeHttpRequests(auth -> {
                    // 設置白名單
                    for (String path : securityConfigProperties.getWhiteList()){
                        auth.requestMatchers(path).permitAll();
                    }
                    // 角色 - API 映射
                    // 設置角色映射路由和方法
                    for (SecurityConfigProperties.RoleMapping roleMapping : securityConfigProperties.getRoleMappings()) {
                        for (SecurityConfigProperties.RoleMapping.MethodRoles methodRole : roleMapping.getMethods()) {
                            System.out.println("URL" + roleMapping.getPath());
                            if (!methodRole.getAnyRole().isEmpty()) {
                                String[] rolesArray = methodRole.getAnyRole().toArray(new String[0]);
                                System.out.println("任何ROLE : " + Arrays.toString(rolesArray));
                                auth.requestMatchers(
                                        HttpMethod.valueOf(methodRole.getMethod()),
                                        roleMapping.getPath()
                                        ).hasAnyRole(rolesArray);
                            }
                            if (!methodRole.getMatchAllRoles().isEmpty()) {
                                String[] rolesArray = methodRole.getMatchAllRoles().toArray(new String[0]);
                                System.out.println("All Match : " + Arrays.toString(rolesArray));
                                for (String role : methodRole.getMatchAllRoles()){
                                    auth.requestMatchers(
                                            HttpMethod.valueOf(methodRole.getMethod()),
                                            roleMapping.getPath()
                                    ).hasRole(role);
                                }
                            }
                        }
                    }

                    // 其他所有路由都需要認證
                    auth.anyRequest().authenticated();

                })
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
