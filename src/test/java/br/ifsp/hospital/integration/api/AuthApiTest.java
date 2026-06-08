package br.ifsp.hospital.integration.api;

import br.ifsp.hospital.annotation.ApiTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.hamcrest.Matchers.*;

@ApiTest
@DisplayName("Testes de API – Autenticação")
class AuthApiTest extends BaseApiIntegrationTest {

    @Nested
    @DisplayName("POST /api/v1/register")
    class Register {

        @Test
        @DisplayName("Deve registrar usuário com dados válidos e retornar 201 com UUID")
        void shouldRegisterUserWithValidDataAndReturn201() {
            withoutAuth()
                    .body(Map.of(
                            "name", faker.name().firstName(),
                            "lastname", faker.name().lastName(),
                            "email", faker.internet().emailAddress(),
                            "password", "Test@1234"
                    ))
                    .post("/api/v1/register")
                    .then()
                    .statusCode(201)
                    .body("id", notNullValue());
        }

        @Test
        @DisplayName("Deve retornar 409 ao registrar com email já existente")
        void shouldReturn409WhenRegisteringWithDuplicateEmail() {
            String email = faker.internet().emailAddress();
            Map<String, String> body = Map.of(
                    "name", faker.name().firstName(),
                    "lastname", faker.name().lastName(),
                    "email", email,
                    "password", "Test@1234"
            );

            withoutAuth().body(body).post("/api/v1/register");

            withoutAuth()
                    .body(body)
                    .post("/api/v1/register")
                    .then()
                    .statusCode(409);
        }

        @Test
        @DisplayName("Deve retornar 400 ao registrar com password nulo")
        void shouldReturn400WhenRegisteringWithNullPassword() {
            withoutAuth()
                    .body(Map.of(
                            "name", faker.name().firstName(),
                            "lastname", faker.name().lastName(),
                            "email", faker.internet().emailAddress()
                    ))
                    .post("/api/v1/register")
                    .then()
                    .statusCode(400);
        }

        @Test
        @DisplayName("Deve retornar 400 ao registrar com email nulo")
        void shouldReturn400WhenRegisteringWithNullEmail() {
            withoutAuth()
                    .body(Map.of(
                            "name", faker.name().firstName(),
                            "lastname", faker.name().lastName(),
                            "password", "Test@1234"
                    ))
                    .post("/api/v1/register")
                    .then()
                    .statusCode(400);
        }

        @Test
        @DisplayName("Deve permitir registro sem token Bearer no header")
        void shouldAllowRegisterWithoutBearerToken() {
            withoutAuth()
                    .body(Map.of(
                            "name", faker.name().firstName(),
                            "lastname", faker.name().lastName(),
                            "email", faker.internet().emailAddress(),
                            "password", "Test@1234"
                    ))
                    .post("/api/v1/register")
                    .then()
                    .statusCode(201);
        }
    }

}
