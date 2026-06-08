package br.ifsp.hospital.integration.api;

import br.ifsp.hospital.annotation.ApiTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.hamcrest.Matchers.*;

@ApiTest
@DisplayName("Testes de API – Pacientes")
class PatientApiTest extends BaseApiIntegrationTest {

    private Map<String, Object> buildPatient() {
        return Map.of(
                "name", faker.name().fullName(),
                "document", faker.numerify("###.###.###-##"),
                "insuranceType", "BASIC"
        );
    }

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

        @Test
        @DisplayName("Deve retornar lista com todos os pacientes cadastrados")
        void shouldReturnAllRegisteredPatients() {
            withAuth().body(buildPatient()).post("/api/v1/patients");
            withAuth().body(buildPatient()).post("/api/v1/patients");

            withAuth()
                    .get("/api/v1/patients")
                    .then()
                    .statusCode(200)
                    .body("$", hasSize(2));
        }

        @Test
        @DisplayName("Deve retornar 401 ao listar pacientes sem token de autenticação")
        void shouldReturn401WhenListingPatientsWithoutAuthToken() {
            withoutAuth()
                    .get("/api/v1/patients")
                    .then()
                    .statusCode(401);
        }
    }

    @Nested
    @DisplayName("POST /api/v1/patients")
    class Create {

        @Test
        @DisplayName("Deve criar paciente com dados válidos e retornar 201")
        void shouldCreatePatientWithValidDataAndReturn201() {
            withAuth()
                    .body(buildPatient())
                    .post("/api/v1/patients")
                    .then()
                    .statusCode(201)
                    .body("id", notNullValue());
        }

        @Test
        @DisplayName("Deve criar paciente com insuranceType PREMIUM e retornar 201")
        void shouldCreatePatientWithPremiumInsuranceAndReturn201() {
            withAuth()
                    .body(Map.of(
                            "name", faker.name().fullName(),
                            "document", faker.numerify("###.###.###-##"),
                            "insuranceType", "PREMIUM"
                    ))
                    .post("/api/v1/patients")
                    .then()
                    .statusCode(201)
                    .body("insuranceType", equalTo("PREMIUM"));
        }

        @Test
        @DisplayName("Deve criar paciente com insuranceType NONE e retornar 201")
        void shouldCreatePatientWithNoneInsuranceAndReturn201() {
            withAuth()
                    .body(Map.of(
                            "name", faker.name().fullName(),
                            "document", faker.numerify("###.###.###-##"),
                            "insuranceType", "NONE"
                    ))
                    .post("/api/v1/patients")
                    .then()
                    .statusCode(201)
                    .body("insuranceType", equalTo("NONE"));
        }

        @Test
        @DisplayName("Deve retornar 400 ao criar paciente com nome nulo")
        void shouldReturn400WhenNameIsNull() {
            withAuth()
                    .body(Map.of(
                            "document", faker.numerify("###.###.###-##"),
                            "insuranceType", "BASIC"
                    ))
                    .post("/api/v1/patients")
                    .then()
                    .statusCode(400);
        }

        @Test
        @DisplayName("Deve retornar 400 ao criar paciente com nome em branco")
        void shouldReturn400WhenNameIsBlank() {
            withAuth()
                    .body(Map.of(
                            "name", "   ",
                            "document", faker.numerify("###.###.###-##"),
                            "insuranceType", "BASIC"
                    ))
                    .post("/api/v1/patients")
                    .then()
                    .statusCode(400);
        }

        @Test
        @DisplayName("Deve retornar 400 ao criar paciente com documento nulo")
        void shouldReturn400WhenDocumentIsNull() {
            withAuth()
                    .body(Map.of(
                            "name", faker.name().fullName(),
                            "insuranceType", "BASIC"
                    ))
                    .post("/api/v1/patients")
                    .then()
                    .statusCode(400);
        }

        @Test
        @DisplayName("Deve retornar 400 ao criar paciente com documento em branco")
        void shouldReturn400WhenDocumentIsBlank() {
            withAuth()
                    .body(Map.of(
                            "name", faker.name().fullName(),
                            "document", "   ",
                            "insuranceType", "BASIC"
                    ))
                    .post("/api/v1/patients")
                    .then()
                    .statusCode(400);
        }

        @Test
        @DisplayName("Deve retornar 400 ao criar paciente com insuranceType nulo")
        void shouldReturn400WhenInsuranceTypeIsNull() {
            withAuth()
                    .body(Map.of(
                            "name", faker.name().fullName(),
                            "document", faker.numerify("###.###.###-##")
                    ))
                    .post("/api/v1/patients")
                    .then()
                    .statusCode(400);
        }

        @Test
        @DisplayName("Deve retornar 400 ao criar paciente com insuranceType inválido")
        void shouldReturn400WhenInsuranceTypeIsInvalid() {
            withAuth()
                    .body(Map.of(
                            "name", faker.name().fullName(),
                            "document", faker.numerify("###.###.###-##"),
                            "insuranceType", "INVALIDO"
                    ))
                    .post("/api/v1/patients")
                    .then()
                    .statusCode(400);
        }

        @Test
        @DisplayName("Deve retornar 409 ao criar paciente com documento duplicado")
        void shouldReturn409WhenDocumentIsDuplicate() {
            String document = faker.numerify("###.###.###-##");

            withAuth()
                    .body(Map.of(
                            "name", faker.name().fullName(),
                            "document", document,
                            "insuranceType", "BASIC"
                    ))
                    .post("/api/v1/patients");

            withAuth()
                    .body(Map.of(
                            "name", faker.name().fullName(),
                            "document", document,
                            "insuranceType", "BASIC"
                    ))
                    .post("/api/v1/patients")
                    .then()
                    .statusCode(409);
        }

        @Test
        @DisplayName("Deve retornar 401 ao criar paciente sem token de autenticação")
        void shouldReturn401WhenCreatingPatientWithoutAuthToken() {
            withoutAuth()
                    .body(buildPatient())
                    .post("/api/v1/patients")
                    .then()
                    .statusCode(401);
        }
    }

    @Nested
    @DisplayName("GET /api/v1/patients/{id}")
    class FindById {

        @Test
        @DisplayName("Deve retornar paciente quando ID existente")
        void shouldReturnPatientWhenIdExists() {
            String id = withAuth()
                    .body(buildPatient())
                    .post("/api/v1/patients")
                    .then()
                    .statusCode(201)
                    .extract().path("id");

            withAuth()
                    .get("/api/v1/patients/" + id)
                    .then()
                    .statusCode(200)
                    .body("id", equalTo(id));
        }

        @Test
        @DisplayName("Deve retornar 404 ao buscar paciente com ID inexistente")
        void shouldReturn404WhenPatientIdNotFound() {
            withAuth()
                    .get("/api/v1/patients/00000000-0000-0000-0000-000000000000")
                    .then()
                    .statusCode(404);
        }

        @Test
        @DisplayName("Deve retornar 400 ao buscar paciente com ID em formato inválido")
        void shouldReturn400WhenPatientIdIsInvalid() {
            withAuth()
                    .get("/api/v1/patients/id-invalido")
                    .then()
                    .statusCode(400);
        }

        @Test
        @DisplayName("Deve retornar 401 ao buscar paciente sem token de autenticação")
        void shouldReturn401WhenFindingPatientWithoutAuthToken() {
            withoutAuth()
                    .get("/api/v1/patients/00000000-0000-0000-0000-000000000000")
                    .then()
                    .statusCode(401);
        }
    }
}


