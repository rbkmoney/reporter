package com.rbkmoney.reporter.event;

import com.rbkmoney.damsel.domain.Invoice;
import com.rbkmoney.damsel.domain.InvoicePayment;
import com.rbkmoney.damsel.domain.InvoicePaymentAdjustmentPending;
import com.rbkmoney.damsel.domain.InvoicePaymentPending;
import com.rbkmoney.damsel.domain.InvoicePaymentRefundPending;
import com.rbkmoney.damsel.domain.*;
import com.rbkmoney.damsel.event_stock.SourceEvent;
import com.rbkmoney.damsel.event_stock.StockEvent;
import com.rbkmoney.damsel.payment_processing.EventPayload;
import com.rbkmoney.damsel.payment_processing.*;
import com.rbkmoney.damsel.payout_processing.Wallet;
import com.rbkmoney.damsel.payout_processing.*;
import com.rbkmoney.geck.common.util.TypeUtil;
import com.rbkmoney.machinegun.eventsink.MachineEvent;
import com.rbkmoney.machinegun.eventsink.SinkEvent;
import com.rbkmoney.machinegun.msgpack.Value;
import com.rbkmoney.reporter.batch.InvoiceBatchManager;
import com.rbkmoney.reporter.dao.*;
import com.rbkmoney.reporter.domain.tables.pojos.Adjustment;
import com.rbkmoney.reporter.domain.tables.pojos.Payment;
import com.rbkmoney.reporter.domain.tables.pojos.Refund;
import com.rbkmoney.reporter.handle.InvoiceBatchHandler;
import com.rbkmoney.reporter.listener.machineevent.PaymentEventsMessageListener;
import com.rbkmoney.reporter.service.BatchService;
import com.rbkmoney.sink.common.handle.stockevent.StockEventHandler;
import com.rbkmoney.sink.common.parser.Parser;
import com.rbkmoney.sink.common.serialization.impl.PaymentEventPayloadSerializer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.test.context.jdbc.Sql;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static io.github.benas.randombeans.api.EnhancedRandom.random;
import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class EventServiceTests extends AbstractAppEventServiceTests {

    private PaymentEventPayloadSerializer paymentEventPayloadSerializer = new PaymentEventPayloadSerializer();

    @Autowired
    private StockEventHandler<StockEvent> payoutEventStockEventHandler;

    @Autowired
    private Parser<MachineEvent, EventPayload> paymentEventPayloadMachineEventParser;

    @Autowired
    private InvoiceBatchManager invoiceBatchManager;

    @Autowired
    private BatchService batchService;

    @Autowired
    private InvoiceBatchHandler<com.rbkmoney.reporter.domain.tables.pojos.Invoice, Void> invoiceBatchHandler;

    @Autowired
    private InvoiceBatchHandler<Payment, com.rbkmoney.reporter.domain.tables.pojos.Invoice> paymentInvoiceBatchHandler;

    @Autowired
    private InvoiceBatchHandler<Adjustment, com.rbkmoney.reporter.domain.tables.pojos.Invoice> adjustmentInvoiceBatchHandler;

    @Autowired
    private InvoiceBatchHandler<Refund, Payment> refundInvoiceBatchHandler;

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
    public void batchHandlingTest() {
        PaymentEventsMessageListener paymentEventsMessageListener = new PaymentEventsMessageListener(paymentEventPayloadMachineEventParser, invoiceBatchManager, batchService, invoiceBatchHandler, paymentInvoiceBatchHandler, adjustmentInvoiceBatchHandler, refundInvoiceBatchHandler);

        String invoiceId = generateString();
        String paymentId = generateString();
        String adjustmentId = generateString();
        String refundId = generateString();

        List<ConsumerRecord<String, SinkEvent>> messages = new ArrayList<>();

        messages.add(getConsumerRecord(getSinkEvent(1L, invoiceId, getInvoiceCreated(InvoiceStatus.unpaid(new InvoiceUnpaid())))));
        messages.add(getConsumerRecord(getPaymentMachineEvent(2L, invoiceId, paymentId, getInvoicePaymentStarted(InvoicePaymentStatus.pending(new InvoicePaymentPending())))));
        messages.add(getConsumerRecord(getPaymentMachineEvent(3L, invoiceId, paymentId, getInvoicePaymentStatusChanged(getCaptured()))));
        messages.add(getConsumerRecord(getPaymentMachineEvent(32L, invoiceId, paymentId, getInvoicePaymentOther())));
        messages.add(getConsumerRecord(getPaymentMachineEvent(33L, invoiceId, paymentId, getInvoicePaymentSessionStarted())));
        messages.add(getConsumerRecord(getRefundMachineEvent(4L, invoiceId, paymentId, refundId, getInvoicePaymentRefundCreated(InvoicePaymentRefundStatus.pending(new InvoicePaymentRefundPending())))));
        messages.add(getConsumerRecord(getRefundMachineEvent(5L, invoiceId, paymentId, refundId, getInvoicePaymentRefundStatusChanged(InvoicePaymentRefundStatus.succeeded(new InvoicePaymentRefundSucceeded())))));
        messages.add(getConsumerRecord(getAdjustmentMachineEvent(6L, invoiceId, paymentId, adjustmentId, getAdjustmentCreated(InvoicePaymentAdjustmentStatus.pending(new InvoicePaymentAdjustmentPending())))));
        messages.add(getConsumerRecord(getAdjustmentMachineEvent(7L, invoiceId, paymentId, adjustmentId, getAdjustmentStatusChanged(InvoicePaymentAdjustmentStatus.captured(new InvoicePaymentAdjustmentCaptured(TypeUtil.temporalToString(LocalDateTime.now().plusDays(1))))))));
        messages.add(getConsumerRecord(getSinkEvent(8L, invoiceId, getInvoiceStatusChanged(InvoiceStatus.paid(new InvoicePaid())))));
        paymentEventsMessageListener.listen(messages, getAcknowledgment());

        Adjustment adjustment = adjustmentDao.get(invoiceId, paymentId, adjustmentId);
        Refund refund = refundDao.get(invoiceId, paymentId, refundId);
        Payment payment = paymentDao.get(invoiceId, paymentId);
        com.rbkmoney.reporter.domain.tables.pojos.Invoice invoice = invoiceDao.get(invoiceId);

        assertEquals((long) 7, (long) adjustment.getSequenceId());
        assertEquals((long) 5, (long) refund.getSequenceId());
        assertEquals((long) 3, (long) payment.getSequenceId());
        assertEquals((long) 8, (long) invoice.getSequenceId());

        messages.clear();
        messages.add(getConsumerRecord(getPaymentMachineEvent(11L, invoiceId, paymentId, getInvoicePaymentStatusChanged(getCaptured()))));
        messages.add(getConsumerRecord(getRefundMachineEvent(13L, invoiceId, paymentId, refundId, getInvoicePaymentRefundStatusChanged(InvoicePaymentRefundStatus.succeeded(new InvoicePaymentRefundSucceeded())))));
        messages.add(getConsumerRecord(getAdjustmentMachineEvent(15L, invoiceId, paymentId, adjustmentId, getAdjustmentStatusChanged(InvoicePaymentAdjustmentStatus.captured(new InvoicePaymentAdjustmentCaptured(TypeUtil.temporalToString(LocalDateTime.now().plusDays(1))))))));
        messages.add(getConsumerRecord(getSinkEvent(16L, invoiceId, getInvoiceStatusChanged(InvoiceStatus.paid(new InvoicePaid())))));
        paymentEventsMessageListener.listen(messages, getAcknowledgment());

        adjustment = adjustmentDao.get(invoiceId, paymentId, adjustmentId);
        refund = refundDao.get(invoiceId, paymentId, refundId);
        payment = paymentDao.get(invoiceId, paymentId);
        invoice = invoiceDao.get(invoiceId);

        assertEquals((long) 15, (long) adjustment.getSequenceId());
        assertEquals((long) 13, (long) refund.getSequenceId());
        assertEquals((long) 11, (long) payment.getSequenceId());
        assertEquals((long) 16, (long) invoice.getSequenceId());

        adjustmentId = generateString();
        refundId = generateString();

        messages.clear();
        messages.add(getConsumerRecord(getRefundMachineEvent(20L, invoiceId, paymentId, refundId, getInvoicePaymentRefundCreated(InvoicePaymentRefundStatus.pending(new InvoicePaymentRefundPending())))));
        messages.add(getConsumerRecord(getAdjustmentMachineEvent(21L, invoiceId, paymentId, adjustmentId, getAdjustmentCreated(InvoicePaymentAdjustmentStatus.pending(new InvoicePaymentAdjustmentPending())))));
        paymentEventsMessageListener.listen(messages, getAcknowledgment());

        adjustment = adjustmentDao.get(invoiceId, paymentId, adjustmentId);
        refund = refundDao.get(invoiceId, paymentId, refundId);

        assertEquals((long) 21, (long) adjustment.getSequenceId());
        assertEquals((long) 20, (long) refund.getSequenceId());
    }

    private Acknowledgment getAcknowledgment() {
        return () -> {
        };
    }

    private InvoicePaymentStatus getCaptured() {
        Cash cash = new Cash();
        cash.setAmount(1L);
        cash.setCurrency(new CurrencyRef("USD"));
        InvoicePaymentCaptured captured = new InvoicePaymentCaptured();
        captured.setCost(cash);
        return InvoicePaymentStatus.captured(captured);
    }

    private InvoicePaymentRefundChangePayload getInvoicePaymentRefundStatusChanged(InvoicePaymentRefundStatus status) {
        InvoicePaymentRefundStatusChanged invoicePaymentRefundStatusChanged = new InvoicePaymentRefundStatusChanged();
        invoicePaymentRefundStatusChanged.setStatus(status);
        return InvoicePaymentRefundChangePayload.invoice_payment_refund_status_changed(invoicePaymentRefundStatusChanged);
    }

    private InvoicePaymentRefundChangePayload getInvoicePaymentRefundCreated(InvoicePaymentRefundStatus status) {
        InvoicePaymentRefund refund = random(InvoicePaymentRefund.class, "cart", "status");
        refund.setStatus(status);
        refund.setCreatedAt(generateDate());

        InvoicePaymentRefundCreated invoicePaymentRefundCreated = new InvoicePaymentRefundCreated();
        invoicePaymentRefundCreated.setRefund(refund);
        invoicePaymentRefundCreated.setCashFlow(getCashFlows());

        return InvoicePaymentRefundChangePayload.invoice_payment_refund_created(invoicePaymentRefundCreated);
    }

    private SinkEvent getRefundMachineEvent(Long eventId, String invoiceId, String paymentId, String refundId, InvoicePaymentRefundChangePayload payload) {
        InvoicePaymentRefundChange invoicePaymentRefundChange = new InvoicePaymentRefundChange();
        invoicePaymentRefundChange.setId(refundId);
        invoicePaymentRefundChange.setPayload(payload);

        InvoicePaymentChange invoicePaymentChange = new InvoicePaymentChange();
        invoicePaymentChange.setId(paymentId);
        invoicePaymentChange.setPayload(InvoicePaymentChangePayload.invoice_payment_refund_change(invoicePaymentRefundChange));

        InvoiceChange invoiceChange = InvoiceChange.invoice_payment_change(invoicePaymentChange);

        return getSinkEvent(eventId, invoiceId, invoiceChange);
    }

    private InvoicePaymentChangePayload getInvoicePaymentStatusChanged(InvoicePaymentStatus status) {
        InvoicePaymentStatusChanged statusChanged = new InvoicePaymentStatusChanged();
        statusChanged.setStatus(status);
        return InvoicePaymentChangePayload.invoice_payment_status_changed(statusChanged);
    }

    private SinkEvent getPaymentMachineEvent(Long eventId, String invoiceId, String paymentId, InvoicePaymentChangePayload payload) {
        InvoicePaymentChange invoicePaymentChange = new InvoicePaymentChange();
        invoicePaymentChange.setId(paymentId);
        invoicePaymentChange.setPayload(payload);

        InvoiceChange invoiceChange = InvoiceChange.invoice_payment_change(invoicePaymentChange);

        return getSinkEvent(eventId, invoiceId, invoiceChange);
    }

    private InvoicePaymentChangePayload getInvoicePaymentStarted(InvoicePaymentStatus status) {
        PaymentTerminal paymentTerminal = new PaymentTerminal();
        paymentTerminal.setTerminalType(TerminalPaymentProvider.euroset);

        CustomerPayer customerPayer = random(CustomerPayer.class, "payment_tool");
        customerPayer.setPaymentTool(PaymentTool.payment_terminal(paymentTerminal));

        InvoicePayment payment = random(InvoicePayment.class, "status", "payer", "flow", "context");
        payment.setStatus(status);
        payment.setCreatedAt(generateDate());
        payment.setPayer(Payer.customer(customerPayer));
        payment.setFlow(InvoicePaymentFlow.instant(new InvoicePaymentFlowInstant()));

        InvoicePaymentStarted invoicePaymentStarted = new InvoicePaymentStarted();
        invoicePaymentStarted.setCashFlow(getCashFlows());
        invoicePaymentStarted.setPayment(payment);
        return InvoicePaymentChangePayload.invoice_payment_started(invoicePaymentStarted);
    }

    private InvoicePaymentChangePayload getInvoicePaymentOther() {
        InvoicePaymentRiskScoreChanged riskScoreChanged = new InvoicePaymentRiskScoreChanged();
        riskScoreChanged.setRiskScore(RiskScore.high);
        return InvoicePaymentChangePayload.invoice_payment_risk_score_changed(riskScoreChanged);
    }

    private InvoicePaymentChangePayload getInvoicePaymentSessionStarted() {
        SessionStarted sessionStarted = new SessionStarted();

        SessionChangePayload sessionChangePayload = new SessionChangePayload();
        sessionChangePayload.setSessionStarted(sessionStarted);

        InvoicePaymentSessionChange invoicePaymentSessionChange = new InvoicePaymentSessionChange();
        invoicePaymentSessionChange.setPayload(sessionChangePayload);
        invoicePaymentSessionChange.setTarget(TargetInvoicePaymentStatus.processed(new InvoicePaymentProcessed()));
        return InvoicePaymentChangePayload.invoice_payment_session_change(invoicePaymentSessionChange);
    }

    private InvoiceChange getInvoiceCreated(InvoiceStatus status) {
        Invoice invoice = random(Invoice.class, "status", "details", "context");
        invoice.setStatus(status);
        invoice.setDetails(random(InvoiceDetails.class, "cart"));
        invoice.setOwnerId(UUID.randomUUID().toString());
        invoice.setCreatedAt(generateDate());
        invoice.setDue(generateDate());

        InvoiceCreated invoiceCreated = new InvoiceCreated();
        invoiceCreated.setInvoice(invoice);

        return InvoiceChange.invoice_created(invoiceCreated);
    }

    private InvoiceChange getInvoiceStatusChanged(InvoiceStatus status) {
        InvoiceStatusChanged invoiceStatusChanged = new InvoiceStatusChanged();
        invoiceStatusChanged.setStatus(status);
        return InvoiceChange.invoice_status_changed(invoiceStatusChanged);
    }

    private ConsumerRecord<String, SinkEvent> getConsumerRecord(SinkEvent sinkEvent) {
        return new ConsumerRecord<>("asad", 1, 1, "asd", sinkEvent);
    }

    private InvoicePaymentAdjustmentChangePayload getAdjustmentStatusChanged(InvoicePaymentAdjustmentStatus status) {
        InvoicePaymentAdjustmentStatusChanged invoicePaymentAdjustmentStatusChanged = new InvoicePaymentAdjustmentStatusChanged();
        invoicePaymentAdjustmentStatusChanged.setStatus(status);
        return InvoicePaymentAdjustmentChangePayload.invoice_payment_adjustment_status_changed(invoicePaymentAdjustmentStatusChanged);
    }

    private InvoicePaymentAdjustmentChangePayload getAdjustmentCreated(InvoicePaymentAdjustmentStatus status) {
        InvoicePaymentAdjustment adjustment = new InvoicePaymentAdjustment();
        adjustment.setId(generateString());
        adjustment.setStatus(status);
        adjustment.setCreatedAt(generateDate());
        adjustment.setDomainRevision(generateLong());
        adjustment.setReason(generateString());
        adjustment.setNewCashFlow(getCashFlows());
        adjustment.setOldCashFlowInverse(getCashFlows());

        InvoicePaymentAdjustmentCreated invoicePaymentAdjustmentCreated = new InvoicePaymentAdjustmentCreated();
        invoicePaymentAdjustmentCreated.setAdjustment(adjustment);
        return InvoicePaymentAdjustmentChangePayload.invoice_payment_adjustment_created(invoicePaymentAdjustmentCreated);
    }

    private SinkEvent getAdjustmentMachineEvent(Long eventId, String invoiceId, String paymentId, String adjustmentId, InvoicePaymentAdjustmentChangePayload payload) {
        InvoicePaymentAdjustmentChange invoicePaymentAdjustmentChange = new InvoicePaymentAdjustmentChange();
        invoicePaymentAdjustmentChange.setId(adjustmentId);
        invoicePaymentAdjustmentChange.setPayload(payload);

        InvoicePaymentChange invoicePaymentChange = new InvoicePaymentChange();
        invoicePaymentChange.setId(paymentId);
        invoicePaymentChange.setPayload(InvoicePaymentChangePayload.invoice_payment_adjustment_change(invoicePaymentAdjustmentChange));

        InvoiceChange invoiceChange = InvoiceChange.invoice_payment_change(invoicePaymentChange);

        return getSinkEvent(eventId, invoiceId, invoiceChange);
    }

    private SinkEvent getSinkEvent(Long eventId, String invoiceId, InvoiceChange invoiceChange) {
        List<InvoiceChange> invoiceChanges = new ArrayList<>();
        invoiceChanges.add(invoiceChange);

        EventPayload eventPayload = EventPayload.invoice_changes(invoiceChanges);

        MachineEvent machineEvent = new MachineEvent();
        machineEvent.setSourceNs(generateString());
        machineEvent.setSourceId(invoiceId);
        machineEvent.setEventId(eventId);
        machineEvent.setCreatedAt(generateDate());
        machineEvent.setData(Value.bin(paymentEventPayloadSerializer.serialize(eventPayload)));

        SinkEvent sinkEvent = new SinkEvent();
        sinkEvent.setEvent(machineEvent);
        return sinkEvent;
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

        payoutEventStockEventHandler.handle(stockEvent, stockEvent);

        assertNotNull(payoutDao.get(payoutId));
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
