package com.rbkmoney.reporter.service;

import com.rbkmoney.easyway.AbstractTestUtils;
import com.rbkmoney.easyway.TestContainers;
import com.rbkmoney.easyway.TestContainersBuilder;
import com.rbkmoney.easyway.TestContainersParameters;
import com.rbkmoney.reporter.config.ApplicationConfig;
import com.rbkmoney.reporter.config.SchedulerConfig;
import com.rbkmoney.reporter.config.StorageConfig;
import com.rbkmoney.reporter.scheduler.impl.PendingReportScheduledJobImpl;
import lombok.extern.slf4j.Slf4j;
import org.junit.ClassRule;
import org.junit.runner.Description;
import org.junit.runner.RunWith;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureJdbc;
import org.springframework.boot.test.context.ConfigFileApplicationContextInitializer;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.testcontainers.containers.FailureDetectingExternalResource;

import java.util.function.Supplier;

@RunWith(SpringRunner.class)
@ContextConfiguration(
        classes = {
                ApplicationConfig.class,
                SchedulerConfig.class,
                StorageConfig.class,
                PendingReportScheduledJobImpl.class
        },
        initializers = AbstractAppServiceTests.Initializer.class
)
@ComponentScan(
        basePackages = {
                "com.rbkmoney.reporter.dao",
                "com.rbkmoney.reporter.service",
                "com.rbkmoney.reporter.handle.impl"
        }
)
@AutoConfigureJdbc
@TestPropertySource("classpath:application.yml")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@EnableScheduling
@Slf4j
public abstract class AbstractAppServiceTests extends AbstractTestUtils {

    private static TestContainers testContainers = TestContainersBuilder.builderWithTestContainers(getTestContainersParametersSupplier())
            .addPostgresqlTestContainer()
            .addCephTestContainer()
            .build();

    @ClassRule
    public static final FailureDetectingExternalResource resource = new FailureDetectingExternalResource() {

        @Override
        protected void starting(Description description) {
            testContainers.startTestContainers();
        }

        @Override
        protected void failed(Throwable e, Description description) {
            log.warn("Test Container running was failed ", e);
        }

        @Override
        protected void finished(Description description) {
            testContainers.stopTestContainers();
        }
    };

    public static class Initializer extends ConfigFileApplicationContextInitializer {

        @Override
        public void initialize(ConfigurableApplicationContext configurableApplicationContext) {
            super.initialize(configurableApplicationContext);
            TestPropertyValues.of(
                    testContainers.getEnvironmentProperties(
                            environmentProperties -> {
                            }
                    )
            )
                    .applyTo(configurableApplicationContext);
        }
    }

    private static Supplier<TestContainersParameters> getTestContainersParametersSupplier() {
        return () -> {
            TestContainersParameters testContainersParameters = new TestContainersParameters();
            testContainersParameters.setPostgresqlJdbcUrl("jdbc:postgresql://localhost:5432/reporter");

            return testContainersParameters;
        };
    }
}
