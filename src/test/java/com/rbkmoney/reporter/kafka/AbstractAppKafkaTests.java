package com.rbkmoney.reporter.kafka;

import com.rbkmoney.easyway.*;
import com.rbkmoney.reporter.batch.InvoiceBatchManager;
import com.rbkmoney.reporter.config.KafkaConsumerConfig;
import com.rbkmoney.reporter.config.KafkaPaymentMachineEventConfig;
import com.rbkmoney.reporter.config.properties.KafkaSslProperties;
import com.rbkmoney.reporter.dao.mapper.dto.PartyData;
import com.rbkmoney.reporter.dao.mapper.dto.PaymentPartyData;
import com.rbkmoney.reporter.handle.InvoiceBatchHandler;
import com.rbkmoney.reporter.listener.machineevent.PaymentEventsMessageListener;
import com.rbkmoney.reporter.service.BatchService;
import lombok.extern.slf4j.Slf4j;
import org.junit.ClassRule;
import org.junit.runner.Description;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.ConfigFileApplicationContextInitializer;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ConfigurableApplicationContext;
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
                KafkaSslProperties.class,
                KafkaConsumerConfig.class,
                KafkaPaymentMachineEventConfig.class,
                PaymentEventsMessageListener.class
        },
        initializers = AbstractAppKafkaTests.Initializer.class
)
@TestPropertySource("classpath:application.yml")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@Slf4j
public abstract class AbstractAppKafkaTests extends AbstractTestUtils {

    private static TestContainers testContainers = TestContainersBuilder.builderWithTestContainers(getTestContainersParametersSupplier())
            .addKafkaTestContainer()
            .build();

    @MockBean
    private InvoiceBatchManager invoiceBatchManager;

    @MockBean
    private BatchService batchService;

    @MockBean
    private InvoiceBatchHandler<PartyData, Void> invoiceBatchHandler;

    @MockBean
    private InvoiceBatchHandler<PaymentPartyData, PartyData> paymentInvoiceBatchHandler;

    @MockBean(name = "adjustmentInvoiceBatchHandler")
    private InvoiceBatchHandler<Void, PaymentPartyData> adjustmentInvoiceBatchHandler;

    @MockBean(name = "refundInvoiceBatchHandler")
    private InvoiceBatchHandler<Void, PaymentPartyData> refundInvoiceBatchHandler;

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
                            getEnvironmentPropertiesConsumer()
                    )
            )
                    .applyTo(configurableApplicationContext);
        }
    }

    private static Consumer<EnvironmentProperties> getEnvironmentPropertiesConsumer() {
        return environmentProperties -> environmentProperties.put("info.single-instance-mode", "false");
    }

    private static Supplier<TestContainersParameters> getTestContainersParametersSupplier() {
        return () -> {
            TestContainersParameters testContainersParameters = new TestContainersParameters();
            testContainersParameters.setPostgresqlJdbcUrl("jdbc:postgresql://localhost:5432/reporter");

            return testContainersParameters;
        };
    }
}
