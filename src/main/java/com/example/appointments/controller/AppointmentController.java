package com.example.appointments.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import com.example.appointments.dto.AppointmentRequest;
import com.example.appointments.dto.AppointmentResponse;
import com.example.appointments.model.Specialist;
import com.example.appointments.model.Status;
import com.example.appointments.service.AppointmentService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Tag(name = "Записи", description = "Запись клиентов к специалистам")
@RestController
@RequestMapping("/api/appointments")
public class AppointmentController {
    private final AppointmentService service;

    AppointmentController(AppointmentService service) {
        this.service = service;
    }

    @Operation(summary = "Создать запись")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AppointmentResponse createAppointment(@RequestBody @Valid AppointmentRequest request) {
        return service.createAppointment(request);
    }

    @Operation(summary = "Получить запись по id")
    @GetMapping("/{id}")
    public AppointmentResponse findById(@PathVariable Long id) {
        return service.findById(id);
    }

    @Operation(summary = "Список записей с фильтрами по специалисту, статусу и дню, по возрастанию начала")
    @GetMapping
    public List<AppointmentResponse> findAll(
            @RequestParam(required = false) Specialist specialist,
            @RequestParam(required = false) Status status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return service.findAll(specialist, status, date);
    }

    @Operation(summary = "Изменить запись (только в статусе SCHEDULED)")
    @PutMapping("/{id}")
    public AppointmentResponse update(@PathVariable Long id, @RequestBody @Valid AppointmentRequest request) {
        return service.update(id, request);
    }

    @Operation(summary = "Отменить запись (только в статусе SCHEDULED)")
    @PatchMapping("/{id}/cancel")
    public AppointmentResponse cancel(@PathVariable Long id) {
        return service.cancel(id);
    }

    @Operation(summary = "Завершить запись (только в статусе SCHEDULED)")
    @PatchMapping("/{id}/complete")
    public AppointmentResponse complete(@PathVariable Long id) {
        return service.complete(id);
    }

}
