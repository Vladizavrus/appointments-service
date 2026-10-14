package com.example.appointments.model;

import java.time.LocalDateTime;

public class NewAppointment {

    private String clientName;

    private String phone;

    private Specialist specialist;

    private LocalDateTime startAt;

    private LocalDateTime endAt;

    private Status status;

    private LocalDateTime createdAt;

    public NewAppointment(String clientName, String phone, Specialist specialist, LocalDateTime startAt, LocalDateTime endAt, Status status, LocalDateTime createdAt) {
        this.clientName = clientName;
        this.phone = phone;
        this.specialist = specialist;
        this.startAt = startAt;
        this.endAt = endAt;
        this.status = status;
        this.createdAt = createdAt;
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
