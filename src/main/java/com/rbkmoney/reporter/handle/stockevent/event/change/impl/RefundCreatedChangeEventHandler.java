package com.rbkmoney.reporter.handle.stockevent.event.change.impl;

import com.rbkmoney.damsel.domain.Cash;
import com.rbkmoney.damsel.domain.InvoicePaymentRefund;
import com.rbkmoney.damsel.event_stock.StockEvent;
import com.rbkmoney.damsel.payment_processing.*;
import com.rbkmoney.geck.common.util.TBaseUtil;
import com.rbkmoney.geck.common.util.TypeUtil;
import com.rbkmoney.reporter.domain.enums.InvoiceEventType;
import com.rbkmoney.reporter.domain.enums.RefundStatus;
import com.rbkmoney.reporter.domain.tables.pojos.Payment;
import com.rbkmoney.reporter.domain.tables.pojos.Refund;
import com.rbkmoney.reporter.handle.stockevent.event.change.InvoiceChangeEventsHandler;
import com.rbkmoney.reporter.service.PaymentService;
import com.rbkmoney.reporter.service.RefundService;
import com.rbkmoney.reporter.util.CashFlowUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@Slf4j
@RequiredArgsConstructor
public class RefundCreatedChangeEventHandler implements InvoiceChangeEventsHandler {

    private final RefundService refundService;
    private final PaymentService paymentService;

    @Override
    public boolean accept(InvoiceChange change) {
        return change.isSetInvoicePaymentChange()
                && change.getInvoicePaymentChange().getPayload().isSetInvoicePaymentRefundChange()
                && change.getInvoicePaymentChange().getPayload().getInvoicePaymentRefundChange().getPayload().isSetInvoicePaymentRefundCreated();
    }

    @Override
    public void handle(InvoiceChange change, StockEvent stockEvent) {
        Event event = stockEvent.getSourceEvent().getProcessingEvent();

        InvoicePaymentChange invoicePaymentChange = change.getInvoicePaymentChange();
        InvoicePaymentRefundChange invoicePaymentRefundChange = getInvoicePaymentRefundChange(invoicePaymentChange);
        InvoicePaymentRefundCreated invoicePaymentRefundCreated = getInvoicePaymentRefundCreated(invoicePaymentRefundChange);
        InvoicePaymentRefund invoicePaymentRefund = invoicePaymentRefundCreated.getRefund();

        String refundId = invoicePaymentRefundChange.getId();
        String paymentId = invoicePaymentChange.getId();
        String invoiceId = event.getSource().getInvoiceId();

        Payment payment = paymentService.get(invoiceId, paymentId);

        log.info("Start invoice payment refund created handling, refundId={}, invoiceId={}, paymentId={}", refundId, invoiceId, paymentId);

        Refund refund = new Refund();
        refund.setEventId(event.getId());
        refund.setEventCreatedAt(TypeUtil.stringToLocalDateTime(event.getCreatedAt()));
        refund.setEventType(InvoiceEventType.INVOICE_PAYMENT_REFUND_CREATED);
        refund.setSequenceId(event.getSequence());
        refund.setInvoiceId(invoiceId);
        refund.setPaymentId(paymentId);
        refund.setPartyId(payment.getPaymentId());
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

            refund.setRefundCurrencyCode(cash.getCurrency().getSymbolicCode());
            refund.setRefundAmount(cash.getAmount());
        } else {
            refund.setRefundAmount(payment.getPaymentAmount());
            refund.setRefundCurrencyCode(payment.getPaymentCurrencyCode());
        }
        refund.setRefundReason(invoicePaymentRefund.getReason());
        refund.setRefundCashFlow(CashFlowUtil.toDtoFinalCashFlow(invoicePaymentRefundCreated.getCashFlow()));

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
