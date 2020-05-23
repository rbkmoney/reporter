package com.rbkmoney.reporter.service.invoicing;

import com.rbkmoney.damsel.domain.*;
import com.rbkmoney.damsel.payment_processing.EventPayload;
import com.rbkmoney.damsel.payment_processing.EventRange;
import com.rbkmoney.damsel.payment_processing.InvoicingSrv;
import com.rbkmoney.damsel.payment_processing.UserInfo;
import com.rbkmoney.machinegun.eventsink.MachineEvent;
import com.rbkmoney.reporter.config.AbstractInvoicingServiceConfig;
import com.rbkmoney.reporter.dao.AdjustmentDao;
import com.rbkmoney.reporter.dao.InvoiceDao;
import com.rbkmoney.reporter.dao.PaymentDao;
import com.rbkmoney.reporter.dao.RefundDao;
import com.rbkmoney.reporter.data.InvoicingData;
import com.rbkmoney.reporter.domain.enums.AdjustmentStatus;
import com.rbkmoney.reporter.domain.enums.InvoiceStatus;
import com.rbkmoney.reporter.domain.enums.RefundStatus;
import com.rbkmoney.reporter.domain.tables.pojos.Adjustment;
import com.rbkmoney.reporter.domain.tables.pojos.Invoice;
import com.rbkmoney.reporter.domain.tables.pojos.Payment;
import com.rbkmoney.reporter.domain.tables.pojos.Refund;
import com.rbkmoney.reporter.service.impl.InvoicingService;
import com.rbkmoney.sink.common.parser.Parser;
import org.apache.thrift.TException;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.rbkmoney.reporter.data.InvoicingData.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

public class InvoicingServiceTest extends AbstractInvoicingServiceConfig {

    @Autowired
    private InvoicingService invoicingService;

    @Autowired
    private InvoiceDao invoiceDao;

    @Autowired
    private PaymentDao paymentDao;

    @Autowired
    private RefundDao refundDao;

    @Autowired
    private AdjustmentDao adjustmentDao;

    @MockBean
    private Parser<MachineEvent, EventPayload> paymentMachineEventParser;

    @MockBean
    private InvoicingSrv.Iface hgInvoicingService;

    private static final String INVOICE_ID = "inv-1";
    private static final String PAYMENT_ID = "pay-1";
    private static final String REFUND_ID = "ref-1";
    private static final String ADJUSTMENT_ID = "adj-1";

    @Before
    public void init() throws TException {
        when(hgInvoicingService.get(any(UserInfo.class), anyString(), any(EventRange.class)))
                .thenReturn(createHgInvoice(INVOICE_ID, PAYMENT_ID, REFUND_ID, ADJUSTMENT_ID));
    }

    @Test
    public void addNewInvoiceTest() throws Exception {
        List<InvoicingData.InvoiceChangeStatusInfo> statusInfoList = new ArrayList<>();
        statusInfoList.add(new InvoicingData.InvoiceChangeStatusInfo(
                1, InvoiceStatus.paid));
        statusInfoList.add(new InvoicingData.InvoiceChangeStatusInfo(
                1, InvoiceStatus.unpaid));

        when(paymentMachineEventParser.parse(any(MachineEvent.class)))
                .thenReturn(createTestInvoiceEventPayload(statusInfoList));

        invoicingService.handleEvents(Arrays.asList(createMachineEvent(INVOICE_ID)));
        List<Invoice> invoices = invoiceDao.getInvoicesByState(
                LocalDateTime.now().minus(10L, ChronoUnit.MINUTES),
                LocalDateTime.now(),
                Arrays.asList(InvoiceStatus.paid, InvoiceStatus.unpaid, InvoiceStatus.cancelled, InvoiceStatus.fulfilled)
        );
        assertEquals("Received count of invoices is not equal to expected", 1, invoices.size());
        Invoice invoice = invoices.get(0);
        assertTrue("Received invoice is not equal to expected",
                INVOICE_ID.equals(invoice.getInvoiceId())
                && invoice.getStatus() == InvoiceStatus.paid
                && "RUR".equals(invoice.getCurrencyCode())
                && invoice.getAmount() == 1000L);
    }

    @Test
    public void addNewPaymentTest() throws Exception {
        List<InvoicingData.PaymentChangeStatusInfo> statusInfoList = new ArrayList<>();
        InvoicePaymentStatus captureStatus = new InvoicePaymentStatus();
        captureStatus.setCaptured(new InvoicePaymentCaptured());
        statusInfoList.add(new InvoicingData.PaymentChangeStatusInfo(PAYMENT_ID, captureStatus));

        InvoicePaymentStatus pendingStatus = new InvoicePaymentStatus();
        pendingStatus.setPending(new InvoicePaymentPending());
        statusInfoList.add(new InvoicingData.PaymentChangeStatusInfo("2", pendingStatus));

        when(paymentMachineEventParser.parse(any(MachineEvent.class)))
                .thenReturn(createTestPaymentEventPayload(statusInfoList));

        invoicingService.handleEvents(Arrays.asList(createMachineEvent(INVOICE_ID)));
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
                INVOICE_ID.equals(payment.getInvoiceId())
                && PAYMENT_ID.equals(payment.getPaymentId())
                && payment.getStatus() == com.rbkmoney.reporter.domain.enums.InvoicePaymentStatus.captured
                && "RUR".equals(payment.getCurrencyCode())
                && payment.getAmount() == 1000L);
    }

    @Test
    public void addNewRefundTest() throws Exception {
        List<InvoicingData.RefundChangeStatusInfo> statusInfoList = new ArrayList<>();
        var captureStatus = new com.rbkmoney.damsel.domain.InvoicePaymentRefundStatus();
        captureStatus.setSucceeded(new InvoicePaymentRefundSucceeded());
        statusInfoList.add(new InvoicingData.RefundChangeStatusInfo(PAYMENT_ID, REFUND_ID, captureStatus));
        var pendingStatus = new com.rbkmoney.damsel.domain.InvoicePaymentRefundStatus();
        pendingStatus.setPending(new InvoicePaymentRefundPending());
        statusInfoList.add(new InvoicingData.RefundChangeStatusInfo(PAYMENT_ID, "2", pendingStatus));
        when(paymentMachineEventParser.parse(any(MachineEvent.class)))
                .thenReturn(createTestRefundEventPayload(statusInfoList));

        invoicingService.handleEvents(Arrays.asList(createMachineEvent(INVOICE_ID)));

        List<Refund> refunds = refundDao.getRefundsByState(
                LocalDateTime.now().minus(10L, ChronoUnit.MINUTES),
                LocalDateTime.now(),
                Arrays.asList(RefundStatus.failed, RefundStatus.succeeded)
        );
        assertEquals("Received count of refunds is not equal to expected", 1, refunds.size());

        Refund refund = refunds.get(0);
        assertTrue("Received refund is not equal to expected",
                INVOICE_ID.equals(refund.getInvoiceId())
                        && PAYMENT_ID.equals(refund.getPaymentId())
                        && REFUND_ID.equals(refund.getRefundId())
                        && refund.getStatus() == RefundStatus.succeeded
                        && "RUR".equals(refund.getCurrencyCode())
                        && refund.getAmount() == 1000L);
    }

    @Test
    public void addNewAdjustmentTest() throws Exception {
        List<InvoicingData.AdjustmentChangeStatusInfo> statusInfoList = new ArrayList<>();
        var captureStatus = new com.rbkmoney.damsel.domain.InvoicePaymentAdjustmentStatus();
        captureStatus.setCaptured(new InvoicePaymentAdjustmentCaptured());
        statusInfoList.add(new InvoicingData.AdjustmentChangeStatusInfo(PAYMENT_ID, ADJUSTMENT_ID, captureStatus));

        var pengingStatus = new com.rbkmoney.damsel.domain.InvoicePaymentAdjustmentStatus();
        pengingStatus.setPending(new InvoicePaymentAdjustmentPending());
        statusInfoList.add(new InvoicingData.AdjustmentChangeStatusInfo(PAYMENT_ID, ADJUSTMENT_ID, pengingStatus));

        when(paymentMachineEventParser.parse(any(MachineEvent.class)))
                .thenReturn(createTestAdjustmentEventPayload(statusInfoList));

        invoicingService.handleEvents(Arrays.asList(createMachineEvent(INVOICE_ID)));

        List<Adjustment> adjustments = adjustmentDao.getAdjustmentsByState(
                LocalDateTime.now().minus(10L, ChronoUnit.MINUTES),
                LocalDateTime.now(),
                Arrays.asList(AdjustmentStatus.cancelled, AdjustmentStatus.captured)
        );
        assertEquals("Received count of adjustments is not equal to expected", 1, adjustments.size());

        Adjustment adjustment = adjustments.get(0);
        assertTrue("Received adjustment is not equal to expected",
                INVOICE_ID.equals(adjustment.getInvoiceId())
                        && PAYMENT_ID.equals(adjustment.getPaymentId())
                        && ADJUSTMENT_ID.equals(adjustment.getAdjustmentId())
                        && adjustment.getStatus() == AdjustmentStatus.captured
                        && "RUR".equals(adjustment.getCurrencyCode())
                        && adjustment.getAmount() == 2418L);
    }

}
