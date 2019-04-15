package com.rbkmoney.reporter.event;

import com.rbkmoney.damsel.domain.Invoice;
import com.rbkmoney.damsel.domain.InvoicePayment;
import com.rbkmoney.damsel.domain.InvoicePaymentAdjustmentPending;
import com.rbkmoney.damsel.domain.InvoicePaymentPending;
import com.rbkmoney.damsel.domain.InvoicePaymentRefundPending;
import com.rbkmoney.damsel.domain.*;
import com.rbkmoney.damsel.event_stock.SourceEvent;
import com.rbkmoney.damsel.event_stock.StockEvent;
import com.rbkmoney.damsel.payment_processing.Event;
import com.rbkmoney.damsel.payment_processing.EventPayload;
import com.rbkmoney.damsel.payment_processing.EventSource;
import com.rbkmoney.damsel.payment_processing.*;
import com.rbkmoney.damsel.payout_processing.Wallet;
import com.rbkmoney.damsel.payout_processing.*;
import com.rbkmoney.reporter.dao.*;
import com.rbkmoney.reporter.domain.tables.pojos.Payment;
import com.rbkmoney.reporter.handler.EventStockClientHandler;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static io.github.benas.randombeans.api.EnhancedRandom.random;
import static java.util.Collections.singletonList;
import static org.junit.Assert.assertNotNull;

public class EventServiceTests extends AbstractAppEventServiceTests {

    @Autowired
    private EventStockClientHandler clientHandler;

    @Autowired
    private AdjustmentDao adjustmentDao;

    @Autowired
    private InvoiceDao invoiceDao;

    @Autowired
    private PaymentDao paymentDao;

    @Autowired
    private RefundDao refundDao;

    @Autowired
    private PayoutDao payoutDao;

    @Test
    @Sql("classpath:data/sql/truncate.sql")
    public void adjustmentEventServiceTest() throws Exception {
        InvoicePaymentAdjustment adjustment = new InvoicePaymentAdjustment();
        adjustment.setId(generateString());
        adjustment.setStatus(InvoicePaymentAdjustmentStatus.pending(new InvoicePaymentAdjustmentPending()));
        adjustment.setCreatedAt(generateDate());
        adjustment.setDomainRevision(generateLong());
        adjustment.setReason(generateString());
        adjustment.setNewCashFlow(getCashFlows());
        adjustment.setOldCashFlowInverse(getCashFlows());

        InvoicePaymentAdjustmentCreated invoicePaymentAdjustmentCreated = new InvoicePaymentAdjustmentCreated();
        invoicePaymentAdjustmentCreated.setAdjustment(adjustment);

        InvoicePaymentAdjustmentChange invoicePaymentAdjustmentChange = new InvoicePaymentAdjustmentChange();
        invoicePaymentAdjustmentChange.setId(generateString());
        invoicePaymentAdjustmentChange.setPayload(InvoicePaymentAdjustmentChangePayload.invoice_payment_adjustment_created(invoicePaymentAdjustmentCreated));

        InvoicePaymentChange invoicePaymentChange = new InvoicePaymentChange();
        invoicePaymentChange.setId(generateString());
        invoicePaymentChange.setPayload(InvoicePaymentChangePayload.invoice_payment_adjustment_change(invoicePaymentAdjustmentChange));

        List<InvoiceChange> invoiceChanges = new ArrayList<>();
        invoiceChanges.add(InvoiceChange.invoice_payment_change(invoicePaymentChange));

        Event event = new Event();
        event.setId(generateLong());
        event.setCreatedAt(generateDate());
        event.setSource(EventSource.invoice_id(generateString()));
        event.setPayload(EventPayload.invoice_changes(invoiceChanges));

        StockEvent stockEvent = new StockEvent();
        stockEvent.setSourceEvent(SourceEvent.processing_event(event));

        String adjustmentId = invoicePaymentAdjustmentChange.getId();
        String paymentId = invoicePaymentChange.getId();
        String invoiceId = event.getSource().getInvoiceId();

        Payment payment = random(Payment.class, "paymentCashFlow");
        payment.setInvoiceId(invoiceId);
        payment.setPaymentId(paymentId);
        payment.setId(null);
        payment.setCurrent(true);
        paymentDao.save(payment);

        clientHandler.handle(stockEvent, "");

        assertNotNull(adjustmentDao.get(invoiceId, paymentId, adjustmentId));
    }

    @Test
    @Sql("classpath:data/sql/truncate.sql")
    public void invoiceEventServiceTest() throws Exception {
        InvoiceDetails details = random(InvoiceDetails.class, "cart");

        Invoice invoice = random(Invoice.class, "status", "details", "context");
        invoice.setStatus(InvoiceStatus.paid(new InvoicePaid()));
        invoice.setDetails(details);
        invoice.setOwnerId(UUID.randomUUID().toString());
        invoice.setCreatedAt(generateDate());
        invoice.setDue(generateDate());

        InvoiceCreated invoiceCreated = new InvoiceCreated();
        invoiceCreated.setInvoice(invoice);

        List<InvoiceChange> invoiceChanges = new ArrayList<>();
        invoiceChanges.add(InvoiceChange.invoice_created(invoiceCreated));

        Event event = new Event();
        event.setId(generateLong());
        event.setCreatedAt(generateDate());
        event.setSource(EventSource.invoice_id(generateString()));
        event.setPayload(EventPayload.invoice_changes(invoiceChanges));

        StockEvent stockEvent = new StockEvent();
        stockEvent.setSourceEvent(SourceEvent.processing_event(event));

        String invoiceId = event.getSource().getInvoiceId();

        clientHandler.handle(stockEvent, "");

        assertNotNull(invoiceDao.get(invoiceId));
    }

    @Test
    @Sql("classpath:data/sql/truncate.sql")
    public void paymentEventServiceTest() throws Exception {
        PaymentTerminal paymentTerminal = new PaymentTerminal();
        paymentTerminal.setTerminalType(TerminalPaymentProvider.euroset);

        ContactInfo contactInfo = new ContactInfo();

        CustomerPayer customerPayer = new CustomerPayer();
        customerPayer.setPaymentTool(PaymentTool.payment_terminal(paymentTerminal));
        customerPayer.setContactInfo(contactInfo);

        InvoicePayment payment = random(InvoicePayment.class, "status", "payer", "flow", "context");
        payment.setStatus(InvoicePaymentStatus.pending(new InvoicePaymentPending()));
        payment.setCreatedAt(generateDate());
        payment.setPayer(Payer.customer(customerPayer));
        payment.setFlow(InvoicePaymentFlow.instant(new InvoicePaymentFlowInstant()));

        InvoicePaymentStarted invoicePaymentStarted = new InvoicePaymentStarted();
        invoicePaymentStarted.setCashFlow(getCashFlows());
        invoicePaymentStarted.setPayment(payment);

        InvoicePaymentChange invoicePaymentChange = new InvoicePaymentChange();
        invoicePaymentChange.setId(generateString());
        invoicePaymentChange.setPayload(InvoicePaymentChangePayload.invoice_payment_started(invoicePaymentStarted));

        List<InvoiceChange> invoiceChanges = new ArrayList<>();
        invoiceChanges.add(InvoiceChange.invoice_payment_change(invoicePaymentChange));

        Event event = new Event();
        event.setId(generateLong());
        event.setCreatedAt(generateDate());
        event.setSource(EventSource.invoice_id(generateString()));
        event.setPayload(EventPayload.invoice_changes(invoiceChanges));

        StockEvent stockEvent = new StockEvent();
        stockEvent.setSourceEvent(SourceEvent.processing_event(event));

        String paymentId = invoicePaymentChange.getId();
        String invoiceId = event.getSource().getInvoiceId();

        com.rbkmoney.reporter.domain.tables.pojos.Invoice invoice = random(com.rbkmoney.reporter.domain.tables.pojos.Invoice.class);
        invoice.setId(null);
        invoice.setCurrent(true);
        invoice.setInvoiceId(invoiceId);
        invoiceDao.save(invoice);

        clientHandler.handle(stockEvent, "");

        assertNotNull(paymentDao.get(invoiceId, paymentId));
    }

    @Test
    @Sql("classpath:data/sql/truncate.sql")
    public void payoutEventServiceTest() throws Exception {
        Payout payout = random(Payout.class, "status", "payout_flow", "type", "summary", "metadata");
        payout.setPartyId(UUID.randomUUID().toString());
        payout.setCreatedAt(generateDate());
        payout.setStatus(PayoutStatus.paid(new PayoutPaid()));
        payout.setPayoutFlow(getCashFlows());
        payout.setType(PayoutType.wallet(new Wallet()));

        PayoutCreated payoutCreated = new PayoutCreated();
        payoutCreated.setPayout(payout);

        List<PayoutChange> payoutChanges = new ArrayList<>();
        payoutChanges.add(PayoutChange.payout_created(payoutCreated));

        com.rbkmoney.damsel.payout_processing.Event event = new com.rbkmoney.damsel.payout_processing.Event();
        event.setId(generateLong());
        event.setCreatedAt(generateDate());
        event.setSource(com.rbkmoney.damsel.payout_processing.EventSource.payout_id(generateString()));
        event.setPayload(com.rbkmoney.damsel.payout_processing.EventPayload.payout_changes(payoutChanges));

        StockEvent stockEvent = new StockEvent();
        stockEvent.setSourceEvent(SourceEvent.payout_event(event));

        String payoutId = event.getSource().getPayoutId();

        clientHandler.handle(stockEvent, "");

        assertNotNull(payoutDao.get(payoutId));
    }

    @Test
    @Sql("classpath:data/sql/truncate.sql")
    public void refundEventServiceTest() throws Exception {
        InvoicePaymentRefund refund = random(InvoicePaymentRefund.class, "cash", "status");
        refund.setStatus(InvoicePaymentRefundStatus.pending(new InvoicePaymentRefundPending()));
        refund.setCreatedAt(generateDate());

        InvoicePaymentRefundCreated invoicePaymentRefundCreated = new InvoicePaymentRefundCreated();
        invoicePaymentRefundCreated.setRefund(refund);
        invoicePaymentRefundCreated.setCashFlow(getCashFlows());

        InvoicePaymentRefundChange invoicePaymentRefundChange = new InvoicePaymentRefundChange();
        invoicePaymentRefundChange.setId(generateString());
        invoicePaymentRefundChange.setPayload(InvoicePaymentRefundChangePayload.invoice_payment_refund_created(invoicePaymentRefundCreated));

        InvoicePaymentChange invoicePaymentChange = new InvoicePaymentChange();
        invoicePaymentChange.setId(generateString());
        invoicePaymentChange.setPayload(InvoicePaymentChangePayload.invoice_payment_refund_change(invoicePaymentRefundChange));

        List<InvoiceChange> invoiceChanges = new ArrayList<>();
        invoiceChanges.add(InvoiceChange.invoice_payment_change(invoicePaymentChange));

        Event event = new Event();
        event.setId(generateLong());
        event.setCreatedAt(generateDate());
        event.setSource(EventSource.invoice_id(generateString()));
        event.setPayload(EventPayload.invoice_changes(invoiceChanges));

        StockEvent stockEvent = new StockEvent();
        stockEvent.setSourceEvent(SourceEvent.processing_event(event));

        String refundId = invoicePaymentRefundChange.getId();
        String paymentId = invoicePaymentChange.getId();
        String invoiceId = event.getSource().getInvoiceId();

        Payment payment = random(Payment.class, "paymentCashFlow");
        payment.setInvoiceId(invoiceId);
        payment.setPaymentId(paymentId);
        payment.setId(null);
        payment.setCurrent(true);
        paymentDao.save(payment);

        clientHandler.handle(stockEvent, "");

        assertNotNull(refundDao.get(invoiceId, paymentId, refundId));
    }

    private List<FinalCashFlowPosting> getCashFlows() {
        return singletonList(
                new FinalCashFlowPosting(
                        new FinalCashFlowAccount(
                                CashFlowAccount.merchant(MerchantCashFlowAccount.payout),
                                generateLong()
                        ),
                        new FinalCashFlowAccount(
                                CashFlowAccount.provider(ProviderCashFlowAccount.settlement),
                                generateLong()
                        ),
                        random(Cash.class)
                )
        );
    }
}