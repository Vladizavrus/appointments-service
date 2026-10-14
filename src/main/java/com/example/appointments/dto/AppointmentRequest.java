package com.example.appointments.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import com.example.appointments.model.Specialist;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public class AppointmentRequest {

    @Schema(description = "Имя клиента", example = "Анна Иванова")
    @NotBlank
    @Size(max=100)
    private String clientName;

    @Schema(description = "Телефон клиента (необязательный)", example = "+79991234567")
    @Size(max=20)
    private String phone;

    @Schema(description = "Специалист", example = "DENTIST")
    @NotNull
    private Specialist specialist;

    @Schema(description = "Начало приёма, не в прошлом", example = "2026-12-01T10:00:00")
    @NotNull
    @FutureOrPresent
    private LocalDateTime startAt;

    @Schema(description = "Конец приёма, строго позже начала", example = "2026-12-01T11:00:00")
    @NotNull
    @FutureOrPresent
    private LocalDateTime endAt;

    public AppointmentRequest(String clientName, String phone, Specialist specialist, LocalDateTime startAt, LocalDateTime endAt) {
        this.clientName = clientName;
        this.phone = phone;
        this.specialist = specialist;
        this.startAt = startAt;
        this.endAt = endAt;
    }

    public String getClientName() {
        return clientName;
    }

    public String getPhone() {
        return phone;
    }

    public Specialist getSpecialist() {
        return specialist;
    }

    public LocalDateTime getStartAt() {
        return startAt;
    }

    public LocalDateTime getEndAt() {
        return endAt;
    }
}
