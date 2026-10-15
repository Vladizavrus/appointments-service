package com.example.appointments.service;

import com.example.appointments.dto.AppointmentRequest;
import com.example.appointments.dto.AppointmentResponse;
import com.example.appointments.exception.BadRequestException;
import com.example.appointments.exception.ConflictException;
import com.example.appointments.exception.NotFoundException;
import com.example.appointments.model.Appointment;
import com.example.appointments.model.NewAppointment;
import com.example.appointments.model.Specialist;
import com.example.appointments.model.Status;
import com.example.appointments.repository.AppointmentRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class AppointmentService {
    private final AppointmentRepository repository;

    public AppointmentService(AppointmentRepository repository) {
        this.repository = repository;
    }

    public AppointmentResponse createAppointment(AppointmentRequest request) {

        if (!request.getEndAt().isAfter(request.getStartAt())) {
            throw new BadRequestException("конец записи должен быть позже начала");
        }

        if (repository.isIntersect(request.getSpecialist(), null, request.getStartAt(), request.getEndAt())) {
            throw new ConflictException("запись не должна накладываться на другие");
        }

        NewAppointment appointmentModel = newAppointmentFromRequest(request, Status.SCHEDULED, LocalDateTime.now());
        return appointmentToResponse(repository.createAppointment(appointmentModel));
    }

    public AppointmentResponse findById(Long id) {
        Appointment appointment = repository.findById(id).orElseThrow(() -> new NotFoundException("такой записи не существует"));
        return appointmentToResponse(appointment);
    }

    public List<AppointmentResponse> findAll(Specialist specialist, Status status, LocalDate date) {
        List<Appointment> appointments = repository.findAll(specialist, status, date);
        return appointments.stream().map((appointment) -> appointmentToResponse(appointment)).toList();
    }

    public AppointmentResponse update(Long id, AppointmentRequest request) {
        Appointment appointment = repository.findById(id).orElseThrow(() -> new NotFoundException("такой записи не существует"));

        if (appointment.getStatus() != Status.SCHEDULED) throw new ConflictException("нельзя изменить завершенную или отмененную запись");
        if (!request.getEndAt().isAfter(request.getStartAt())) {
            throw new BadRequestException("конец записи должен быть позже начала");
        }

        if (repository.isIntersect(request.getSpecialist(), id, request.getStartAt(), request.getEndAt())) {
            throw new ConflictException("запись не должна накладываться на другие");
        }

        appointment.setClientName(request.getClientName());
        appointment.setStartAt(request.getStartAt());
        appointment.setEndAt(request.getEndAt());
        appointment.setPhone(request.getPhone());
        appointment.setSpecialist(request.getSpecialist());

        if (repository.update(appointment) == 0) throw new ConflictException("Запись уже изменена другим запросом");

        return appointmentToResponse(appointment);
    }

    public AppointmentResponse complete(Long id) {
        return closeAppointment(id, Status.COMPLETED);
    }

    public AppointmentResponse cancel(Long id) {
        return closeAppointment(id, Status.CANCELLED);
    }

    private AppointmentResponse closeAppointment(Long id, Status status) {
        Appointment appointment = repository.findById(id).orElseThrow(() -> new NotFoundException("такой записи не существует"));
        if (appointment.getStatus() != Status.SCHEDULED) throw new ConflictException("Запись уже была завершена или отменена");
        appointment.setStatus(status);

        if (repository.update(appointment) == 0) throw new ConflictException("Запись уже изменена другим запросом");

        return appointmentToResponse(appointment);
    }

    private NewAppointment newAppointmentFromRequest(AppointmentRequest request, Status status, LocalDateTime createdAt) {
        return new NewAppointment(
                request.getClientName(), request.getPhone(),
                request.getSpecialist(), request.getStartAt(),
                request.getEndAt(), status, createdAt
        );
    }
    private AppointmentResponse appointmentToResponse(Appointment appointment) {
        return new AppointmentResponse(
                appointment.getId(), appointment.getClientName(),
                appointment.getPhone(), appointment.getSpecialist(),
                appointment.getStartAt(), appointment.getEndAt(),
                appointment.getStatus(), appointment.getCreatedAt()
        );
    }
}
