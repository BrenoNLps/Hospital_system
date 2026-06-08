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
    }
}
