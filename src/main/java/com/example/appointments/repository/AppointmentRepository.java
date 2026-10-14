package com.example.appointments.repository;

import com.example.appointments.model.Appointment;
import com.example.appointments.model.NewAppointment;
import com.example.appointments.model.Specialist;
import com.example.appointments.model.Status;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public class AppointmentRepository {
    private final JdbcTemplate jdbcTemplate;

    public AppointmentRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Appointment createAppointment(NewAppointment newAppointment) {
        Long id = jdbcTemplate.queryForObject(
                "insert into appointments (client_name, phone," +
                        " specialist, start_at, end_at, status, created_at)" +
                        " values (?, ?, ?, ?, ?, ?, ?) returning id",
                Long.class,
                newAppointment.getClientName(), newAppointment.getPhone(),
                newAppointment.getSpecialist().name(), newAppointment.getStartAt(),
                newAppointment.getEndAt(), newAppointment.getStatus().name(),
                newAppointment.getCreatedAt());
        return new Appointment(id, newAppointment);
    }

    public Optional<Appointment> findById(Long id) {
        List<Appointment> result = jdbcTemplate.query("select * from appointments where id = ?", rowMapper, id);
        return result.stream().findFirst();
    }

    public List<Appointment> findAll(Specialist specialist, Status status, LocalDate date) {
        String specialistFilter = specialist == null ? null : specialist.name();
        String statusFilter = status == null ? null : status.name();
        return jdbcTemplate.query("select * from appointments where " +
                        "(?::text is null or specialist = ?) and " +
                        "(?::text is null or status = ?) and " +
                        "(?::date is null or start_at::date = ?) order by start_at",
                rowMapper, specialistFilter, specialistFilter, statusFilter, statusFilter, date, date);
    }

    public void update(Appointment appointment) {
        jdbcTemplate.update("update appointments set client_name = ?, phone = ?, specialist = ?, start_at = ?, end_at = ?, status = ? where id = ?",
                appointment.getClientName(), appointment.getPhone(),
                appointment.getSpecialist().name(), appointment.getStartAt(),
                appointment.getEndAt(), appointment.getStatus().name(), appointment.getId());
    }

    public boolean isIntersect(Specialist specialist, Long id, LocalDateTime startAt, LocalDateTime endAt) {
        List<Appointment> result = jdbcTemplate.query(
                "select * from appointments where (?::bigint is null or id != ?) and specialist = ? " +
                        "and status != 'CANCELLED' and not(end_at <= ? or start_at >= ?)",
                rowMapper, id, id, specialist.name(), startAt, endAt);
        return !result.isEmpty();
    }

    private final RowMapper<Appointment> rowMapper = (rs, rowNum)  -> new Appointment(
            rs.getLong("id"),
            rs.getString("client_name"),
            rs.getString("phone"),
            Specialist.valueOf(rs.getString("specialist")),
            rs.getObject("start_at", LocalDateTime.class),
            rs.getObject("end_at", LocalDateTime.class),
            Status.valueOf(rs.getString("status")),
            rs.getObject("created_at", LocalDateTime.class)
    );
}
