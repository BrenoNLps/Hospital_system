package br.ifsp.hospital.integration.ui;

import br.ifsp.hospital.annotation.UiTest;
import br.ifsp.hospital.integration.ui.page.IndexPage;
import br.ifsp.hospital.security.user.JpaUserRepository;
import com.github.javafaker.Faker;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.*;
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

import static org.assertj.core.api.Assertions.assertThat;

@UiTest
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@TestPropertySource(properties = "server.port=8080")
@ActiveProfiles("test")
@DisplayName("Testes de UI – index.html")
class IndexPageTest {

    @Autowired
    private JpaUserRepository userRepository;

    private WebDriver driver;
    private WebDriverWait wait;
    private IndexPage page;

    private static final Faker faker = Faker.instance();

    @BeforeEach
    void setUp() {
        WebDriverManager.chromedriver().setup();
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless=new");
        options.addArguments("--disable-web-security");
        options.addArguments("--allow-file-access-from-files");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        driver = new ChromeDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(5));
        page = new IndexPage(driver, wait);
    }

    @AfterEach
    void tearDown() {
        if (driver != null) driver.quit();
        userRepository.deleteAll();
    }

    private String indexUrl() {
        return "file:///" + new File("frontend/index.html")
                .getAbsolutePath().replace("\\", "/");
    }

    private void registerUserViaApi(String name, String lastname, String email, String password) throws Exception {
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

    private String authenticateViaApi(String email, String password) throws Exception {
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

    @Test
    @DisplayName("Deve redirecionar para dashboard quando usuário já está autenticado")
    void shouldRedirectToDashboardWhenAlreadyAuthenticated() throws Exception {
        String email = faker.internet().uuid() + "@test.com";
        String password = "Test@1234";
        registerUserViaApi(faker.name().firstName(), faker.name().lastName(), email, password);
        String token = authenticateViaApi(email, password);

        page.open(indexUrl());
        page.injectTokenAndRefresh(token);

        page.waitForDashboardRedirect();
        assertThat(page.getCurrentUrl()).contains("dashboard.html");
    }

    @Test
    @DisplayName("Deve exibir aba Login ativa por padrão quando não há token")
    void shouldShowLoginTabActiveByDefault() {
        page.open(indexUrl());

        assertThat(page.isLoginTabActive()).isTrue();
        assertThat(page.getActiveTabText()).isEqualTo("Login");
    }

    @Nested
    @DisplayName("Login")
    class Login {

        @Test
        @DisplayName("Deve redirecionar para dashboard com credenciais válidas")
        void shouldRedirectToDashboardWithValidCredentials() throws Exception {
            String email = faker.internet().uuid() + "@test.com";
            String password = "Test@1234";
            registerUserViaApi(faker.name().firstName(), faker.name().lastName(), email, password);

            page.open(indexUrl());
            page.fillLoginForm(email, password);
            page.submitLogin();

            page.waitForDashboardRedirect();
            assertThat(page.getCurrentUrl()).contains("dashboard.html");
        }

        @Test
        @DisplayName("Deve exibir mensagem de erro com credenciais inválidas")
        void shouldShowErrorWithInvalidCredentials() {
            page.open(indexUrl());
            page.fillLoginForm(faker.internet().emailAddress(), faker.internet().password());
            page.submitLogin();

            assertThat(page.isAlertError()).isTrue();
            assertThat(page.getAlertText()).isNotBlank();
        }
    }


    @Nested
    @DisplayName("Registro")
    class Register {

        @Test
        @DisplayName("Deve exibir formulário de registro ao clicar na aba Registrar")
        void shouldShowRegisterFormWhenTabClicked() {
            page.open(indexUrl());
            page.clickRegisterTab();

            assertThat(page.isRegisterFormActive()).isTrue();
        }

        @Test
        @DisplayName("Deve bloquear envio quando campos obrigatórios estão vazios")
        void shouldBlockSubmitWhenRequiredFieldsAreEmpty() {
            page.open(indexUrl());
            page.clickRegisterTab();
            page.submitRegister();

            assertThat(page.isAlertDisplayed()).isFalse();
        }

        @Test
        @DisplayName("Deve registrar usuário com dados válidos e retornar para aba Login")
        void shouldRegisterAndReturnToLoginTab() {
            page.open(indexUrl());
            page.clickRegisterTab();
            page.fillRegisterForm(
                    faker.name().firstName(),
                    faker.name().lastName(),
                    faker.internet().uuid() + "@test.com",
                    "Test@1234"
            );
            page.submitRegister();

            assertThat(page.isAlertSuccess()).isTrue();
            page.waitForLoginTabActive();
            assertThat(page.isLoginTabActive()).isTrue();
        }

    }

}
