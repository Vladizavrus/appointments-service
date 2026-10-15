# Appointments Service

REST-сервис для записи клиентов к специалистам.
Позволяет создавать, просматривать, изменять, завершать и отменять записи 
без пересекающихся приёмов у одного и того же специалиста.

## Стек

- Java 21, Spring Boot 3.5
- PostgreSQL, Spring JDBC (`JdbcTemplate`)
- Flyway — миграции схемы
- jakarta.validation — валидация входных данных
- springdoc-openapi — Swagger UI
- JUnit 5, Mockito, `@WebMvcTest` — тесты

## Запуск

### 1. Что нужно заранее

- JDK 21+
- PostgreSQL 13+

В проекте есть Maven Wrapper (`mvnw` / `mvnw.cmd`).

### 2. База данных

Создайте пустую базу:

```sql
CREATE DATABASE appointments;
```

Таблицы создавать не нужно: при старте Flyway сам применит миграции
из `src/main/resources/db/migration`.

Параметры подключения — в `src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/appointments
spring.datasource.username=postgres
spring.datasource.password=postgres
```

### 3. Запуск приложения

Linux / macOS:

```bash
./mvnw spring-boot:run
```

Windows (PowerShell):

```powershell
.\mvnw.cmd spring-boot:run
````

Приложение стартует на `http://localhost:8080`.

### 4. Swagger UI

После запуска документация и форма для запросов доступны по адресу:

http://localhost:8080/swagger-ui.html

## Тесты

- `AppointmentServiceTest` — unit-тесты сервиса (JUnit 5 + Mockito): правила дат,
  пересечения интервалов, переходы статусов, 404, конкурентное изменение записи.
- `AppointmentControllerTest` — тесты веб-слоя через `@WebMvcTest` и `MockMvc`:
  коды ответов 201/400/404/409 и формат ошибок.

## API

Базовый путь: `/api/appointments`

| Метод | Путь | Описание | Успех |
|---|---|---|---|
| `POST` | `/api/appointments` | создать запись | 201 |
| `GET` | `/api/appointments/{id}` | получить запись по id | 200 |
| `GET` | `/api/appointments` | список записей, фильтры `specialist`, `status`, `date` (необязательные), сортировка по `startAt` по возрастанию | 200 |
| `PUT` | `/api/appointments/{id}` | изменить запись (только в статусе `SCHEDULED`) | 200 |
| `PATCH` | `/api/appointments/{id}/complete` | завершить запись (только из `SCHEDULED`) | 200 |
| `PATCH` | `/api/appointments/{id}/cancel` | отменить запись (только из `SCHEDULED`) | 200 |

Значения перечислений:

- `specialist`: `THERAPIST`, `CARDIOLOGIST`, `DENTIST`
- `status`: `SCHEDULED`, `COMPLETED`, `CANCELLED`

### Примеры curl

Создать запись:

```bash
curl -i -X POST http://localhost:8080/api/appointments \
  -H "Content-Type: application/json" \
  -d '{
        "clientName": "Анна Иванова",
        "phone": "+79991234567",
        "specialist": "DENTIST",
        "startAt": "2026-12-01T10:00:00",
        "endAt": "2026-12-01T11:00:00"
      }'
```

Ответ `201 Created`:

```json
{
  "id": 1,
  "clientName": "Анна Иванова",
  "phone": "+79991234567",
  "specialist": "DENTIST",
  "startAt": "2026-12-01T10:00:00",
  "endAt": "2026-12-01T11:00:00",
  "status": "SCHEDULED",
  "createdAt": "2026-10-15T08:00:00.123456"
}
```

Получить запись по id:

```bash
curl -i http://localhost:8080/api/appointments/1
```

Список с фильтрами (все параметры необязательны):

```bash
curl -i "http://localhost:8080/api/appointments?specialist=DENTIST&status=SCHEDULED&date=2026-12-01"
```

Изменить запись:

```bash
curl -i -X PUT http://localhost:8080/api/appointments/1 \
  -H "Content-Type: application/json" \
  -d '{
        "clientName": "Анна Иванова",
        "phone": "+79991234567",
        "specialist": "DENTIST",
        "startAt": "2026-12-01T12:00:00",
        "endAt": "2026-12-01T13:00:00"
      }'
```

Завершить и отменить:

```bash
curl -i -X PATCH http://localhost:8080/api/appointments/1/complete
curl -i -X PATCH http://localhost:8080/api/appointments/2/cancel
```

### Ошибки

Все ошибки возвращаются в едином формате:

```json
{
  "code": 409,
  "message": "запись не должна накладываться на другие",
  "time": "2026-10-15T08:00:00.123456"
}
```

| Код | Когда |
|---|---|
| 400 | не прошла валидация (пустое имя, слишком длинный телефон, начало в прошлом и т.п.), `endAt` не позже `startAt`, некорректный JSON, неизвестное значение enum, неверный тип параметра |
| 404 | записи с таким id нет |
| 409 | время пересекается с другой записью этого специалиста; попытка изменить, завершить или отменить запись не в статусе `SCHEDULED` |

Пример 409 — повторно отправить тот же `POST`, что выше:

```bash
curl -i -X POST http://localhost:8080/api/appointments \
  -H "Content-Type: application/json" \
  -d '{"clientName":"Пётр","specialist":"DENTIST","startAt":"2026-12-01T10:30:00","endAt":"2026-12-01T11:30:00"}'
```

Пример 404:

```bash
curl -i http://localhost:8080/api/appointments/999999
```

## Решения и ограничения

**Архитектура.** Слои controller → service → repository. В API используются только
DTO (`AppointmentRequest`, `AppointmentResponse`, `ExceptionDTO`), внутренняя модель
наружу не отдаётся. Бизнес-правила проверяются в сервисе, а его исключения 
превращаются в HTTP-ответы в GlobalExceptionHandler.

**Модель.** `NewAppointment` — данные записи до сохранения (ещё без id),
`Appointment` — сохранённая запись. Так не бывает «полузаписи» с `id = null`.

**Пересечения.** Интервалы считаются полуоткрытыми `[startAt, endAt)`: приём
10:00–11:00 и приём 11:00–12:00 у одного специалиста не пересекаются (касание
разрешено). Отменённые записи время не занимают и в проверке не участвуют.
При изменении записи она не сравнивается сама с собой.

**Что будет, если два запроса придут одновременно.**

- *Два клиента записываются на одно и то же время.* Сервис сначала проверяет,
  свободно ли время, и только потом сохраняет запись. Если два запроса придут
  в один момент, оба могут успеть увидеть «свободно» и оба сохраниться. Поэтому
  то же правило добавлено в саму базу (миграция `V2`): PostgreSQL не даст
  сохранить вторую пересекающуюся запись того же специалиста, и клиент получит 409.
- *Одну запись одновременно отменяют и меняют* (или отменяют и завершают).
  Сервис меняет запись только при условии, что она всё ещё в статусе `SCHEDULED`
  (`UPDATE ... WHERE id = ? AND status = 'SCHEDULED'`). Кто успел первым — тот
  и изменил запись, а второй запрос получит 409 и не перезапишет чужое изменение.

**Двойная проверка правил.** Правила продублированы ограничениями базы
(`CHECK` на `endAt > startAt` и допустимые значения enum, `NOT NULL`, `EXCLUDE`) —
как вторая линия защиты на случай записи в базу в обход приложения.


