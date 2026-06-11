package br.ifsp.hospital.integration.ui;

import br.ifsp.hospital.annotation.UiTest;
import br.ifsp.hospital.infrastructure.repository.SpringDataAppointmentRepository;
import br.ifsp.hospital.infrastructure.repository.SpringDataDoctorRepository;
import br.ifsp.hospital.infrastructure.repository.SpringDataPatientRepository;
import br.ifsp.hospital.infrastructure.repository.SpringDataProcedureRepository;
import br.ifsp.hospital.security.user.JpaUserRepository;
import com.github.javafaker.Faker;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.io.File;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

@UiTest
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@TestPropertySource(properties = "server.port=8080")
@ActiveProfiles("test")
public abstract class BaseUiTest {

    @Autowired private JpaUserRepository userRepository;
    @Autowired private SpringDataAppointmentRepository appointmentRepository;
    @Autowired private SpringDataPatientRepository patientRepository;
    @Autowired private SpringDataDoctorRepository doctorRepository;
    @Autowired private SpringDataProcedureRepository procedureRepository;

    protected WebDriver driver;
    protected WebDriverWait wait;

    protected static final Faker faker = Faker.instance();

    @BeforeEach
    void setUpDriver() {
        WebDriverManager.chromedriver().setup();
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless=new");
        options.addArguments("--disable-web-security");
        options.addArguments("--allow-file-access-from-files");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        driver = new ChromeDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(5));
    }

    @AfterEach
    void tearDownDriver() {
        if (driver != null) driver.quit();
        appointmentRepository.deleteAll();
        patientRepository.deleteAll();
        doctorRepository.deleteAll();
        procedureRepository.deleteAll();
        userRepository.deleteAll();
    }

    protected String fileUrl(String filename) {
        return "file:///" + new File("frontend/" + filename)
                .getAbsolutePath().replace("\\", "/");
    }

    protected void registerUserViaApi(String name, String lastname, String email, String password) throws Exception {
        String body = "{\"name\":\"" + name + "\",\"lastname\":\"" + lastname +
                "\",\"email\":\"" + email + "\",\"password\":\"" + password + "\"}";
        HttpClient.newHttpClient().send(
                HttpRequest.newBuilder()
                        .uri(URI.create("http://localhost:8080/api/v1/register"))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(body))
                        .build(),
                HttpResponse.BodyHandlers.discarding());
    }

    protected String authenticateViaApi(String email, String password) throws Exception {
        String body = "{\"username\":\"" + email + "\",\"password\":\"" + password + "\"}";
        String response = HttpClient.newHttpClient().send(
                HttpRequest.newBuilder()
                        .uri(URI.create("http://localhost:8080/api/v1/authenticate"))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(body))
                        .build(),
                HttpResponse.BodyHandlers.ofString()).body();
        return response.replaceAll(".*\"token\":\"([^\"]+)\".*", "$1");
    }
}
