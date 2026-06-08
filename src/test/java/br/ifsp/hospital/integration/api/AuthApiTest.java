package br.ifsp.hospital.integration.api;

import br.ifsp.hospital.annotation.ApiTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.hamcrest.Matchers.*;

@ApiTest
@DisplayName("Testes de API – Usuário")
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
                    .body("id", notNullValue())
                    .body("id", matchesPattern("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}"));
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
                    .statusCode(409)
                    .body("message", notNullValue());
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
                    .statusCode(400)
                    .body("message", notNullValue());
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
                    .statusCode(400)
                    .body("message", notNullValue());
        }

        @Test
        @DisplayName("Deve permitir registro sem token de autenticação")
        void shouldAllowRegisterWithoutAuthToken() {
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
    }


    @Nested
    @DisplayName("POST /api/v1/authenticate")
    class Authenticate {

        private void registerUser(String email, String password) {
            withoutAuth()
                    .body(Map.of("name", faker.name().firstName(), "lastname", faker.name().lastName(),
                            "email", email, "password", password))
                    .post("/api/v1/register");
        }

        @Test
        @DisplayName("Deve autenticar com credenciais válidas e retornar 200 com token JWT")
        void shouldAuthenticateWithValidCredentialsAndReturnToken() {
            String email = faker.internet().emailAddress();
            String password = "Test@1234";
            registerUser(email, password);

            withoutAuth()
                    .body(Map.of("username", email, "password", password))
                    .post("/api/v1/authenticate")
                    .then()
                    .statusCode(200)
                    .body("token", notNullValue())
                    .body("token", not(emptyString()));
        }

        @Test
        @DisplayName("Deve retornar 401 ao autenticar com senha incorreta")
        void shouldReturn401WhenAuthenticatingWithWrongPassword() {
            String email = faker.internet().emailAddress();
            registerUser(email, "Test@1234");

            withoutAuth()
                    .body(Map.of("username", email, "password", "SenhaErrada@999"))
                    .post("/api/v1/authenticate")
                    .then()
                    .statusCode(401);
        }

        @Test
        @DisplayName("Deve retornar 401 ao autenticar com email inexistente")
        void shouldReturn401WhenAuthenticatingWithNonExistentEmail() {
            withoutAuth()
                    .body(Map.of("username", "naoexiste@test.com", "password", "Test@1234"))
                    .post("/api/v1/authenticate")
                    .then()
                    .statusCode(401);
        }

        @Test
        @DisplayName("Deve permitir autenticação sem token de autenticação")
        void shouldAllowAuthenticateWithoutAuthToken() {
            String email = faker.internet().emailAddress();
            String password = "Test@1234";
            registerUser(email, password);

            withoutAuth()
                    .body(Map.of("username", email, "password", password))
                    .post("/api/v1/authenticate")
                    .then()
                    .statusCode(200)
                    .body("token", notNullValue())
                    .body("token", not(emptyString()));
        }
    }
}
