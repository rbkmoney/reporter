package com.rbkmoney.reporter.event;

import com.rbkmoney.easyway.AbstractTestUtils;
import com.rbkmoney.easyway.TestContainers;
import com.rbkmoney.easyway.TestContainersBuilder;
import com.rbkmoney.easyway.TestContainersParameters;
import com.rbkmoney.reporter.config.KafkaPaymentMachineEventConfig;
import com.rbkmoney.reporter.config.PayoutEventStockConfig;
import com.rbkmoney.reporter.handle.machineevent.AdjustmentCreatedChangeMachineEventHandler;
import com.rbkmoney.reporter.handle.machineevent.InvoiceCreatedChangeMachineEventHandler;
import com.rbkmoney.reporter.handle.machineevent.PaymentStartedChangeMachineEventHandler;
import com.rbkmoney.reporter.handle.machineevent.RefundCreatedChangeMachineEventHandler;
import com.rbkmoney.reporter.handle.stockevent.PayoutCreatedChangeEventHandler;
import com.rbkmoney.reporter.service.impl.*;
import lombok.extern.slf4j.Slf4j;
import org.junit.ClassRule;
import org.junit.runner.Description;
import org.junit.runner.RunWith;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureJdbc;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.testcontainers.containers.FailureDetectingExternalResource;

import java.util.function.Supplier;

@RunWith(SpringRunner.class)
@ComponentScan({"com.rbkmoney.reporter.dao"})
@ContextConfiguration(
        classes = {
                PayoutEventStockConfig.class,
                KafkaPaymentMachineEventConfig.class,
                AdjustmentCreatedChangeMachineEventHandler.class,
                AdjustmentServiceImpl.class,
                PaymentServiceImpl.class,
                InvoiceCreatedChangeMachineEventHandler.class,
                InvoiceServiceImpl.class,
                RefundCreatedChangeMachineEventHandler.class,
                RefundServiceImpl.class,
                PayoutCreatedChangeEventHandler.class,
                PayoutServiceImpl.class,
                PaymentStartedChangeMachineEventHandler.class
        },
        initializers = AbstractAppEventServiceTests.Initializer.class
)
@AutoConfigureJdbc
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

    public static class Initializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

        @Override
        public void initialize(ConfigurableApplicationContext configurableApplicationContext) {
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
