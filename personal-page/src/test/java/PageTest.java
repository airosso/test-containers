import org.assertj.core.api.Assertions;
import org.junit.*;
import org.testcontainers.containers.FixedHostPortGenericContainer;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.output.OutputFrame;
import org.testcontainers.containers.wait.strategy.HttpWaitStrategy;

public class PageTest {
    @Rule
    public GenericContainer simpleWebServer
            = new FixedHostPortGenericContainer("burse:1.0-SNAPSHOT")
            .withFixedExposedPort(8080, 8080)
            .withExposedPorts(8080)
            .waitingFor(new HttpWaitStrategy().forPort(8080))
            .withLogConsumer(frame -> {
                System.out.print(((OutputFrame) frame).getUtf8String());
            });

    private PersonalPage page;
    private int userId;

    @Before
    public void before() {
        page = new PersonalPage();
        userId = page.createUser();
    }

    @Test
    public void company_management() throws Exception {
        page.createCompany("Yandex", 200.0, 123.4);
        Assertions.assertThatThrownBy(() -> page.createCompany("Yandex", 100.0, 10.0));
        page.changePrice("Yandex", 150.0);
        page.createCompany("OpenWay", 100.0, 12.3);
    }

    @Test
    public void stock_management() throws Exception {
        page.createCompany("Yandex", 100.0, 12.0);
        page.changeAvailableMoney(userId, 1400.0);
        Assert.assertEquals(1400.0, (double) page.getAllWorth(userId), 1e-6);

        page.buyStocks(userId, "Yandex", 1.0);
        Assert.assertEquals(1400.0, (double) page.getAllWorth(userId), 1e-6);
        Assertions.assertThatThrownBy(() -> page.buyStocks(userId, "Yandex", 13.0));
        page.changePrice("Yandex", 150.0);
        Assert.assertEquals(1450.0, (double) page.getAllWorth(userId), 1e-6);
        Assertions.assertThatThrownBy(() -> page.buyStocks(userId, "Yandex", 11.0));

        page.sellStocks(userId, "Yandex", 0.5);
        Assertions.assertThatThrownBy(() -> page.sellStocks(userId, "OpenWay", 0.5));
        Assertions.assertThatThrownBy(() -> page.sellStocks(userId, "Yandex", 1.0));
    }

    @Test
    public void statistic() throws Exception {
        page.createCompany("Yandex", 100.0, 12.0);
        page.createCompany("OpenWay", 50.0, 5.0);
        page.changeAvailableMoney(userId, 1400.0);
        page.buyStocks(userId, "Yandex", 1.0);
        page.buyStocks(userId, "OpenWay", 1.0);
        System.out.println(page.getAllStocks(userId));
    }
}
