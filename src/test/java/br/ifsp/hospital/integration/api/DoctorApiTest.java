package br.ifsp.hospital.integration.api;

import br.ifsp.hospital.annotation.ApiTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.hamcrest.Matchers.*;

@ApiTest
@DisplayName("Testes de API – Médicos")
class DoctorApiTest extends BaseApiIntegrationTest {

    private Map<String, Object> buildDoctor() {
        return Map.of(
                "name", faker.name().fullName(),
                "specialty", faker.medical().medicineName(),
                "license", faker.numerify("CRM-SP ######")
        );
    }

    @Nested
    @DisplayName("GET /api/v1/doctors")
    class FindAll {

        @Test
        @DisplayName("Deve retornar lista vazia quando nenhum médico está cadastrado")
        void shouldReturnEmptyListWhenNoDoctorsExist() {
            withAuth()
                    .get("/api/v1/doctors")
                    .then()
                    .statusCode(200)
                    .body("$", hasSize(0));
        }
    }
}
