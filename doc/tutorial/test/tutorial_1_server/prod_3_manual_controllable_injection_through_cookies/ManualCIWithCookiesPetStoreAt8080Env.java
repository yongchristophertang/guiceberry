package tutorial_1_server.prod_3_manual_controllable_injection_through_cookies;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.inject.AbstractModule;
import com.google.inject.Module;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.servlet.ServletModule;
import com.google.inject.testing.guiceberry.NoOpTestScopeListener;
import com.google.inject.testing.guiceberry.TestId;
import com.google.inject.testing.guiceberry.TestScopeListener;
import com.google.inject.testing.guiceberry.junit3.GuiceBerryJunit3Env;

import org.openqa.selenium.Cookie;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;

import tutorial_1_server.prod_0_simple.MyPetStoreServer;
import tutorial_1_server.prod_0_simple.PetOfTheMonth;
import tutorial_1_server.prod_0_simple.PortNumber;

import java.util.List;
import java.util.Map;
import java.util.Random;

public final class ManualCIWithCookiesPetStoreAt8080Env extends GuiceBerryJunit3Env {
  
  @Provides
  @PortNumber
  int getPortNumber(MyPetStoreServer server) {
    return server.getPortNumber();
  }
  
  @Provides
  WebDriver getWebDriver(@PortNumber int portNumber, TestId testId) {
    WebDriver driver = new HtmlUnitDriver();
    // !!! HERE !!!
    driver.get("http://localhost:" + portNumber);
    driver.manage().addCookie(
        new Cookie(TestId.COOKIE_NAME, testId.toString()));
    return driver;
  }
  
  @Provides
  @Singleton
  MyPetStoreServer startServer() {
    MyPetStoreServer result = new MyPetStoreServer(8080) {
      @Override
      protected List<? extends Module> getModules() {
        return Lists.newArrayList(
            new PetStoreModuleWithTestIdBasedOverride(),
            new ServletModule());
      }
    };
    result.start();
    return result;
  }
  
  @Override
  protected Class<? extends TestScopeListener> getTestScopeListener() {
    return NoOpTestScopeListener.class;
  }
  
  public static final class PetStoreModuleWithTestIdBasedOverride extends AbstractModule {

    public static final Map<TestId, PetOfTheMonth> override = Maps.newHashMap();

    @Provides
    // !!!HERE!!!!
    PetOfTheMonth getPetOfTheMonth(TestId testId) {
      PetOfTheMonth petOfTheMonth = override.get(testId);
      if (petOfTheMonth != null) {
        return petOfTheMonth;
      }
      return somePetOfTheMonth();
    }

    private final Random rand = new Random();

    /** Simulates a call to a non-deterministic service -- maybe an external
     * server, maybe a DB call to a volatile entry, etc.
     */
    private PetOfTheMonth somePetOfTheMonth() {
      PetOfTheMonth[] allPetsOfTheMonth = PetOfTheMonth.values();
      return allPetsOfTheMonth[(rand.nextInt(allPetsOfTheMonth.length))];
    }

    @Override
    protected void configure() {
      install(new ServletModule());
    }
  }
}