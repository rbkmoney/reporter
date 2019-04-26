package com.rbkmoney.reporter.kafka;

import com.rbkmoney.AbstractTestUtils;
import com.rbkmoney.TestContainers;
import com.rbkmoney.TestContainersBuilder;
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

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = RANDOM_PORT)
@ContextConfiguration(classes = ReporterApplication.class, initializers = AbstractAppKafkaTests.Initializer.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public abstract class AbstractAppKafkaTests extends AbstractTestUtils {

    private static TestContainers testContainers = TestContainersBuilder.builder(false)
            .addKafkaTestContainer()
            .addPostgreSQLTestContainer()
            // todo in fact unused, rm after
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
                    ReporterTestPropertyValuesBuilder.build(testContainers)
            );
        }
    }
}
