package br.ifsp.hospital.integration.ui.page;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.List;

public class ProceduresTabPage {

    private final WebDriver driver;
    private final WebDriverWait wait;

    private static final By SECTION         = By.id("procedures");
    private static final By PROCEDURE_NAME  = By.id("procedure-name");
    private static final By PROCEDURE_COST  = By.id("procedure-cost");
    private static final By SUBMIT_BUTTON   = By.xpath("//div[@id='procedures']//button[text()='Cadastrar']");
    private static final By REFRESH_BUTTON  = By.xpath("//div[@id='procedures']//button[text()='Atualizar Lista']");
    private static final By TABLE_ROWS      = By.cssSelector("#procedures-list tr");

    public ProceduresTabPage(WebDriver driver, WebDriverWait wait) {
        this.driver = driver;
        this.wait = wait;
    }

    public boolean isActive() {
        return driver.findElement(SECTION).getAttribute("class").contains("active");
    }

    public void fillForm(String name, String cost) {
        driver.findElement(PROCEDURE_NAME).sendKeys(name);
        driver.findElement(PROCEDURE_COST).sendKeys(cost);
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
