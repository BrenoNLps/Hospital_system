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

    private String createPatientViaApi(String name, String document) throws Exception {
        String body = "{\"name\":\"" + name + "\",\"document\":\"" + document + "\",\"insuranceType\":\"BASIC\"}";
        String response = HttpClient.newHttpClient().send(
                HttpRequest.newBuilder()
                        .uri(URI.create("http://localhost:8080/api/v1/patients"))
                        .header("Content-Type", "application/json")
                        .header("Authorization", "Bearer " + token)
                        .POST(HttpRequest.BodyPublishers.ofString(body))
                        .build(),
                HttpResponse.BodyHandlers.ofString()).body();
        return response.replaceAll(".*\"id\":\"([^\"]+)\".*", "$1");
    }

    private String createDoctorViaApi(String name, String license) throws Exception {
        String body = "{\"name\":\"" + name + "\",\"specialty\":\"Cardiologia\",\"license\":\"" + license + "\"}";
        String response = HttpClient.newHttpClient().send(
                HttpRequest.newBuilder()
                        .uri(URI.create("http://localhost:8080/api/v1/doctors"))
                        .header("Content-Type", "application/json")
                        .header("Authorization", "Bearer " + token)
                        .POST(HttpRequest.BodyPublishers.ofString(body))
                        .build(),
                HttpResponse.BodyHandlers.ofString()).body();
        return response.replaceAll(".*\"id\":\"([^\"]+)\".*", "$1");
    }

    private String createProcedureViaApi(String name) throws Exception {
        String body = "{\"name\":\"" + name + "\",\"cost\":100.00}";
        String response = HttpClient.newHttpClient().send(
                HttpRequest.newBuilder()
                        .uri(URI.create("http://localhost:8080/api/v1/procedures"))
                        .header("Content-Type", "application/json")
                        .header("Authorization", "Bearer " + token)
                        .POST(HttpRequest.BodyPublishers.ofString(body))
                        .build(),
                HttpResponse.BodyHandlers.ofString()).body();
        return response.replaceAll(".*\"id\":\"([^\"]+)\".*", "$1");
    }

    private String createAppointmentViaApi(String patientId, String doctorId) throws Exception {
        String scheduledAt = LocalDateTime.now().plusDays(1)
                .withHour(10).withMinute(0).withSecond(0).withNano(0)
                .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        String body = "{\"patientId\":\"" + patientId + "\",\"doctorId\":\"" + doctorId +
                "\",\"scheduledAt\":\"" + scheduledAt + "\"}";
        String response = HttpClient.newHttpClient().send(
                HttpRequest.newBuilder()
                        .uri(URI.create("http://localhost:8080/api/v1/appointments"))
                        .header("Content-Type", "application/json")
                        .header("Authorization", "Bearer " + token)
                        .POST(HttpRequest.BodyPublishers.ofString(body))
                        .build(),
                HttpResponse.BodyHandlers.ofString()).body();
        return response.replaceAll(".*\"id\":\"([^\"]+)\".*", "$1");
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

        @Test
        @DisplayName("Deve fazer logout e redirecionar para index ao clicar em Sair")
        void shouldLogoutAndRedirectToIndex() {
            openDashboard();
            page.clickLogout();

            page.waitForRedirectToIndex();
            assertThat(page.getCurrentUrl()).contains("index.html");
        }
    }

    @Nested
    @DisplayName("Pacientes")
    class Patients {

        @Test
        @DisplayName("Deve carregar lista de pacientes automaticamente ao abrir o dashboard")
        void shouldLoadPatientsAutomaticallyOnDashboardOpen() throws Exception {
            String name = faker.name().fullName();
            createPatientViaApi(name, faker.numerify("###.###.###-##"));

            openDashboard();
            PatientsTabPage patients = page.getPatientsTab();
            patients.waitForRowCount(1);

            assertThat(patients.listContains(name)).isTrue();
        }

        @Test
        @DisplayName("Deve cadastrar paciente com dados válidos")
        void shouldRegisterPatientWithValidData() {
            openDashboard();
            String name = faker.name().fullName();
            PatientsTabPage patients = page.getPatientsTab();
            patients.fillForm(name, faker.numerify("###.###.###-##"), "BASIC");
            patients.submit();

            assertThat(page.isAlertSuccess()).isTrue();
            patients.waitForRowCount(1);
            assertThat(patients.listContains(name)).isTrue();
        }

        @Test
        @DisplayName("Deve exibir erro ao cadastrar paciente com campos vazios")
        void shouldShowErrorWhenPatientFieldsAreEmpty() {
            openDashboard();
            page.getPatientsTab().submit();

            assertThat(page.isAlertError()).isTrue();
        }

        @Test
        @DisplayName("Deve exibir erro ao cadastrar paciente com documento duplicado")
        void shouldShowErrorWhenPatientDocumentIsDuplicate() throws Exception {
            String document = faker.numerify("###.###.###-##");
            createPatientViaApi(faker.name().fullName(), document);

            openDashboard();
            PatientsTabPage patients = page.getPatientsTab();
            patients.fillForm(faker.name().fullName(), document, "NONE");
            patients.submit();

            assertThat(page.isAlertError()).isTrue();
        }

        @Test
        @DisplayName("Deve atualizar lista de pacientes ao clicar em Atualizar Lista")
        void shouldUpdatePatientListOnRefresh() throws Exception {
            openDashboard();
            String name = faker.name().fullName();
            createPatientViaApi(name, faker.numerify("###.###.###-##"));

            PatientsTabPage patients = page.getPatientsTab();
            patients.clickRefresh();
            patients.waitForRowCount(1);

            assertThat(patients.listContains(name)).isTrue();
        }
    }

    @Nested
    @DisplayName("Médicos")
    class Doctors {

        @Test
        @DisplayName("Deve carregar lista de médicos automaticamente ao abrir o dashboard")
        void shouldLoadDoctorsAutomaticallyOnDashboardOpen() throws Exception {
            String name = faker.name().fullName();
            createDoctorViaApi(name, faker.numerify("CRM-SP ######"));

            openDashboard();
            DoctorsTabPage doctors = page.clickDoctorsTab();
            doctors.waitForRowCount(1);

            assertThat(doctors.listContains(name)).isTrue();
        }

        @Test
        @DisplayName("Deve cadastrar médico com dados válidos")
        void shouldRegisterDoctorWithValidData() {
            openDashboard();
            String name = faker.name().fullName();
            DoctorsTabPage doctors = page.clickDoctorsTab();
            doctors.fillForm(name, faker.medical().medicineName(), faker.numerify("CRM-SP ######"));
            doctors.submit();

            assertThat(page.isAlertSuccess()).isTrue();
            doctors.waitForRowCount(1);
            assertThat(doctors.listContains(name)).isTrue();
        }

        @Test
        @DisplayName("Deve exibir erro ao cadastrar médico com campos vazios")
        void shouldShowErrorWhenDoctorFieldsAreEmpty() {
            openDashboard();
            page.clickDoctorsTab().submit();

            assertThat(page.isAlertError()).isTrue();
        }

        @Test
        @DisplayName("Deve exibir erro ao cadastrar médico com CRM duplicado")
        void shouldShowErrorWhenDoctorLicenseIsDuplicate() throws Exception {
            String license = faker.numerify("CRM-SP ######");
            createDoctorViaApi(faker.name().fullName(), license);

            openDashboard();
            DoctorsTabPage doctors = page.clickDoctorsTab();
            doctors.fillForm(faker.name().fullName(), "Cardiologia", license);
            doctors.submit();

            assertThat(page.isAlertError()).isTrue();
        }

        @Test
        @DisplayName("Deve atualizar lista de médicos ao clicar em Atualizar Lista")
        void shouldUpdateDoctorListOnRefresh() throws Exception {
            openDashboard();
            String name = faker.name().fullName();
            createDoctorViaApi(name, faker.numerify("CRM-SP ######"));

            DoctorsTabPage doctors = page.clickDoctorsTab();
            doctors.clickRefresh();
            doctors.waitForRowCount(1);

            assertThat(doctors.listContains(name)).isTrue();
        }
    }

    @Nested
    @DisplayName("Procedimentos")
    class Procedures {

        @Test
        @DisplayName("Deve carregar lista de procedimentos automaticamente ao abrir o dashboard")
        void shouldLoadProceduresAutomaticallyOnDashboardOpen() throws Exception {
            String name = faker.medical().medicineName();
            createProcedureViaApi(name);

            openDashboard();
            ProceduresTabPage procedures = page.clickProceduresTab();
            procedures.waitForRowCount(1);

            assertThat(procedures.listContains(name)).isTrue();
        }

        @Test
        @DisplayName("Deve cadastrar procedimento com dados válidos")
        void shouldRegisterProcedureWithValidData() {
            openDashboard();
            String name = faker.medical().medicineName();
            ProceduresTabPage procedures = page.clickProceduresTab();
            procedures.fillForm(name, "250.00");
            procedures.submit();

            assertThat(page.isAlertSuccess()).isTrue();
            procedures.waitForRowCount(1);
            assertThat(procedures.listContains(name)).isTrue();
        }

        @Test
        @DisplayName("Deve exibir erro ao cadastrar procedimento com campos vazios")
        void shouldShowErrorWhenProcedureFieldsAreEmpty() {
            openDashboard();
            page.clickProceduresTab().submit();

            assertThat(page.isAlertError()).isTrue();
        }

        @Test
        @DisplayName("Deve exibir erro ao cadastrar procedimento com custo negativo")
        void shouldShowErrorWhenProcedureCostIsNegative() {
            openDashboard();
            ProceduresTabPage procedures = page.clickProceduresTab();
            procedures.fillForm(faker.medical().medicineName(), "-50.00");
            procedures.submit();

            assertThat(page.isAlertError()).isTrue();
        }

        @Test
        @DisplayName("Deve atualizar lista de procedimentos ao clicar em Atualizar Lista")
        void shouldUpdateProcedureListOnRefresh() throws Exception {
            openDashboard();
            String name = faker.medical().medicineName();
            createProcedureViaApi(name);

            ProceduresTabPage procedures = page.clickProceduresTab();
            procedures.clickRefresh();
            procedures.waitForRowCount(1);

            assertThat(procedures.listContains(name)).isTrue();
        }
    }

    @Nested
    @DisplayName("Atendimentos")
    class Appointments {

        @Test
        @DisplayName("Deve carregar lista de atendimentos automaticamente ao abrir o dashboard")
        void shouldLoadAppointmentsAutomaticallyOnDashboardOpen() throws Exception {
            String patientName = faker.name().fullName();
            String patientId = createPatientViaApi(patientName, faker.numerify("###.###.###-##"));
            String doctorId = createDoctorViaApi(faker.name().fullName(), faker.numerify("CRM-SP ######"));
            createAppointmentViaApi(patientId, doctorId);

            openDashboard();
            AppointmentsTabPage appointments = page.clickAppointmentsTab();
            appointments.waitForRowCount(1);

            assertThat(appointments.listContains(patientName)).isTrue();
        }

        @Test
        @DisplayName("Deve criar atendimento com dados válidos")
        void shouldCreateAppointmentWithValidData() throws Exception {
            String patientName = faker.name().fullName();
            createPatientViaApi(patientName, faker.numerify("###.###.###-##"));
            String doctorName = faker.name().fullName();
            createDoctorViaApi(doctorName, faker.numerify("CRM-SP ######"));

            openDashboard();
            AppointmentsTabPage appointments = page.clickAppointmentsTab();
            appointments.waitForSelectsPopulated();
            appointments.selectPatient(patientName);
            appointments.selectDoctor(doctorName);
            appointments.setDate("2099-12-31T10:00");
            appointments.submit();

            assertThat(page.isAlertSuccess()).isTrue();
            appointments.waitForRowCount(1);
            assertThat(appointments.listContains(patientName)).isTrue();
        }
    }
}
