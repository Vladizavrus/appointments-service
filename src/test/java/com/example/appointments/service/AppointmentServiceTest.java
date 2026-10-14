package com.example.appointments.service;

import com.example.appointments.dto.AppointmentRequest;
import com.example.appointments.dto.AppointmentResponse;
import com.example.appointments.exception.BadRequestException;
import com.example.appointments.exception.ConflictException;
import com.example.appointments.exception.NotFoundException;
import com.example.appointments.model.Appointment;
import com.example.appointments.model.Specialist;
import com.example.appointments.model.Status;
import com.example.appointments.repository.AppointmentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AppointmentServiceTest {

    LocalDateTime firstDate = LocalDateTime.of(2030, Month.JANUARY, 1,1,1);
    LocalDateTime secondDate = LocalDateTime.of(2031, Month.JANUARY, 1,1,1);

    AppointmentRequest requestWithBadDifferentDates = new AppointmentRequest(
            "Иван", null, Specialist.CARDIOLOGIST, secondDate, firstDate);
    AppointmentRequest requestWithSameDates = new AppointmentRequest(
            "Иван", null, Specialist.CARDIOLOGIST, firstDate, firstDate);
    AppointmentRequest goodRequest = new AppointmentRequest(
            "Иван", null, Specialist.CARDIOLOGIST, firstDate, secondDate);
    Appointment goodAppointment = new Appointment(
            1L, "Иван", null, Specialist.CARDIOLOGIST, firstDate, secondDate,
            Status.SCHEDULED, LocalDateTime.of(2026, Month.JANUARY, 1, 0, 0));
    Appointment finishedAppointment = new Appointment(
            2L, "Иван", null, Specialist.CARDIOLOGIST, firstDate, secondDate,
            Status.COMPLETED, LocalDateTime.of(2026, Month.JANUARY, 1, 0, 0));

    @Mock
    private AppointmentRepository repository;

    @InjectMocks
    private AppointmentService service;

    @Test
    void createAppointmentWithWrongDifferentDates() {
        assertThrows(BadRequestException.class, () -> service.createAppointment(requestWithBadDifferentDates));
        verify(repository, never()).createAppointment(any());
    }

    @Test
    void createAppointmentWithSameDates() {
        assertThrows(BadRequestException.class, () -> service.createAppointment(requestWithSameDates));
        verify(repository, never()).createAppointment(any());
    }

    @Test
    void createIntersectingAppointment() {
        when(repository.isIntersect(goodRequest.getSpecialist(), null, goodRequest.getStartAt(), goodRequest.getEndAt())).thenReturn(true);

        assertThrows(ConflictException.class, () -> service.createAppointment(goodRequest));
        verify(repository, never()).createAppointment(any());
    }

    @Test
    void createAppointment() {
        when(repository.isIntersect(goodRequest.getSpecialist(), null, goodRequest.getStartAt(), goodRequest.getEndAt())).thenReturn(false);
        when(repository.createAppointment(any())).thenReturn(goodAppointment);

        AppointmentResponse result = service.createAppointment(goodRequest);

        verify(repository).createAppointment(any());
        assertEquals(goodRequest.getStartAt(), result.getStartAt());
        assertEquals(Status.SCHEDULED, result.getStatus());
    }

    @Test
    void findByWrongId() {
        when(repository.findById(goodAppointment.getId())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.findById(goodAppointment.getId()));
    }

    @Test
    void findByGoodId() {
        when(repository.findById(goodAppointment.getId())).thenReturn(Optional.of(goodAppointment));

        AppointmentResponse result = service.findById(goodAppointment.getId());

        assertEquals(goodAppointment.getId(), result.getId());
        assertEquals(goodAppointment.getClientName(), result.getClientName());
    }

    @Test
    void findAllWithoutFilters() {
        when(repository.findAll(null, null, null)).thenReturn(List.of(goodAppointment, finishedAppointment));

        List<AppointmentResponse> result = service.findAll(null, null, null);

        assertEquals(2, result.size());
        assertEquals(goodAppointment.getId(), result.get(0).getId());
        assertEquals(finishedAppointment.getId(), result.get(1).getId());
    }

    @Test
    void findAllPassesFiltersToRepository() {
        LocalDate date = firstDate.toLocalDate();
        when(repository.findAll(Specialist.CARDIOLOGIST, Status.SCHEDULED, date)).thenReturn(List.of(goodAppointment));

        List<AppointmentResponse> result = service.findAll(Specialist.CARDIOLOGIST, Status.SCHEDULED, date);

        verify(repository).findAll(Specialist.CARDIOLOGIST, Status.SCHEDULED, date);
        assertEquals(1, result.size());
        assertEquals(Status.SCHEDULED, result.get(0).getStatus());
    }

    @Test
    void findAllWhenNothingFound() {
        when(repository.findAll(null, null, null)).thenReturn(List.of());

        List<AppointmentResponse> result = service.findAll(null, null, null);

        assertTrue(result.isEmpty());
    }

    @Test
    void updateScheduledAppointment() {
        AppointmentRequest newRequest = new AppointmentRequest(
                "Пётр", null, Specialist.CARDIOLOGIST, firstDate, secondDate);
        when(repository.findById(goodAppointment.getId())).thenReturn(Optional.of(goodAppointment));
        when(repository.isIntersect(Specialist.CARDIOLOGIST, goodAppointment.getId(), firstDate, secondDate)).thenReturn(false);

        AppointmentResponse result = service.update(goodAppointment.getId(), newRequest);

        assertEquals("Пётр", result.getClientName());
        verify(repository).isIntersect(Specialist.CARDIOLOGIST, goodAppointment.getId(), firstDate, secondDate);
        verify(repository).update(any());
    }

    @Test
    void updateNotExistingAppointment() {
        when(repository.findById(42L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.update(42L, goodRequest));
        verify(repository, never()).update(any());
    }

    @Test
    void updateFinishedAppointment() {
        when(repository.findById(finishedAppointment.getId())).thenReturn(Optional.of(finishedAppointment));

        assertThrows(ConflictException.class, () -> service.update(finishedAppointment.getId(), goodRequest));
        verify(repository, never()).update(any());
    }

    @Test
    void updateWithWrongDates() {
        when(repository.findById(goodAppointment.getId())).thenReturn(Optional.of(goodAppointment));

        assertThrows(BadRequestException.class, () -> service.update(goodAppointment.getId(), requestWithBadDifferentDates));
        verify(repository, never()).update(any());
    }

    @Test
    void updateIntersectingAppointment() {
        when(repository.findById(goodAppointment.getId())).thenReturn(Optional.of(goodAppointment));
        when(repository.isIntersect(Specialist.CARDIOLOGIST, goodAppointment.getId(), firstDate, secondDate)).thenReturn(true);

        assertThrows(ConflictException.class, () -> service.update(goodAppointment.getId(), goodRequest));
        verify(repository, never()).update(any());
    }

    @Test
    void completeFinishedAppointment() {
        when(repository.findById(finishedAppointment.getId())).thenReturn(Optional.of(finishedAppointment));

        assertThrows(ConflictException.class, () -> service.complete(finishedAppointment.getId()));
        verify(repository, never()).update(any());
    }

    @Test
    void cancelFinishedAppointment() {
        when(repository.findById(finishedAppointment.getId())).thenReturn(Optional.of(finishedAppointment));

        assertThrows(ConflictException.class, () -> service.cancel(finishedAppointment.getId()));
        verify(repository, never()).update(any());
    }

    @Test
    void completeScheduledAppointment() {
        when(repository.findById(goodAppointment.getId())).thenReturn(Optional.of(goodAppointment));

        AppointmentResponse result = service.complete(goodAppointment.getId());

        assertEquals(Status.COMPLETED, result.getStatus());
        verify(repository).update(any());
    }

    @Test
    void cancelScheduledAppointment() {
        when(repository.findById(goodAppointment.getId())).thenReturn(Optional.of(goodAppointment));

        AppointmentResponse result = service.cancel(goodAppointment.getId());

        assertEquals(Status.CANCELLED, result.getStatus());
        verify(repository).update(any());
    }

    @Test
    void completeNotExistingAppointment() {
        when(repository.findById(42L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.complete(42L));
        verify(repository, never()).update(any());
    }

    @Test
    void cancelNotExistingAppointment() {
        when(repository.findById(42L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.cancel(42L));
        verify(repository, never()).update(any());
    }
}