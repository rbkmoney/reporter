package com.rbkmoney.reporter.handle.machineevent;

import com.rbkmoney.damsel.domain.Cash;
import com.rbkmoney.damsel.domain.InvoicePaymentRefund;
import com.rbkmoney.damsel.payment_processing.InvoiceChange;
import com.rbkmoney.damsel.payment_processing.InvoicePaymentChange;
import com.rbkmoney.damsel.payment_processing.InvoicePaymentRefundChange;
import com.rbkmoney.damsel.payment_processing.InvoicePaymentRefundCreated;
import com.rbkmoney.geck.common.util.TBaseUtil;
import com.rbkmoney.geck.common.util.TypeUtil;
import com.rbkmoney.machinegun.eventsink.MachineEvent;
import com.rbkmoney.reporter.domain.enums.InvoiceEventType;
import com.rbkmoney.reporter.domain.enums.RefundStatus;
import com.rbkmoney.reporter.domain.tables.pojos.Payment;
import com.rbkmoney.reporter.domain.tables.pojos.Refund;
import com.rbkmoney.reporter.service.PaymentService;
import com.rbkmoney.reporter.service.RefundService;
import com.rbkmoney.reporter.util.json.FinalCashFlowUtil;
import com.rbkmoney.sink.common.handle.machineevent.eventpayload.change.InvoiceChangeEventHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@Slf4j
@RequiredArgsConstructor
public class RefundCreatedChangeMachineEventHandler implements InvoiceChangeEventHandler {

    private final RefundService refundService;
    private final PaymentService paymentService;

    @Override
    public boolean accept(InvoiceChange payload) {
        return payload.isSetInvoicePaymentChange()
                && payload.getInvoicePaymentChange().getPayload().isSetInvoicePaymentRefundChange()
                && payload.getInvoicePaymentChange().getPayload().getInvoicePaymentRefundChange().getPayload().isSetInvoicePaymentRefundCreated();
    }

    @Override
    public void handle(InvoiceChange payload, MachineEvent baseEvent, Integer changeId) {
        InvoicePaymentChange invoicePaymentChange = payload.getInvoicePaymentChange();
        InvoicePaymentRefundChange invoicePaymentRefundChange = getInvoicePaymentRefundChange(invoicePaymentChange);
        InvoicePaymentRefundCreated invoicePaymentRefundCreated = getInvoicePaymentRefundCreated(invoicePaymentRefundChange);
        InvoicePaymentRefund invoicePaymentRefund = invoicePaymentRefundCreated.getRefund();

        String refundId = invoicePaymentRefundChange.getId();
        String paymentId = invoicePaymentChange.getId();
        String invoiceId = baseEvent.getSourceId();

        Payment payment = paymentService.get(invoiceId, paymentId);

        log.info("Start invoice payment refund created handling, refundId={}, invoiceId={}, paymentId={}", refundId, invoiceId, paymentId);

        Refund refund = new Refund();
        refund.setEventCreatedAt(TypeUtil.stringToLocalDateTime(baseEvent.getCreatedAt()));
        refund.setEventType(InvoiceEventType.INVOICE_PAYMENT_REFUND_CREATED);
        refund.setInvoiceId(invoiceId);
        refund.setSequenceId(baseEvent.getEventId());
        refund.setChangeId(changeId);
        refund.setPaymentId(paymentId);
        refund.setPartyId(payment.getPartyId());
        refund.setPartyShopId(payment.getPartyShopId());
        refund.setRefundId(refundId);
        refund.setRefundStatus(getRefundStatus(invoicePaymentRefund));
        refund.setRefundCreatedAt(getRefundCreatedAt(invoicePaymentRefund));
        refund.setRefundDomainRevision(invoicePaymentRefund.getDomainRevision());
        if (invoicePaymentRefund.isSetPartyRevision()) {
            refund.setRefundPartyRevision(invoicePaymentRefund.getPartyRevision());
        }
        if (invoicePaymentRefund.isSetCash()) {
            Cash cash = invoicePaymentRefund.getCash();

            refund.setRefundAmount(cash.getAmount());
            refund.setRefundCurrencyCode(cash.getCurrency().getSymbolicCode());
        } else {
            refund.setRefundAmount(payment.getPaymentAmount());
            refund.setRefundCurrencyCode(payment.getPaymentCurrencyCode());
        }
        refund.setRefundReason(invoicePaymentRefund.getReason());
        refund.setRefundCashFlow(FinalCashFlowUtil.toDtoFinalCashFlow(invoicePaymentRefundCreated.getCashFlow()));

        refundService.save(refund);
        log.info("Invoice payment refund has been created, refundId={}, invoiceId={}, paymentId={}", refundId, invoiceId, paymentId);
    }

    private LocalDateTime getRefundCreatedAt(InvoicePaymentRefund invoicePaymentRefund) {
        return TypeUtil.stringToLocalDateTime(invoicePaymentRefund.getCreatedAt());
    }

    private RefundStatus getRefundStatus(InvoicePaymentRefund invoicePaymentRefund) {
        return TBaseUtil.unionFieldToEnum(invoicePaymentRefund.getStatus(), RefundStatus.class);
    }

    private InvoicePaymentRefundCreated getInvoicePaymentRefundCreated(InvoicePaymentRefundChange invoicePaymentRefundChange) {
        return invoicePaymentRefundChange
                .getPayload().getInvoicePaymentRefundCreated();
    }

    private InvoicePaymentRefundChange getInvoicePaymentRefundChange(InvoicePaymentChange invoicePaymentChange) {
        return invoicePaymentChange
                .getPayload().getInvoicePaymentRefundChange();
    }
}
