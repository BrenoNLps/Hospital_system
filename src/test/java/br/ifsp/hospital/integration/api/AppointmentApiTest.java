package br.ifsp.hospital.integration.api;

import br.ifsp.hospital.annotation.ApiTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import static org.hamcrest.Matchers.*;

@ApiTest
@DisplayName("Testes de API – Atendimentos")
class AppointmentApiTest extends BaseApiIntegrationTest {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private String createPatient() {
        return withAuth()
                .body(Map.of(
                        "name", faker.name().fullName(),
                        "document", faker.numerify("###.###.###-##"),
                        "insuranceType", "BASIC"
                ))
                .post("/api/v1/patients")
                .then()
                .statusCode(201)
                .extract().path("id");
    }

    private String createDoctor() {
        return withAuth()
                .body(Map.of(
                        "name", faker.name().fullName(),
                        "specialty", faker.medical().medicineName(),
                        "license", faker.numerify("CRM-SP ######")
                ))
                .post("/api/v1/doctors")
                .then()
                .statusCode(201)
                .extract().path("id");
    }

    private String createProcedure() {
        return withAuth()
                .body(Map.of("name", faker.medical().medicineName(), "cost", 100.00))
                .post("/api/v1/procedures")
                .then()
                .statusCode(201)
                .extract().path("id");
    }

    private String futureDateTime() {
        return LocalDateTime.now().plusDays(1).withHour(10).withMinute(0).withSecond(0).withNano(0)
                .format(FORMATTER);
    }

    @Nested
    @DisplayName("GET /api/v1/appointments")
    class FindAll {

        @Test
        @DisplayName("Deve retornar lista vazia quando nenhum atendimento está cadastrado")
        void shouldReturnEmptyListWhenNoAppointmentsExist() {
            withAuth()
                    .get("/api/v1/appointments")
                    .then()
                    .statusCode(200)
                    .body("$", hasSize(0));
        }

        @Test
        @DisplayName("Deve retornar lista com todos os atendimentos cadastrados")
        void shouldReturnAllRegisteredAppointments() {
            String patientId = createPatient();
            String doctorId = createDoctor();

            withAuth()
                    .body(Map.of("patientId", patientId, "doctorId", doctorId, "scheduledAt", futureDateTime()))
                    .post("/api/v1/appointments");

            withAuth()
                    .get("/api/v1/appointments")
                    .then()
                    .statusCode(200)
                    .body("$", hasSize(1))
                    .body("[0].id", notNullValue())
                    .body("[0].status", equalTo("OPEN"))
                    .body("[0].patient.id", equalTo(patientId))
                    .body("[0].doctor.id", equalTo(doctorId));
        }

        @Test
        @DisplayName("Deve retornar 401 ao listar atendimentos sem token de autenticação")
        void shouldReturn401WhenListingAppointmentsWithoutAuthToken() {
            withoutAuth()
                    .get("/api/v1/appointments")
                    .then()
                    .statusCode(401);
        }
    }

    @Nested
    @DisplayName("GET /api/v1/appointments/{id}")
    class FindById {

        @Test
        @DisplayName("Deve retornar atendimento quando ID existente")
        void shouldReturnAppointmentWhenIdExists() {
            String patientId = createPatient();
            String doctorId = createDoctor();

            String id = withAuth()
                    .body(Map.of("patientId", patientId, "doctorId", doctorId, "scheduledAt", futureDateTime()))
                    .post("/api/v1/appointments")
                    .then()
                    .statusCode(201)
                    .extract().path("id");

            withAuth()
                    .get("/api/v1/appointments/" + id)
                    .then()
                    .statusCode(200)
                    .body("id", equalTo(id))
                    .body("status", equalTo("OPEN"))
                    .body("patient.id", equalTo(patientId))
                    .body("doctor.id", equalTo(doctorId));
        }

        @Test
        @DisplayName("Deve retornar 404 ao buscar atendimento com ID inexistente")
        void shouldReturn404WhenAppointmentIdNotFound() {
            withAuth()
                    .get("/api/v1/appointments/00000000-0000-0000-0000-000000000000")
                    .then()
                    .statusCode(404)
                    .body("message", notNullValue());
        }

        @Test
        @DisplayName("Deve retornar 401 ao buscar atendimento sem token de autenticação")
        void shouldReturn401WhenFindingAppointmentWithoutAuthToken() {
            withoutAuth()
                    .get("/api/v1/appointments/00000000-0000-0000-0000-000000000000")
                    .then()
                    .statusCode(401);
        }
    }

    @Nested
    @DisplayName("GET /api/v1/appointments/patient/{patientId}")
    class FindByPatient {

        @Test
        @DisplayName("Deve retornar atendimentos do paciente")
        void shouldReturnAppointmentsByPatient() {
            String patientId = createPatient();
            String doctorId = createDoctor();

            withAuth()
                    .body(Map.of("patientId", patientId, "doctorId", doctorId, "scheduledAt", futureDateTime()))
                    .post("/api/v1/appointments");

            withAuth()
                    .get("/api/v1/appointments/patient/" + patientId)
                    .then()
                    .statusCode(200)
                    .body("$", hasSize(1))
                    .body("[0].patient.id", equalTo(patientId));
        }

        @Test
        @DisplayName("Deve retornar 401 ao buscar atendimentos por paciente sem token de autenticação")
        void shouldReturn401WhenFindingByPatientWithoutAuthToken() {
            withoutAuth()
                    .get("/api/v1/appointments/patient/00000000-0000-0000-0000-000000000000")
                    .then()
                    .statusCode(401);
        }
    }

    @Nested
    @DisplayName("GET /api/v1/appointments/doctor/{doctorId}")
    class FindByDoctor {

        @Test
        @DisplayName("Deve retornar atendimentos do médico")
        void shouldReturnAppointmentsByDoctor() {
            String patientId = createPatient();
            String doctorId = createDoctor();

            withAuth()
                    .body(Map.of("patientId", patientId, "doctorId", doctorId, "scheduledAt", futureDateTime()))
                    .post("/api/v1/appointments");

            withAuth()
                    .get("/api/v1/appointments/doctor/" + doctorId)
                    .then()
                    .statusCode(200)
                    .body("$", hasSize(1))
                    .body("[0].doctor.id", equalTo(doctorId));
        }

        @Test
        @DisplayName("Deve retornar 401 ao buscar atendimentos por médico sem token de autenticação")
        void shouldReturn401WhenFindingByDoctorWithoutAuthToken() {
            withoutAuth()
                    .get("/api/v1/appointments/doctor/00000000-0000-0000-0000-000000000000")
                    .then()
                    .statusCode(401);
        }
    }

    @Nested
    @DisplayName("POST /api/v1/appointments")
    class Create {

        @Test
        @DisplayName("Deve criar atendimento com dados válidos e retornar 201 com status OPEN")
        void shouldCreateAppointmentWithValidDataAndReturn201() {
            String patientId = createPatient();
            String doctorId = createDoctor();

            withAuth()
                    .body(Map.of("patientId", patientId, "doctorId", doctorId, "scheduledAt", futureDateTime()))
                    .post("/api/v1/appointments")
                    .then()
                    .statusCode(201)
                    .body("id", notNullValue())
                    .body("status", equalTo("OPEN"))
                    .body("patient.id", equalTo(patientId))
                    .body("doctor.id", equalTo(doctorId));
        }

        @Test
        @DisplayName("Deve retornar 400 ao criar atendimento com data no passado")
        void shouldReturn400WhenScheduledAtIsInThePast() {
            String patientId = createPatient();
            String doctorId = createDoctor();
            String pastDate = LocalDateTime.now().minusDays(1).format(FORMATTER);

            withAuth()
                    .body(Map.of("patientId", patientId, "doctorId", doctorId, "scheduledAt", pastDate))
                    .post("/api/v1/appointments")
                    .then()
                    .statusCode(400)
                    .body("message", notNullValue());
        }

        @Test
        @DisplayName("Deve retornar 404 ao criar atendimento com paciente inexistente")
        void shouldReturn404WhenPatientNotFound() {
            withAuth()
                    .body(Map.of(
                            "patientId", "00000000-0000-0000-0000-000000000000",
                            "doctorId", createDoctor(),
                            "scheduledAt", futureDateTime()
                    ))
                    .post("/api/v1/appointments")
                    .then()
                    .statusCode(404)
                    .body("message", notNullValue());
        }

        @Test
        @DisplayName("Deve retornar 404 ao criar atendimento com médico inexistente")
        void shouldReturn404WhenDoctorNotFound() {
            withAuth()
                    .body(Map.of(
                            "patientId", createPatient(),
                            "doctorId", "00000000-0000-0000-0000-000000000000",
                            "scheduledAt", futureDateTime()
                    ))
                    .post("/api/v1/appointments")
                    .then()
                    .statusCode(404)
                    .body("message", notNullValue());
        }

        @Test
        @DisplayName("Deve retornar 401 ao criar atendimento sem token de autenticação")
        void shouldReturn401WhenCreatingAppointmentWithoutAuthToken() {
            withoutAuth()
                    .body(Map.of(
                            "patientId", "00000000-0000-0000-0000-000000000000",
                            "doctorId", "00000000-0000-0000-0000-000000000000",
                            "scheduledAt", futureDateTime()
                    ))
                    .post("/api/v1/appointments")
                    .then()
                    .statusCode(401);
        }

        @Test
        @DisplayName("Deve retornar 422 ao criar atendimento com paciente já com atendimento aberto")
        void shouldReturn422WhenPatientAlreadyHasOpenAppointment() {
            String patientId = createPatient();
            String doctorId = createDoctor();

            withAuth()
                    .body(Map.of("patientId", patientId, "doctorId", doctorId, "scheduledAt", futureDateTime()))
                    .post("/api/v1/appointments")
                    .then()
                    .statusCode(201);

            withAuth()
                    .body(Map.of("patientId", patientId, "doctorId", createDoctor(), "scheduledAt", futureDateTime()))
                    .post("/api/v1/appointments")
                    .then()
                    .statusCode(422)
                    .body("message", notNullValue());
        }
    }

    @Nested
    @DisplayName("PATCH /api/v1/appointments/{id}/reschedule")
    class Reschedule {

        @Test
        @DisplayName("Deve retornar 400 ao reagendar atendimento com data no passado")
        void shouldReturn400WhenRescheduleDateIsInThePast() {
            String appointmentId = withAuth()
                    .body(Map.of("patientId", createPatient(), "doctorId", createDoctor(), "scheduledAt", futureDateTime()))
                    .post("/api/v1/appointments")
                    .then()
                    .statusCode(201)
                    .extract().path("id");

            String pastDate = LocalDateTime.now().minusDays(1).format(FORMATTER);

            withAuth()
                    .body(Map.of("newScheduledAt", pastDate))
                    .patch("/api/v1/appointments/" + appointmentId + "/reschedule")
                    .then()
                    .statusCode(400)
                    .body("message", notNullValue());
        }

        @Test
        @DisplayName("Deve reagendar atendimento e retornar 200 com rescheduleCount incrementado")
        void shouldRescheduleAppointmentAndReturn200WithIncrementedCount() {
            String appointmentId = withAuth()
                    .body(Map.of("patientId", createPatient(), "doctorId", createDoctor(), "scheduledAt", futureDateTime()))
                    .post("/api/v1/appointments")
                    .then()
                    .statusCode(201)
                    .extract().path("id");

            String newScheduledAt = LocalDateTime.now().plusDays(3)
                    .withHour(14).withMinute(0).withSecond(0).withNano(0)
                    .format(FORMATTER);

            withAuth()
                    .body(Map.of("newScheduledAt", newScheduledAt))
                    .patch("/api/v1/appointments/" + appointmentId + "/reschedule")
                    .then()
                    .statusCode(200)
                    .body("id", equalTo(appointmentId))
                    .body("rescheduleCount", equalTo(1))
                    .body("scheduledAt", equalTo(newScheduledAt));
        }
    }

    @Nested
    @DisplayName("PATCH /api/v1/appointments/{id}/bill")
    class MarkAsBilled {

        @Test
        @DisplayName("Deve retornar 422 ao faturar atendimento que não está CLOSED")
        void shouldReturn422WhenAppointmentIsNotClosed() {
            String appointmentId = withAuth()
                    .body(Map.of("patientId", createPatient(), "doctorId", createDoctor(), "scheduledAt", futureDateTime()))
                    .post("/api/v1/appointments")
                    .then()
                    .statusCode(201)
                    .extract().path("id");

            withAuth()
                    .patch("/api/v1/appointments/" + appointmentId + "/bill")
                    .then()
                    .statusCode(422)
                    .body("message", notNullValue());
        }

        @Test
        @DisplayName("Deve faturar atendimento fechado e retornar 200 com status BILLED")
        void shouldMarkAppointmentAsBilledAndReturn200() {
            String appointmentId = withAuth()
                    .body(Map.of("patientId", createPatient(), "doctorId", createDoctor(), "scheduledAt", futureDateTime()))
                    .post("/api/v1/appointments")
                    .then()
                    .statusCode(201)
                    .extract().path("id");

            withAuth().patch("/api/v1/appointments/" + appointmentId + "/close");

            withAuth()
                    .patch("/api/v1/appointments/" + appointmentId + "/bill")
                    .then()
                    .statusCode(200)
                    .body("id", equalTo(appointmentId))
                    .body("status", equalTo("BILLED"));
        }
    }

    @Nested
    @DisplayName("PATCH /api/v1/appointments/{id}/close")
    class Close {

        @Test
        @DisplayName("Deve retornar 422 ao fechar atendimento com procedimentos pendentes")
        void shouldReturn422WhenAppointmentHasOpenProcedures() {
            String appointmentId = withAuth()
                    .body(Map.of("patientId", createPatient(), "doctorId", createDoctor(), "scheduledAt", futureDateTime()))
                    .post("/api/v1/appointments")
                    .then()
                    .statusCode(201)
                    .extract().path("id");

            withAuth()
                    .body(Map.of("procedureId", createProcedure(), "quantity", 1))
                    .post("/api/v1/appointments/" + appointmentId + "/procedures");

            withAuth()
                    .patch("/api/v1/appointments/" + appointmentId + "/close")
                    .then()
                    .statusCode(422)
                    .body("message", notNullValue());
        }

        @Test
        @DisplayName("Deve fechar atendimento sem procedimentos pendentes e retornar 200 com status CLOSED")
        void shouldCloseAppointmentAndReturn200WithStatusClosed() {
            String appointmentId = withAuth()
                    .body(Map.of("patientId", createPatient(), "doctorId", createDoctor(), "scheduledAt", futureDateTime()))
                    .post("/api/v1/appointments")
                    .then()
                    .statusCode(201)
                    .extract().path("id");

            withAuth()
                    .patch("/api/v1/appointments/" + appointmentId + "/close")
                    .then()
                    .statusCode(200)
                    .body("id", equalTo(appointmentId))
                    .body("status", equalTo("CLOSED"));
        }
    }

    @Nested
    @DisplayName("POST /api/v1/appointments/{id}/procedures")
    class AddProcedure {

        @Test
        @DisplayName("Deve retornar 422 ao adicionar procedimento em atendimento que não está OPEN")
        void shouldReturn422WhenAppointmentIsNotOpen() {
            String appointmentId = withAuth()
                    .body(Map.of("patientId", createPatient(), "doctorId", createDoctor(), "scheduledAt", futureDateTime()))
                    .post("/api/v1/appointments")
                    .then()
                    .statusCode(201)
                    .extract().path("id");

            withAuth().patch("/api/v1/appointments/" + appointmentId + "/close");

            withAuth()
                    .body(Map.of("procedureId", createProcedure(), "quantity", 1))
                    .post("/api/v1/appointments/" + appointmentId + "/procedures")
                    .then()
                    .statusCode(422)
                    .body("message", notNullValue());
        }

        @Test
        @DisplayName("Deve retornar 404 ao adicionar procedimento com procedimento inexistente")
        void shouldReturn404WhenProcedureNotFound() {
            String appointmentId = withAuth()
                    .body(Map.of("patientId", createPatient(), "doctorId", createDoctor(), "scheduledAt", futureDateTime()))
                    .post("/api/v1/appointments")
                    .then()
                    .statusCode(201)
                    .extract().path("id");

            withAuth()
                    .body(Map.of("procedureId", "00000000-0000-0000-0000-000000000000", "quantity", 1))
                    .post("/api/v1/appointments/" + appointmentId + "/procedures")
                    .then()
                    .statusCode(404)
                    .body("message", notNullValue());
        }

        @Test
        @DisplayName("Deve retornar 404 ao adicionar procedimento com atendimento inexistente")
        void shouldReturn404WhenAppointmentNotFound() {
            withAuth()
                    .body(Map.of("procedureId", createProcedure(), "quantity", 1))
                    .post("/api/v1/appointments/00000000-0000-0000-0000-000000000000/procedures")
                    .then()
                    .statusCode(404)
                    .body("message", notNullValue());
        }

        @Test
        @DisplayName("Deve retornar 400 ao adicionar procedimento com quantidade zero ou negativa")
        void shouldReturn400WhenQuantityIsZeroOrNegative() {
            String appointmentId = withAuth()
                    .body(Map.of("patientId", createPatient(), "doctorId", createDoctor(), "scheduledAt", futureDateTime()))
                    .post("/api/v1/appointments")
                    .then()
                    .statusCode(201)
                    .extract().path("id");

            withAuth()
                    .body(Map.of("procedureId", createProcedure(), "quantity", 0))
                    .post("/api/v1/appointments/" + appointmentId + "/procedures")
                    .then()
                    .statusCode(400)
                    .body("message", notNullValue());
        }

        @Test
        @DisplayName("Deve adicionar procedimento ao atendimento e retornar 200 com procedimento na lista")
        void shouldAddProcedureToAppointmentAndReturn200() {
            String appointmentId = withAuth()
                    .body(Map.of("patientId", createPatient(), "doctorId", createDoctor(), "scheduledAt", futureDateTime()))
                    .post("/api/v1/appointments")
                    .then()
                    .statusCode(201)
                    .extract().path("id");

            String procedureId = createProcedure();

            withAuth()
                    .body(Map.of("procedureId", procedureId, "quantity", 1))
                    .post("/api/v1/appointments/" + appointmentId + "/procedures")
                    .then()
                    .statusCode(200)
                    .body("id", equalTo(appointmentId))
                    .body("procedures", hasSize(1))
                    .body("procedures[0].procedureName", notNullValue())
                    .body("procedures[0].quantity", equalTo(1))
                    .body("procedures[0].totalCost", notNullValue());
        }
    }
}
