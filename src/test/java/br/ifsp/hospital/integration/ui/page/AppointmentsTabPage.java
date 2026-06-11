package br.ifsp.hospital.integration.ui.page;

import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.List;

public class AppointmentsTabPage {

    private final WebDriver driver;
    private final WebDriverWait wait;

    private static final By SECTION      = By.id("appointments");
    private static final By APPT_PATIENT = By.id("appt-patient");
    private static final By APPT_DOCTOR  = By.id("appt-doctor");
    private static final By APPT_DATE    = By.id("appt-date");
    private static final By SUBMIT_BUTTON  = By.xpath("//div[@id='appointments']//button[text()='Criar Atendimento']");
    private static final By REFRESH_BUTTON = By.xpath("//div[@id='appointments']//button[text()='Atualizar Lista']");
    private static final By TABLE_ROWS     = By.cssSelector("#appointments-list tr");

    public AppointmentsTabPage(WebDriver driver, WebDriverWait wait) {
        this.driver = driver;
        this.wait = wait;
    }

    public boolean isActive() {
        return driver.findElement(SECTION).getAttribute("class").contains("active");
    }

    public void waitForSelectsPopulated() {
        wait.until(d -> new Select(d.findElement(APPT_PATIENT)).getOptions().size() > 1);
        wait.until(d -> new Select(d.findElement(APPT_DOCTOR)).getOptions().size() > 1);
    }

    public void selectPatient(String patientName) {
        new Select(driver.findElement(APPT_PATIENT)).selectByVisibleText(patientName);
    }

    public void selectDoctor(String doctorName) {
        new Select(driver.findElement(APPT_DOCTOR)).selectByVisibleText(doctorName);
    }

    public void setDate(String dateTime) {
        WebElement input = driver.findElement(APPT_DATE);
        ((JavascriptExecutor) driver).executeScript("arguments[0].value = arguments[1]", input, dateTime);
    }

    public void submit() {
        driver.findElement(SUBMIT_BUTTON).click();
    }

    public void clickRefresh() {
        driver.findElement(REFRESH_BUTTON).click();
    }

    public List<WebElement> getRows() {
        return driver.findElements(TABLE_ROWS);
    }

    public int getRowCount() {
        return getRows().size();
    }

    public boolean listContains(String text) {
        return getRows().stream().anyMatch(r -> r.getText().contains(text));
    }

    public void waitForRowCount(int count) {
        wait.until(ExpectedConditions.numberOfElementsToBe(TABLE_ROWS, count));
    }

    public void clickCloseInRow(int rowIndex) {
        getRows().get(rowIndex).findElement(By.xpath(".//button[text()='Fechar']")).click();
    }

    public void clickBillInRow(int rowIndex) {
        getRows().get(rowIndex).findElement(By.xpath(".//button[text()='Faturar']")).click();
    }

    public void clickCancelInRow(int rowIndex, String reason) {
        getRows().get(rowIndex).findElement(By.xpath(".//button[text()='Cancelar']")).click();
        Alert prompt = wait.until(ExpectedConditions.alertIsPresent());
        prompt.sendKeys(reason);
        prompt.accept();
    }

    public void clickCancelInRowAndDismiss(int rowIndex) {
        getRows().get(rowIndex).findElement(By.xpath(".//button[text()='Cancelar']")).click();
        Alert prompt = wait.until(ExpectedConditions.alertIsPresent());
        prompt.dismiss();
    }

    public boolean patientSelectContains(String name) {
        return new Select(driver.findElement(APPT_PATIENT)).getOptions().stream()
                .anyMatch(o -> o.getText().equals(name));
    }

    public boolean doctorSelectContains(String name) {
        return new Select(driver.findElement(APPT_DOCTOR)).getOptions().stream()
                .anyMatch(o -> o.getText().equals(name));
    }

    public boolean isProcedureSelectorVisible() {
        List<WebElement> elements = driver.findElements(By.id("appt-procedure"));
        return !elements.isEmpty() && elements.get(0).isDisplayed();
    }

    public boolean isProcedureColumnVisible() {
        List<WebElement> headers = driver.findElements(
                By.cssSelector("#appointments table th"));
        return headers.stream().anyMatch(h -> h.getText().equalsIgnoreCase("Procedimento"));
    }
}
