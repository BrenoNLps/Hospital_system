package br.ifsp.hospital.integration.api;

import com.github.javafaker.Faker;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

import br.ifsp.hospital.infrastructure.repository.SpringDataAppointmentRepository;
import br.ifsp.hospital.infrastructure.repository.SpringDataDoctorRepository;
import br.ifsp.hospital.infrastructure.repository.SpringDataPatientRepository;
import br.ifsp.hospital.infrastructure.repository.SpringDataProcedureRepository;
import br.ifsp.hospital.security.user.JpaUserRepository;

import java.util.Map;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public abstract class BaseApiIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private SpringDataAppointmentRepository appointmentRepository;
    @Autowired
    private SpringDataPatientRepository patientRepository;
    @Autowired
    private SpringDataDoctorRepository doctorRepository;
    @Autowired
    private SpringDataProcedureRepository procedureRepository;
    @Autowired
    private JpaUserRepository userRepository;

    protected static final Faker faker = Faker.instance();
    protected String authToken;

    @BeforeEach
    void setUpAuth() {
        RestAssured.port = port;
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();

        String email = "test_" + faker.internet().uuid() + "@test.com";
        String password = "Test@1234";

        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(Map.of("name", "Test", "lastname", "User", "email", email, "password", password))
                .post("/api/v1/register");

        authToken = RestAssured.given()
                .contentType(ContentType.JSON)
                .body(Map.of("username", email, "password", password))
                .post("/api/v1/authenticate")
                .then()
                .statusCode(200)
                .extract().path("token");
    }

    @AfterEach
    void tearDown() {
        appointmentRepository.deleteAll();
        patientRepository.deleteAll();
        doctorRepository.deleteAll();
        procedureRepository.deleteAll();
        userRepository.deleteAll();
    }

}
