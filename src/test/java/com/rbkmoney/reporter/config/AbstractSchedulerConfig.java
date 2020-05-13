package com.rbkmoney.reporter.config;

import com.rbkmoney.easyway.AbstractTestUtils;
import com.rbkmoney.easyway.TestContainers;
import com.rbkmoney.easyway.TestContainersBuilder;
import com.rbkmoney.easyway.TestContainersParameters;
import com.rbkmoney.reporter.dao.impl.ContractMetaDaoImpl;
import com.rbkmoney.reporter.dao.impl.ReportDaoImpl;
import com.rbkmoney.reporter.job.GenerateReportJob;
import com.rbkmoney.reporter.service.DomainConfigService;
import com.rbkmoney.reporter.service.ReportService;
import com.rbkmoney.reporter.service.impl.DomainConfigServiceImpl;
import com.rbkmoney.reporter.service.impl.PartyServiceImpl;
import com.rbkmoney.reporter.service.impl.TaskServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.junit.ClassRule;
import org.junit.runner.Description;
import org.junit.runner.RunWith;
import org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.JdbcTemplateAutoConfiguration;
import org.springframework.boot.autoconfigure.quartz.QuartzAutoConfiguration;
import org.springframework.boot.autoconfigure.transaction.TransactionAutoConfiguration;
import org.springframework.boot.test.context.ConfigFileApplicationContextInitializer;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.testcontainers.containers.FailureDetectingExternalResource;

import java.util.function.Supplier;

@RunWith(SpringRunner.class)
@EnableScheduling
@ContextConfiguration(
        classes = {
                DataSourceAutoConfiguration.class,
                JdbcTemplateAutoConfiguration.class,
                TransactionAutoConfiguration.class,
                DataSourceTransactionManagerAutoConfiguration.class,
                FlywayAutoConfiguration.class,
                QuartzAutoConfiguration.class,
                ApplicationConfig.class,
                ReportDaoImpl.class,
                ContractMetaDaoImpl.class,
                PartyServiceImpl.class,
                ReportService.class,
                GenerateReportJob.class,
                DomainConfigServiceImpl.class,
                TaskServiceImpl.class,
        },
        initializers = AbstractSchedulerConfig.Initializer.class
)
@TestPropertySource("classpath:application.yml")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@Slf4j
public abstract class AbstractSchedulerConfig extends AbstractTestUtils {

    private static TestContainers testContainers = TestContainersBuilder.builderWithTestContainers(getTestContainersParametersSupplier())
            .addPostgresqlTestContainer()
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
