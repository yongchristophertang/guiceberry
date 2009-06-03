package com.google.inject.testing.guiceberry.tutorial_1_server;

import com.google.inject.Inject;
import com.google.inject.testing.guiceberry.GuiceBerryEnv;
import com.google.inject.testing.guiceberry.junit3.GuiceBerryJunit3TestCase;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

@GuiceBerryEnv(Tutorial1Envs.REGULAR_PET_STORE_AT_8080_ENV)
public class Example0StartingAnHttpServerTest extends GuiceBerryJunit3TestCase {

  @Inject
  WebDriver driver;
  
  public void testMyServlet() {
    WebElement element = driver.findElement(By.xpath("//div[@id='welcome']"));
    assertEquals("Welcome!", element.getText());
  }
}
