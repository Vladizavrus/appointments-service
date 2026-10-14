package com.example.appointments.dto;

import com.example.appointments.model.Specialist;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public class AppointmentRequest {

    @NotBlank
    @Size(max=100)
    private String clientName;

    @Size(max=20)
    private String phone;

    @NotNull
    private Specialist specialist;

    @NotNull
    @FutureOrPresent
    private LocalDateTime startAt;

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
