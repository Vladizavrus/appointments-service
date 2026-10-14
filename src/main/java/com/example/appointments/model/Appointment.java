package com.example.appointments.model;

import java.time.LocalDateTime;

public class Appointment {

    private Long id;

    private String clientName;

    private String phone;

    private Specialist specialist;

    private LocalDateTime startAt;

    private LocalDateTime endAt;

    private Status status;

    private LocalDateTime createdAt;

    public Appointment(Long id, String clientName, String phone, Specialist specialist, LocalDateTime startAt, LocalDateTime endAt, Status status, LocalDateTime createdAt) {
        this.id = id;
        this.clientName = clientName;
        this.phone = phone;
        this.specialist = specialist;
        this.startAt = startAt;
        this.endAt = endAt;
        this.status = status;
        this.createdAt = createdAt;
    }

    public Appointment(String clientName, String phone, Specialist specialist, LocalDateTime startAt, LocalDateTime endAt, LocalDateTime createdAt) {
        this.clientName = clientName;
        this.phone = phone;
        this.specialist = specialist;
        this.startAt = startAt;
        this.endAt = endAt;
        this.status = Status.SCHEDULED;
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

    public Long getId() {
        return id;
    }

    public void setStatus(Status status) {
        this.status = status;
    }


    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setSpecialist(Specialist specialist) {
        this.specialist = specialist;
    }

    public void setStartAt(LocalDateTime startAt) {
        this.startAt = startAt;
    }

    public void setEndAt(LocalDateTime endAt) {
        this.endAt = endAt;
    }
}
