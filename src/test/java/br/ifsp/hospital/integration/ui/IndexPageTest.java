package br.ifsp.hospital.integration.ui;

import br.ifsp.hospital.integration.ui.page.IndexPage;
import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Testes de UI – index.html")
class IndexPageTest extends BaseUiTest {

    private static final String INDEX_URL = "index.html";

    private IndexPage page;

    @BeforeEach
    void setUp() {
        page = new IndexPage(driver, wait);
    }

    @Test
    @DisplayName("Deve redirecionar para dashboard quando usuário já está autenticado")
    void shouldRedirectToDashboardWhenAlreadyAuthenticated() throws Exception {
        String email = faker.internet().uuid() + "@test.com";
        String password = "Test@1234";
        registerUserViaApi(faker.name().firstName(), faker.name().lastName(), email, password);
        String token = authenticateViaApi(email, password);

        page.open(fileUrl(INDEX_URL));
        page.injectTokenAndRefresh(token);

        page.waitForDashboardRedirect();
        assertThat(page.getCurrentUrl()).contains("dashboard.html");
    }

    @Test
    @DisplayName("Deve exibir aba Login ativa por padrão quando não há token")
    void shouldShowLoginTabActiveByDefault() {
        page.open(fileUrl(INDEX_URL));

        assertThat(page.isLoginTabActive()).isTrue();
        assertThat(page.getActiveTabText()).isEqualTo("Login");
    }

    @Nested
    @DisplayName("Login")
    class Login {

        @Test
        @DisplayName("Deve exibir formulário de login ao clicar na aba Login")
        void shouldShowLoginFormWhenLoginTabClicked() {
            page.open(fileUrl(INDEX_URL));
            page.clickRegisterTab();
            page.clickLoginTab();

            assertThat(page.isLoginTabActive()).isTrue();
        }

        @Test
        @DisplayName("Deve bloquear envio quando campos obrigatórios estão vazios")
        void shouldBlockSubmitWhenLoginFieldsAreEmpty() {
            page.open(fileUrl(INDEX_URL));
            page.submitLogin();

            assertThat(page.isAlertDisplayed()).isFalse();
        }

        @Test
        @DisplayName("Deve bloquear envio com email em formato inválido")
        void shouldBlockSubmitWhenLoginEmailIsInvalid() {
            page.open(fileUrl(INDEX_URL));
            page.fillLoginForm("emailinvalido", "Test@1234");
            page.submitLogin();

            assertThat(page.isAlertDisplayed()).isFalse();
        }

        @Test
        @DisplayName("Deve redirecionar para dashboard com credenciais válidas")
        void shouldRedirectToDashboardWithValidCredentials() throws Exception {
            String email = faker.internet().uuid() + "@test.com";
            String password = "Test@1234";
            registerUserViaApi(faker.name().firstName(), faker.name().lastName(), email, password);

            page.open(fileUrl(INDEX_URL));
            page.fillLoginForm(email, password);
            page.submitLogin();

            page.waitForDashboardRedirect();
            assertThat(page.getCurrentUrl()).contains("dashboard.html");
        }

        @Test
        @DisplayName("Deve exibir mensagem de erro com credenciais inválidas")
        void shouldShowErrorWithInvalidCredentials() {
            page.open(fileUrl(INDEX_URL));
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
            page.open(fileUrl(INDEX_URL));
            page.clickRegisterTab();

            assertThat(page.isRegisterFormActive()).isTrue();
        }

        @Test
        @DisplayName("Deve bloquear envio quando campos obrigatórios estão vazios")
        void shouldBlockSubmitWhenRequiredFieldsAreEmpty() {
            page.open(fileUrl(INDEX_URL));
            page.clickRegisterTab();
            page.submitRegister();

            assertThat(page.isAlertDisplayed()).isFalse();
        }

        @Test
        @DisplayName("Deve registrar usuário com dados válidos e retornar para aba Login")
        void shouldRegisterAndReturnToLoginTab() {
            page.open(fileUrl(INDEX_URL));
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

        @Test
        @DisplayName("Deve exibir mensagem de erro ao registrar com email já cadastrado")
        void shouldShowErrorWhenEmailAlreadyExists() throws Exception {
            String email = faker.internet().uuid() + "@test.com";
            registerUserViaApi(faker.name().firstName(), faker.name().lastName(), email, "Test@1234");

            page.open(fileUrl(INDEX_URL));
            page.clickRegisterTab();
            page.fillRegisterForm(
                    faker.name().firstName(),
                    faker.name().lastName(),
                    email,
                    "Test@1234"
            );
            page.submitRegister();

            assertThat(page.isAlertError()).isTrue();
        }
    }
    
}
