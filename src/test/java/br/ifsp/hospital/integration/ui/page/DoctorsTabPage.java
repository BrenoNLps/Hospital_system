package br.ifsp.hospital.integration.ui.page;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.List;

public class DoctorsTabPage {

    private final WebDriver driver;
    private final WebDriverWait wait;

    private static final By SECTION          = By.id("doctors");
    private static final By DOCTOR_NAME      = By.id("doctor-name");
    private static final By DOCTOR_SPECIALTY = By.id("doctor-specialty");
    private static final By DOCTOR_LICENSE   = By.id("doctor-license");
    private static final By SUBMIT_BUTTON    = By.xpath("//div[@id='doctors']//button[text()='Cadastrar']");
    private static final By REFRESH_BUTTON   = By.xpath("//div[@id='doctors']//button[text()='Atualizar Lista']");
    private static final By TABLE_ROWS       = By.cssSelector("#doctors-list tr");

    public DoctorsTabPage(WebDriver driver, WebDriverWait wait) {
        this.driver = driver;
        this.wait = wait;
    }

    public boolean isActive() {
        return driver.findElement(SECTION).getAttribute("class").contains("active");
    }

    public void fillForm(String name, String specialty, String license) {
        driver.findElement(DOCTOR_NAME).sendKeys(name);
        driver.findElement(DOCTOR_SPECIALTY).sendKeys(specialty);
        driver.findElement(DOCTOR_LICENSE).sendKeys(license);
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
}
