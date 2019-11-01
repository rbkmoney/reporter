package com.rbkmoney.reporter.event;

import com.rbkmoney.easyway.*;
import com.rbkmoney.reporter.config.*;
import com.rbkmoney.reporter.dao.impl.CommonBatchDao;
import com.rbkmoney.reporter.dao.impl.InvoiceDaoImpl;
import com.rbkmoney.reporter.dao.impl.PaymentDaoImpl;
import com.rbkmoney.reporter.handle.stockevent.PayoutCreatedChangeEventHandler;
import com.rbkmoney.reporter.service.impl.*;
import lombok.extern.slf4j.Slf4j;
import org.junit.ClassRule;
import org.junit.runner.Description;
import org.junit.runner.RunWith;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureJdbc;
import org.springframework.boot.test.context.ConfigFileApplicationContextInitializer;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.testcontainers.containers.FailureDetectingExternalResource;

import java.util.function.Consumer;
import java.util.function.Supplier;

@RunWith(SpringRunner.class)
@ContextConfiguration(
        classes = {
                ApplicationConfig.class,
                CacheConfig.class,
                InvoiceBatchHandlerConfig.class,
                KafkaPaymentMachineEventConfig.class,
                PayoutEventStockConfig.class,
                PayoutCreatedChangeEventHandler.class,
                CommonBatchDao.class,
                InvoiceDaoImpl.class,
                PaymentDaoImpl.class
        },
        initializers = AbstractAppEventServiceTests.Initializer.class
)
@ComponentScan(
        basePackages = {
                "com.rbkmoney.reporter.dao.query",
                "com.rbkmoney.reporter.service",
                "com.rbkmoney.reporter.mapper",
                "com.rbkmoney.reporter.batch"
        },
        excludeFilters = {
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, value = S3StorageServiceImpl.class),
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, value = JobServiceImpl.class),
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, value = ReportServiceImpl.class),
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, value = TaskServiceImpl.class),
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, value = ReportingServiceImpl.class),
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, value = EventServiceImpl.class),
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, value = PaymentRegistryTemplateImpl.class),
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, value = ProvisionOfServiceTemplateImpl.class)
        }
)
@AutoConfigureJdbc
@TestPropertySource("classpath:application.yml")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@Slf4j
public abstract class AbstractAppEventServiceTests extends AbstractTestUtils {

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
            TestPropertyValues.of(testContainers.getEnvironmentProperties(getEnvironmentPropertiesConsumer()))
                    .applyTo(configurableApplicationContext);
        }
    }

    private static Consumer<EnvironmentProperties> getEnvironmentPropertiesConsumer() {
        return environmentProperties -> environmentProperties.put("info.single-instance-mode", "true");
    }


    private static Supplier<TestContainersParameters> getTestContainersParametersSupplier() {
        return () -> {
            TestContainersParameters testContainersParameters = new TestContainersParameters();
            testContainersParameters.setPostgresqlJdbcUrl("jdbc:postgresql://localhost:5432/reporter");

            return testContainersParameters;
        };
    }
}
