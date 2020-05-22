package com.rbkmoney.reporter.service.invoicing;

import com.rbkmoney.damsel.domain.*;
import com.rbkmoney.damsel.payment_processing.EventPayload;
import com.rbkmoney.damsel.payment_processing.EventRange;
import com.rbkmoney.damsel.payment_processing.InvoicingSrv;
import com.rbkmoney.damsel.payment_processing.UserInfo;
import com.rbkmoney.easyway.TestContainers;
import com.rbkmoney.easyway.TestContainersBuilder;
import com.rbkmoney.easyway.TestContainersParameters;
import com.rbkmoney.machinegun.eventsink.MachineEvent;
import com.rbkmoney.reporter.ReporterApplication;
import com.rbkmoney.reporter.dao.InvoiceDao;
import com.rbkmoney.reporter.dao.PaymentDao;
import com.rbkmoney.reporter.data.InvoicingData;
import com.rbkmoney.reporter.domain.enums.InvoiceStatus;
import com.rbkmoney.reporter.domain.tables.pojos.Invoice;
import com.rbkmoney.reporter.domain.tables.pojos.Payment;
import com.rbkmoney.reporter.service.ReportNewProtoService;
import com.rbkmoney.reporter.service.StorageService;
import com.rbkmoney.reporter.service.impl.InvoicingService;
import com.rbkmoney.reporter.service.impl.TaskServiceImpl;
import com.rbkmoney.sink.common.parser.Parser;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

import static com.rbkmoney.reporter.data.InvoicingData.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = RANDOM_PORT)
@ContextConfiguration(classes = ReporterApplication.class)
@TestPropertySource("classpath:application.yml")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@Slf4j
public class InvoicingServiceTest {

    @Autowired
    private InvoicingService invoicingService;

    @Autowired
    private InvoiceDao invoiceDao;

    @Autowired
    private PaymentDao paymentDao;

    @MockBean
    private Parser<MachineEvent, EventPayload> paymentMachineEventParser;

    @MockBean
    private InvoicingSrv.Iface hgInvoicingService;

    @MockBean
    private ReportNewProtoService reportNewProtoService;

    @MockBean
    private StorageService S3StorageServiceImpl;

    @MockBean
    private TaskServiceImpl taskService;

    private static TestContainers testContainers =
            TestContainersBuilder.builderWithTestContainers(getTestContainersParametersSupplier())
            .addPostgresqlTestContainer()
            .build();

    @Before
    public void init() {
    }

    @Test
    public void addNewInvoiceTest() throws Exception {
        String invoiceId = "inv-1";
        String paymentId = "pay-1";
        when(hgInvoicingService.get(any(UserInfo.class), anyString(), any(EventRange.class)))
                .thenReturn(createHgInvoice(invoiceId, paymentId));

        List<InvoicingData.InvoiceChangeStatusInfo> statusInfoList = new ArrayList<>();
        statusInfoList.add(new InvoicingData.InvoiceChangeStatusInfo(
                1, InvoiceStatus.paid));
        statusInfoList.add(new InvoicingData.InvoiceChangeStatusInfo(
                1, InvoiceStatus.unpaid));

        when(paymentMachineEventParser.parse(any(MachineEvent.class)))
                .thenReturn(createTestInvoiceEventPayload(statusInfoList));

        invoicingService.handleEvents(Arrays.asList(createMachineEvent(invoiceId)));
        List<Invoice> invoices = invoiceDao.getInvoicesByState(
                LocalDateTime.now().minus(10L, ChronoUnit.MINUTES),
                LocalDateTime.now(),
                Arrays.asList(InvoiceStatus.paid, InvoiceStatus.unpaid, InvoiceStatus.cancelled, InvoiceStatus.fulfilled)
        );
        assertEquals("Received count of invoices is not equal to expected", 1, invoices.size());
        Invoice invoice = invoices.get(0);
        assertTrue("Received invoice is not equal to expected", "inv-1".equals(invoice.getInvoiceId())
                && invoice.getStatus() == InvoiceStatus.paid
                && "RUR".equals(invoice.getCurrencyCode())
                && invoice.getAmount() == 1000L);
    }

    @Test
    public void addNewPaymentTest() throws Exception {
        String invoiceId = "inv-1";
        String paymentId = "pay-2";
        when(hgInvoicingService.get(any(UserInfo.class), anyString(), any(EventRange.class)))
                .thenReturn(createHgInvoice(invoiceId, paymentId));

        List<InvoicingData.PaymentChangeStatusInfo> statusInfoList = new ArrayList<>();
        InvoicePaymentStatus captureStatus = new InvoicePaymentStatus();
        captureStatus.setCaptured(new InvoicePaymentCaptured());
        statusInfoList.add(new InvoicingData.PaymentChangeStatusInfo(paymentId, captureStatus));

        InvoicePaymentStatus pendingStatus = new InvoicePaymentStatus();
        pendingStatus.setPending(new InvoicePaymentPending());
        statusInfoList.add(new InvoicingData.PaymentChangeStatusInfo("2", pendingStatus));

        when(paymentMachineEventParser.parse(any(MachineEvent.class)))
                .thenReturn(createTestPaymentEventPayload(statusInfoList));

        invoicingService.handleEvents(Arrays.asList(createMachineEvent(invoiceId)));
        List<Payment> payments = paymentDao.getPaymentsByState(
                LocalDateTime.now().minus(10L, ChronoUnit.MINUTES),
                LocalDateTime.now(),
                Arrays.asList(com.rbkmoney.reporter.domain.enums.InvoicePaymentStatus.captured,
                        com.rbkmoney.reporter.domain.enums.InvoicePaymentStatus.cancelled,
                        com.rbkmoney.reporter.domain.enums.InvoicePaymentStatus.failed)
        );
        assertEquals("Received count of invoices is not equal to expected", 1, payments.size());
        Payment payment = payments.get(0);
        assertTrue("Received payment is not equal to expected",
                invoiceId.equals(payment.getInvoiceId())
                && paymentId.equals(payment.getPaymentId())
                && payment.getStatus() == com.rbkmoney.reporter.domain.enums.InvoicePaymentStatus.captured
                && "RUR".equals(payment.getCurrencyCode())
                && payment.getAmount() == 1000L);
    }

    @Test
    public void addNewRefundTest() {

    }

    @Test
    public void addNewAdjustmentTest() {

    }

    private static Supplier<TestContainersParameters> getTestContainersParametersSupplier() {
        return () -> {
            TestContainersParameters testContainersParameters = new TestContainersParameters();
            testContainersParameters.setPostgresqlJdbcUrl("jdbc:postgresql://localhost:5432/reporter");

            return testContainersParameters;
        };
    }

}
