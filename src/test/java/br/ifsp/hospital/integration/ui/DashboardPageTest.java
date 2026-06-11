package br.ifsp.hospital.integration.ui;

import br.ifsp.hospital.integration.ui.page.*;
import org.junit.jupiter.api.*;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Testes de UI – dashboard.html")
class DashboardPageTest extends BaseUiTest {

    private static final String DASHBOARD_URL = "dashboard.html";
    private static final String INDEX_URL = "index.html";

    private DashboardPage page;
    private String token;

    @BeforeEach
    void setUp() throws Exception {
        String email = faker.internet().uuid() + "@test.com";
        String password = "Test@1234";
        registerUserViaApi(faker.name().firstName(), faker.name().lastName(), email, password);
        token = authenticateViaApi(email, password);
        page = new DashboardPage(driver, wait);
    }

    private void openDashboard() {
        page.open(fileUrl(INDEX_URL));
        page.injectToken(token);
        page.open(fileUrl(DASHBOARD_URL));
    }


    @Nested
    @DisplayName("Navegação")
    class navigation{
        @Test
        @DisplayName("Deve redirecionar para index quando não há token de autenticação")
        void shouldRedirectToIndexWhenNoToken() {
            page.open(fileUrl(DASHBOARD_URL));

            page.waitForRedirectToIndex();
            assertThat(page.getCurrentUrl()).contains("index.html");
        }

        @Test
        @DisplayName("Deve exibir aba Médicos ao clicar em Médicos")
        void shouldShowDoctorsTabWhenClicked() {
            openDashboard();

            DoctorsTabPage doctors = page.clickDoctorsTab();
            assertThat(doctors.isActive()).isTrue();
        }

        @Test
        @DisplayName("Deve exibir aba Procedimentos ao clicar em Procedimentos")
        void shouldShowProceduresTabWhenClicked() {
            openDashboard();

            ProceduresTabPage procedures = page.clickProceduresTab();
            assertThat(procedures.isActive()).isTrue();
        }

        @Test
        @DisplayName("Deve exibir aba Atendimentos ao clicar em Atendimentos")
        void shouldShowAppointmentsTabWhenClicked() {
            openDashboard();

            AppointmentsTabPage appointments = page.clickAppointmentsTab();
            assertThat(appointments.isActive()).isTrue();
        }

    }

}
