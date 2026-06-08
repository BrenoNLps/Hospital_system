package br.ifsp.hospital.integration.api;

import br.ifsp.hospital.annotation.ApiTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.hamcrest.Matchers.*;

@ApiTest
@DisplayName("Testes de API – Procedimentos")
class ProcedureApiTest extends BaseApiIntegrationTest {

    private Map<String, Object> buildProcedure() {
        return Map.of(
                "name", faker.medical().medicineName(),
                "cost", 150.00
        );
    }

    @Nested
    @DisplayName("GET /api/v1/procedures")
    class FindAll {

        @Test
        @DisplayName("Deve retornar lista vazia quando nenhum procedimento está cadastrado")
        void shouldReturnEmptyListWhenNoProceduresExist() {
            withAuth()
                    .get("/api/v1/procedures")
                    .then()
                    .statusCode(200)
                    .body("$", hasSize(0));
        }

        @Test
        @DisplayName("Deve retornar lista com todos os procedimentos cadastrados")
        void shouldReturnAllRegisteredProcedures() {
            withAuth().body(buildProcedure()).post("/api/v1/procedures");
            withAuth().body(buildProcedure()).post("/api/v1/procedures");

            withAuth()
                    .get("/api/v1/procedures")
                    .then()
                    .statusCode(200)
                    .body("$", hasSize(2))
                    .body("[0].id", notNullValue())
                    .body("[0].name", notNullValue())
                    .body("[0].cost", notNullValue());
        }

        @Test
        @DisplayName("Deve retornar 401 ao listar procedimentos sem token de autenticação")
        void shouldReturn401WhenListingProceduresWithoutAuthToken() {
            withoutAuth()
                    .get("/api/v1/procedures")
                    .then()
                    .statusCode(401);
        }
    }

    @Nested
    @DisplayName("POST /api/v1/procedures")
    class Create {

        @Test
        @DisplayName("Deve criar procedimento com dados válidos e retornar 201")
        void shouldCreateProcedureWithValidDataAndReturn201() {
            Map<String, Object> procedure = buildProcedure();

            withAuth()
                    .body(procedure)
                    .post("/api/v1/procedures")
                    .then()
                    .statusCode(201)
                    .body("id", notNullValue())
                    .body("name", equalTo(procedure.get("name")))
                    .body("cost", equalTo(((Double) procedure.get("cost")).floatValue()));
        }

        @Test
        @DisplayName("Deve retornar 400 ao criar procedimento com nome nulo")
        void shouldReturn400WhenNameIsNull() {
            withAuth()
                    .body(Map.of("cost", 150.00))
                    .post("/api/v1/procedures")
                    .then()
                    .statusCode(400)
                    .body("message", notNullValue());
        }

        @Test
        @DisplayName("Deve retornar 400 ao criar procedimento com nome em branco")
        void shouldReturn400WhenNameIsBlank() {
            withAuth()
                    .body(Map.of("name", "   ", "cost", 150.00))
                    .post("/api/v1/procedures")
                    .then()
                    .statusCode(400)
                    .body("message", notNullValue());
        }

        @Test
        @DisplayName("Deve retornar 400 ao criar procedimento com custo nulo")
        void shouldReturn400WhenCostIsNull() {
            withAuth()
                    .body(Map.of("name", faker.medical().medicineName()))
                    .post("/api/v1/procedures")
                    .then()
                    .statusCode(400)
                    .body("message", notNullValue());
        }

        @Test
        @DisplayName("Deve retornar 400 ao criar procedimento com custo negativo")
        void shouldReturn400WhenCostIsNegative() {
            withAuth()
                    .body(Map.of("name", faker.medical().medicineName(), "cost", -1.00))
                    .post("/api/v1/procedures")
                    .then()
                    .statusCode(400)
                    .body("message", notNullValue());
        }

        @Test
        @DisplayName("Deve retornar 401 ao criar procedimento sem token de autenticação")
        void shouldReturn401WhenCreatingProcedureWithoutAuthToken() {
            withoutAuth()
                    .body(buildProcedure())
                    .post("/api/v1/procedures")
                    .then()
                    .statusCode(401);
        }
    }

    @Nested
    @DisplayName("GET /api/v1/procedures/{id}")
    class FindById {

        @Test
        @DisplayName("Deve retornar procedimento quando ID existente")
        void shouldReturnProcedureWhenIdExists() {
            Map<String, Object> procedure = buildProcedure();
            String id = withAuth()
                    .body(procedure)
                    .post("/api/v1/procedures")
                    .then()
                    .statusCode(201)
                    .extract().path("id");

            withAuth()
                    .get("/api/v1/procedures/" + id)
                    .then()
                    .statusCode(200)
                    .body("id", equalTo(id))
                    .body("name", equalTo(procedure.get("name")))
                    .body("cost", equalTo(((Double) procedure.get("cost")).floatValue()));
        }

        @Test
        @DisplayName("Deve retornar 404 ao buscar procedimento com ID inexistente")
        void shouldReturn404WhenProcedureIdNotFound() {
            withAuth()
                    .get("/api/v1/procedures/00000000-0000-0000-0000-000000000000")
                    .then()
                    .statusCode(404)
                    .body("message", notNullValue());
        }

        @Test
        @DisplayName("Deve retornar 400 ao buscar procedimento com ID em formato inválido")
        void shouldReturn400WhenProcedureIdIsInvalid() {
            withAuth()
                    .get("/api/v1/procedures/id-invalido")
                    .then()
                    .statusCode(400)
                    .body("message", notNullValue());
        }

        @Test
        @DisplayName("Deve retornar 401 ao buscar procedimento sem token de autenticação")
        void shouldReturn401WhenFindingProcedureWithoutAuthToken() {
            withoutAuth()
                    .get("/api/v1/procedures/00000000-0000-0000-0000-000000000000")
                    .then()
                    .statusCode(401);
        }
    }
}
