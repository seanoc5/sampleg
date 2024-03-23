package com.oconeco.hacking


import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.openqa.selenium.By
import org.openqa.selenium.WebDriver
import org.openqa.selenium.WebElement
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.chrome.ChromeOptions

Logger log = LogManager.getLogger(this.class.name);
log.info "Starting ${this.class.name}..."

ChromeOptions options = new ChromeOptions();
options.addArguments("headless");
//options.addArguments("window-size=1400,800");
//options.addArguments("disable-gpu")

//WebDriver driver = new HtmlUnitDriver();
WebDriver driver = new ChromeDriver(options);

driver.get("https://intellij-support.jetbrains.com/hc/en-us/community/posts/206151809-Any-way-to-disable-or-change-syntax-error-highlighting-for-the-current-line-");
def title = driver.getTitle()
def body = driver.getPageSource()
log.info "[$title] Page source size: ${body.size()}"



// Navigate to Google
driver.get("http://www.google.com");
// Locate the searchbox using its name
WebElement element = driver.findElement(By.name("q"));

// Enter a search query
element.sendKeys("Guru99");

// Submit the query. Webdriver searches for the form using the text input element automatically
// No need to locate/find the submit button
element.submit();

// This code will print the page title
System.out.println("Page title is: " + driver.getTitle());

driver.quit();
