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

        @Test
        @DisplayName("Deve retornar lista com todos os médicos cadastrados")
        void shouldReturnAllRegisteredDoctors() {
            withAuth().body(buildDoctor()).post("/api/v1/doctors");
            withAuth().body(buildDoctor()).post("/api/v1/doctors");

            withAuth()
                    .get("/api/v1/doctors")
                    .then()
                    .statusCode(200)
                    .body("$", hasSize(2))
                    .body("[0].id", notNullValue())
                    .body("[0].name", notNullValue())
                    .body("[0].specialty", notNullValue())
                    .body("[0].license", notNullValue());
        }

        @Test
        @DisplayName("Deve retornar 401 ao listar médicos sem token de autenticação")
        void shouldReturn401WhenListingDoctorsWithoutAuthToken() {
            withoutAuth()
                    .get("/api/v1/doctors")
                    .then()
                    .statusCode(401);
        }
    }

    @Nested
    @DisplayName("POST /api/v1/doctors")
    class Create {

        @Test
        @DisplayName("Deve criar médico com dados válidos e retornar 201")
        void shouldCreateDoctorWithValidDataAndReturn201() {
            Map<String, Object> doctor = buildDoctor();

            withAuth()
                    .body(doctor)
                    .post("/api/v1/doctors")
                    .then()
                    .statusCode(201)
                    .body("id", notNullValue())
                    .body("name", equalTo(doctor.get("name")))
                    .body("specialty", equalTo(doctor.get("specialty")))
                    .body("license", equalTo(doctor.get("license")));
        }

        @Test
        @DisplayName("Deve retornar 400 ao criar médico com nome nulo")
        void shouldReturn400WhenNameIsNull() {
            withAuth()
                    .body(Map.of(
                            "specialty", faker.medical().medicineName(),
                            "license", faker.numerify("CRM-SP ######")
                    ))
                    .post("/api/v1/doctors")
                    .then()
                    .statusCode(400)
                    .body("message", notNullValue());
        }

        @Test
        @DisplayName("Deve retornar 400 ao criar médico com nome em branco")
        void shouldReturn400WhenNameIsBlank() {
            withAuth()
                    .body(Map.of(
                            "name", "   ",
                            "specialty", faker.medical().medicineName(),
                            "license", faker.numerify("CRM-SP ######")
                    ))
                    .post("/api/v1/doctors")
                    .then()
                    .statusCode(400)
                    .body("message", notNullValue());
        }

        @Test
        @DisplayName("Deve retornar 400 ao criar médico com especialidade nula")
        void shouldReturn400WhenSpecialtyIsNull() {
            withAuth()
                    .body(Map.of(
                            "name", faker.name().fullName(),
                            "license", faker.numerify("CRM-SP ######")
                    ))
                    .post("/api/v1/doctors")
                    .then()
                    .statusCode(400)
                    .body("message", notNullValue());
        }

        @Test
        @DisplayName("Deve retornar 400 ao criar médico com especialidade em branco")
        void shouldReturn400WhenSpecialtyIsBlank() {
            withAuth()
                    .body(Map.of(
                            "name", faker.name().fullName(),
                            "specialty", "   ",
                            "license", faker.numerify("CRM-SP ######")
                    ))
                    .post("/api/v1/doctors")
                    .then()
                    .statusCode(400)
                    .body("message", notNullValue());
        }

        @Test
        @DisplayName("Deve retornar 400 ao criar médico com CRM nulo")
        void shouldReturn400WhenLicenseIsNull() {
            withAuth()
                    .body(Map.of(
                            "name", faker.name().fullName(),
                            "specialty", faker.medical().medicineName()
                    ))
                    .post("/api/v1/doctors")
                    .then()
                    .statusCode(400)
                    .body("message", notNullValue());
        }

        @Test
        @DisplayName("Deve retornar 400 ao criar médico com CRM em branco")
        void shouldReturn400WhenLicenseIsBlank() {
            withAuth()
                    .body(Map.of(
                            "name", faker.name().fullName(),
                            "specialty", faker.medical().medicineName(),
                            "license", "   "
                    ))
                    .post("/api/v1/doctors")
                    .then()
                    .statusCode(400)
                    .body("message", notNullValue());
        }

        @Test
        @DisplayName("Deve retornar 409 ao criar médico com CRM duplicado")
        void shouldReturn409WhenLicenseIsDuplicate() {
            String license = faker.numerify("CRM-SP ######");

            withAuth()
                    .body(Map.of("name", faker.name().fullName(), "specialty", faker.medical().medicineName(), "license", license))
                    .post("/api/v1/doctors");

            withAuth()
                    .body(Map.of("name", faker.name().fullName(), "specialty", faker.medical().medicineName(), "license", license))
                    .post("/api/v1/doctors")
                    .then()
                    .statusCode(409)
                    .body("message", notNullValue());
        }

        @Test
        @DisplayName("Deve retornar 401 ao criar médico sem token de autenticação")
        void shouldReturn401WhenCreatingDoctorWithoutAuthToken() {
            withoutAuth()
                    .body(buildDoctor())
                    .post("/api/v1/doctors")
                    .then()
                    .statusCode(401);
        }
    }

    @Nested
    @DisplayName("GET /api/v1/doctors/{id}")
    class FindById {

        @Test
        @DisplayName("Deve retornar médico quando ID existente")
        void shouldReturnDoctorWhenIdExists() {
            Map<String, Object> doctor = buildDoctor();
            String id = withAuth()
                    .body(doctor)
                    .post("/api/v1/doctors")
                    .then()
                    .statusCode(201)
                    .extract().path("id");

            withAuth()
                    .get("/api/v1/doctors/" + id)
                    .then()
                    .statusCode(200)
                    .body("id", equalTo(id))
                    .body("name", equalTo(doctor.get("name")))
                    .body("specialty", equalTo(doctor.get("specialty")))
                    .body("license", equalTo(doctor.get("license")));
        }
    }
}
