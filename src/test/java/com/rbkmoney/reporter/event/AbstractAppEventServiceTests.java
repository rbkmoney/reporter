package com.rbkmoney.reporter.event;

import com.rbkmoney.easyway.*;
import com.rbkmoney.reporter.ReporterApplication;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.util.EnvironmentTestUtils;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.function.Consumer;
import java.util.function.Supplier;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = RANDOM_PORT)
@ContextConfiguration(classes = ReporterApplication.class, initializers = AbstractAppEventServiceTests.Initializer.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public abstract class AbstractAppEventServiceTests extends AbstractTestUtils {

    private static TestContainers testContainers = TestContainersBuilder.builderWithTestContainers(getTestContainersParametersSupplier())
            .addPostgresqlTestContainer()
            .addCephTestContainer()
            .build();

    @BeforeClass
    public static void beforeClass() {
        testContainers.startTestContainers();
    }

    @AfterClass
    public static void afterClass() {
        testContainers.stopTestContainers();
    }

    public static class Initializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

        @Override
        public void initialize(ConfigurableApplicationContext configurableApplicationContext) {
            EnvironmentTestUtils.addEnvironment(
                    "testcontainers",
                    configurableApplicationContext.getEnvironment(),
                    testContainers.getEnvironmentProperties(getEnvironmentPropertiesConsumer())
            );
        }
    }

    private static Supplier<TestContainersParameters> getTestContainersParametersSupplier() {
        return () -> {
            TestContainersParameters testContainersParameters = new TestContainersParameters();
            testContainersParameters.setPostgresqlJdbcUrl("jdbc:postgresql://localhost:5432/reporter");

            return testContainersParameters;
        };
    }

    private static Consumer<EnvironmentProperties> getEnvironmentPropertiesConsumer() {
        return environmentProperties -> {
            environmentProperties.put("kafka.topics.invoice.enabled", "false");
            environmentProperties.put("bustermaze.payout.polling.enabled", "false");
            environmentProperties.put("bustermaze.payment.polling.enabled", "false");
            environmentProperties.put("jobs.synchronization.enabled", "false");
            environmentProperties.put("jobs.report.enabled", "false");
        };
    }
}
