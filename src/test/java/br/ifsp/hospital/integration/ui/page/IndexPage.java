package br.ifsp.hospital.integration.ui.page;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.Map;

public class IndexPage {

    private final WebDriver driver;
    private final WebDriverWait wait;

    private static final By LOGIN_FORM     = By.id("login");
    private static final By REGISTER_FORM  = By.id("register");
    private static final By ACTIVE_TAB     = By.cssSelector(".tab.active");
    private static final By LOGIN_TAB      = By.xpath("//button[text()='Login']");
    private static final By REGISTER_TAB   = By.xpath("//button[text()='Registrar']");
    private static final By LOGIN_EMAIL    = By.id("login-email");
    private static final By LOGIN_PASSWORD = By.id("login-password");
    private static final By LOGIN_BUTTON   = By.id("btn-login");
    private static final By REG_NAME       = By.id("reg-name");
    private static final By REG_LASTNAME   = By.id("reg-lastname");
    private static final By REG_EMAIL      = By.id("reg-email");
    private static final By REG_PASSWORD   = By.id("reg-password");
    private static final By REG_BUTTON     = By.id("btn-register");
    private static final By ALERT          = By.id("alert");

    public IndexPage(WebDriver driver, WebDriverWait wait) {
        this.driver = driver;
        this.wait = wait;
    }

    public void open(String url) {
        driver.get(url);
    }

    public void injectTokenAndRefresh(String token) {
        ((JavascriptExecutor) driver).executeScript("localStorage.setItem('token', '" + token + "')");
        driver.navigate().refresh();
    }

    public boolean isLoginTabActive() {
        return driver.findElement(LOGIN_FORM).getAttribute("class").contains("active");
    }

    public String getActiveTabText() {
        return driver.findElement(ACTIVE_TAB).getText();
    }

    public void clickLoginTab() {
        driver.findElement(LOGIN_TAB).click();
    }

    public void clickRegisterTab() {
        driver.findElement(REGISTER_TAB).click();
    }

    public boolean isRegisterFormActive() {
        return driver.findElement(REGISTER_FORM).getAttribute("class").contains("active");
    }

    public void fillLoginForm(String email, String password) {
        driver.findElement(LOGIN_EMAIL).sendKeys(email);
        driver.findElement(LOGIN_PASSWORD).sendKeys(password);
    }

    public void submitLogin() {
        driver.findElement(LOGIN_BUTTON).click();
    }

    public void fillRegisterForm(String name, String lastname, String email, String password) {
        driver.findElement(REG_NAME).sendKeys(name);
        driver.findElement(REG_LASTNAME).sendKeys(lastname);
        driver.findElement(REG_EMAIL).sendKeys(email);
        driver.findElement(REG_PASSWORD).sendKeys(password);
    }

    public void submitRegister() {
        driver.findElement(REG_BUTTON).click();
    }

    public void waitForDashboardRedirect() {
        wait.until(ExpectedConditions.urlContains("dashboard.html"));
    }

    public String getCurrentUrl() {
        return driver.getCurrentUrl();
    }

    public boolean isAlertDisplayed() {
        return driver.findElement(ALERT).isDisplayed();
    }

    public boolean isAlertError() {
        WebElement alert = wait.until(ExpectedConditions.visibilityOfElementLocated(ALERT));
        return alert.getAttribute("class").contains("error");
    }

    public boolean isAlertSuccess() {
        WebElement alert = wait.until(ExpectedConditions.visibilityOfElementLocated(ALERT));
        return alert.getAttribute("class").contains("success");
    }

    public String getAlertText() {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(ALERT)).getText();
    }

    public void waitForLoginTabActive() {
        wait.until(ExpectedConditions.attributeContains(LOGIN_FORM, "class", "active"));
    }

    public boolean isLoginTabVisible() {
        return isWithinViewport(driver.findElement(LOGIN_TAB));
    }

    public boolean isRegisterTabVisible() {
        return isWithinViewport(driver.findElement(REGISTER_TAB));
    }

    public boolean isLoginEmailVisible() {
        return isWithinViewport(driver.findElement(LOGIN_EMAIL));
    }

    public boolean isLoginPasswordVisible() {
        return isWithinViewport(driver.findElement(LOGIN_PASSWORD));
    }

    public boolean isLoginButtonVisible() {
        return isWithinViewport(driver.findElement(LOGIN_BUTTON));
    }

    private boolean isWithinViewport(WebElement element) {
        Map<String, Object> rect = (Map<String, Object>) ((JavascriptExecutor) driver).executeScript(
                "var r = arguments[0].getBoundingClientRect();" +
                "return {left: r.left, right: r.right, top: r.top, bottom: r.bottom};", element);
        long windowWidth = (long) ((JavascriptExecutor) driver).executeScript("return window.innerWidth;");
        long windowHeight = (long) ((JavascriptExecutor) driver).executeScript("return window.innerHeight;");
        double left = ((Number) rect.get("left")).doubleValue();
        double right = ((Number) rect.get("right")).doubleValue();
        double top = ((Number) rect.get("top")).doubleValue();
        double bottom = ((Number) rect.get("bottom")).doubleValue();
        return left >= 0 && right <= windowWidth && top >= 0 && bottom <= windowHeight;
    }

    public double getLoginEmailFontSizePx() {
        return parseFontSize(driver.findElement(LOGIN_EMAIL).getCssValue("font-size"));
    }

    public double getLoginButtonFontSizePx() {
        return parseFontSize(driver.findElement(LOGIN_BUTTON).getCssValue("font-size"));
    }

    private double parseFontSize(String value) {
        return Double.parseDouble(value.replace("px", ""));
    }
}
