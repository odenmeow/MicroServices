package com.example.identityservice.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Data
@Configuration
@ConfigurationProperties(prefix = "security")
public class SecurityConfigProperties {
    private List<String> whiteList = new ArrayList<>();
    private List<RoleMapping> roleMappings = new ArrayList<>();

    @Data
    public static class RoleMapping {
        private String path;
        private List<MethodRoles> methods = new ArrayList<>();

        @Data
        public static class MethodRoles {
            private String method;
            private List<String> anyRole = new ArrayList<>();
            private List<String> matchAllRoles = new ArrayList<>();
        }
    }
}