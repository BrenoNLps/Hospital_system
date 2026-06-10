package br.ifsp.hospital.integration.ui.page;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class DashboardPage {

    private final WebDriver driver;
    private final WebDriverWait wait;

    private static final By ALERT            = By.id("alert");
    private static final By LOGOUT_BUTTON    = By.xpath("//button[text()='Sair']");
    private static final By TAB_PATIENTS     = By.xpath("//button[text()='Pacientes']");
    private static final By TAB_DOCTORS      = By.xpath("//button[text()='Médicos']");
    private static final By TAB_PROCEDURES   = By.xpath("//button[text()='Procedimentos']");
    private static final By TAB_APPOINTMENTS = By.xpath("//button[text()='Atendimentos']");

    public DashboardPage(WebDriver driver, WebDriverWait wait) {
        this.driver = driver;
        this.wait = wait;
    }

    public void open(String url) {
        driver.get(url);
    }

    public String getCurrentUrl() {
        return driver.getCurrentUrl();
    }


    //Alerta
    public boolean isAlertDisplayed() {
        return driver.findElement(ALERT).isDisplayed();
    }

    public boolean isAlertSuccess() {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(ALERT))
                .getAttribute("class").contains("success");
    }

    public boolean isAlertError() {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(ALERT))
                .getAttribute("class").contains("error");
    }

    public String getAlertText() {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(ALERT)).getText();
    }


    //Navegação
    public void clickLogout() {
        driver.findElement(LOGOUT_BUTTON).click();
    }

    public void waitForRedirectToIndex() {
        wait.until(ExpectedConditions.urlContains("index.html"));
    }

    public PatientsTabPage clickPatientsTab() {
        driver.findElement(TAB_PATIENTS).click();
        return new PatientsTabPage(driver, wait);
    }

    public DoctorsTabPage clickDoctorsTab() {
        driver.findElement(TAB_DOCTORS).click();
        return new DoctorsTabPage(driver, wait);
    }

    public ProceduresTabPage clickProceduresTab() {
        driver.findElement(TAB_PROCEDURES).click();
        return new ProceduresTabPage(driver, wait);
    }

    public AppointmentsTabPage clickAppointmentsTab() {
        driver.findElement(TAB_APPOINTMENTS).click();
        return new AppointmentsTabPage(driver, wait);
    }

    public PatientsTabPage getPatientsTab() {
        return new PatientsTabPage(driver, wait);
    }
}
