package com.example.identityservice.exceptionHandler;

import com.example.identityservice.custom.CustomBadResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class MyControllerAdvice{

    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<?> userNotFound(UsernameNotFoundException usernameNotFoundException){
        CustomBadResponse customBadResponse =
                new CustomBadResponse(usernameNotFoundException.getMessage(), HttpStatus.NOT_FOUND);
        return ResponseEntity.status(404).body(customBadResponse);
    }



    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleException(Exception ex) {
        System.out.println("捕獲異常：" + ex.getMessage());
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

}
