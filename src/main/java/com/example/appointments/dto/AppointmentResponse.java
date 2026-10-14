package com.example.appointments.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import com.example.appointments.model.Specialist;
import com.example.appointments.model.Status;

import java.time.LocalDateTime;

public class AppointmentResponse {
    @Schema(description = "Идентификатор записи", example = "1")
    private Long id;

    @Schema(description = "Имя клиента", example = "Анна Иванова")
    private String clientName;

    @Schema(description = "Телефон клиента", example = "+79991234567")
    private String phone;

    @Schema(description = "Специалист", example = "DENTIST")
    private Specialist specialist;

    @Schema(description = "Начало приёма", example = "2026-12-01T10:00:00")
    private LocalDateTime startAt;

    @Schema(description = "Конец приёма", example = "2026-12-01T11:00:00")
    private LocalDateTime endAt;

    @Schema(description = "Статус записи", example = "SCHEDULED")
    private Status status;

    @Schema(description = "Время создания записи", example = "2026-10-14T18:30:00")
    private LocalDateTime createdAt;

    public AppointmentResponse(Long id, String clientName, String phone, Specialist specialist, LocalDateTime startAt, LocalDateTime endAt, Status status, LocalDateTime createdAt) {
        this.id = id;
        this.clientName = clientName;
        this.phone = phone;
        this.specialist = specialist;
        this.startAt = startAt;
        this.endAt = endAt;
        this.status = status;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
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

    public Status getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
