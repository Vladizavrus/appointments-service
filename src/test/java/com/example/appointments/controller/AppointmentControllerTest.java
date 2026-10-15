package com.example.appointments.controller;

import com.example.appointments.dto.AppointmentResponse;
import com.example.appointments.exception.ConflictException;
import com.example.appointments.exception.NotFoundException;
import com.example.appointments.model.Specialist;
import com.example.appointments.model.Status;
import com.example.appointments.service.AppointmentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AppointmentController.class)
class AppointmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AppointmentService service;

    private final LocalDateTime start = LocalDateTime.of(2030, 1, 1, 10, 0);
    private final LocalDateTime end = LocalDateTime.of(2030, 1, 1, 11, 0);

    private final String validJson = """
            {
              "clientName": "Анна",
              "phone": "+79991234567",
              "specialist": "DENTIST",
              "startAt": "2030-01-01T10:00:00",
              "endAt": "2030-01-01T11:00:00"
            }
            """;

    @Test
    void createReturns201() throws Exception {
        AppointmentResponse response = new AppointmentResponse(1L, "Анна", "+79991234567",
                Specialist.DENTIST, start, end, Status.SCHEDULED, LocalDateTime.of(2026, 1, 1, 0, 0));
        when(service.createAppointment(any())).thenReturn(response);

        mockMvc.perform(post("/api/appointments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("SCHEDULED"));
    }

    @Test
    void createWithBlankNameReturns400() throws Exception {
        String json = validJson.replace("\"Анна\"", "\"\"");

        mockMvc.perform(post("/api/appointments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400));

        verify(service, never()).createAppointment(any());
    }

    @Test
    void createWithUnknownSpecialistReturns400() throws Exception {
        String json = validJson.replace("DENTIST", "SURGEON");

        mockMvc.perform(post("/api/appointments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    void getUnknownIdReturns404() throws Exception {
        when(service.findById(42L)).thenThrow(new NotFoundException("такой записи не существует"));

        mockMvc.perform(get("/api/appointments/42"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.message").value("такой записи не существует"));
    }

    @Test
    void cancelNotScheduledReturns409() throws Exception {
        when(service.cancel(1L)).thenThrow(new ConflictException("Запись уже была завершена или отменена"));

        mockMvc.perform(patch("/api/appointments/1/cancel"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value(409));
    }

    @Test
    void wrongStatusFilterReturns400() throws Exception {
        mockMvc.perform(get("/api/appointments").param("status", "FOO"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400));
    }
}
