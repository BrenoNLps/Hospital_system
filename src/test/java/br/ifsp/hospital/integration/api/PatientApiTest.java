package br.ifsp.hospital.integration.api;

import br.ifsp.hospital.annotation.ApiTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.*;

@ApiTest
@DisplayName("Testes de API – Pacientes")
class PatientApiTest extends BaseApiIntegrationTest {

    @Nested
    @DisplayName("GET /api/v1/patients")
    class FindAll {

        @Test
        @DisplayName("Deve retornar lista vazia quando nenhum paciente está cadastrado")
        void shouldReturnEmptyListWhenNoPatientsExist() {
            withAuth()
                    .get("/api/v1/patients")
                    .then()
                    .statusCode(200)
                    .body("$", hasSize(0));
        }
    }
}
