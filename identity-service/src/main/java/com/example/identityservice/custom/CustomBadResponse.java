package com.example.identityservice.custom;

import lombok.*;
import org.springframework.http.HttpStatus;
import org.springframework.validation.Errors;
import org.springframework.validation.FieldError;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
public class CustomBadResponse {
    @Getter(AccessLevel.NONE)
    @Builder.Default // 不使用 則 建造者模式 不會以下面的方式 初始化
    private String timestamp = String.valueOf(LocalDateTime.now());
    private String status;
    private String error;
    private Object message;
    private Object path;
    public CustomBadResponse(Object message, HttpStatus status){
        this.message = message;
        this.path = ServletUriComponentsBuilder.fromCurrentRequestUri().build().getPath();
        this.status = String.valueOf(status.value());
        this.error = status.getReasonPhrase();
    }
    // 自己設定getter 就可以避開builder不設定會產出null的問題
    /** 如果timestamp 是 null 使用預設值 ，不是null代表被設定過 所以可以直接使用 */
    public String getTimestamp() {
        return timestamp == null ? String.valueOf(LocalDateTime.now()) : timestamp;
    }
    public static CustomBadResponse customErrorMsg(Errors errors, HttpStatus status){
        Map<String, Object> map = new HashMap<>();
        for(FieldError e : errors.getFieldErrors()){
            map.put(e.getField(), e.getDefaultMessage());
        }
        return new CustomBadResponse(map, status);
    }
}
