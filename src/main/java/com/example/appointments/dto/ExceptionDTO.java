package com.example.appointments.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import org.springframework.http.HttpStatusCode;

import java.time.LocalDateTime;

public class ExceptionDTO {
    @Schema(description = "HTTP-код ошибки", example = "409")
    private int code;
    @Schema(description = "Описание ошибки", example = "запись не должна накладываться на другие")
    private String message;
    @Schema(description = "Время ошибки", example = "2026-10-14T18:30:00")
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
