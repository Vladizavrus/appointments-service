package com.example.appointments.dto;

import org.springframework.http.HttpStatusCode;

import java.time.LocalDateTime;

public class ExceptionDTO {
    private int code;
    private String message;
    private LocalDateTime time;

    public ExceptionDTO(int code, String message) {
        this.code = code;
        this.message = message;
        this.time = LocalDateTime.now();
    }

    public LocalDateTime getTime() {
        return time;
    }

    public String getMessage() {
        return message;
    }

    public int getCode() {
        return code;
    }
}
