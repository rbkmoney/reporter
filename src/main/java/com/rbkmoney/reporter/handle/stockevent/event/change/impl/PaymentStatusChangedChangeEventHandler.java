package com.rbkmoney.reporter.handle.stockevent.event.change.impl;

import com.rbkmoney.damsel.domain.Cash;
import com.rbkmoney.damsel.domain.Failure;
import com.rbkmoney.damsel.domain.InvoicePaymentCaptured;
import com.rbkmoney.damsel.domain.OperationFailure;
import com.rbkmoney.damsel.event_stock.StockEvent;
import com.rbkmoney.damsel.payment_processing.Event;
import com.rbkmoney.damsel.payment_processing.InvoiceChange;
import com.rbkmoney.damsel.payment_processing.InvoicePaymentChange;
import com.rbkmoney.damsel.payment_processing.InvoicePaymentStatusChanged;
import com.rbkmoney.geck.common.util.TBaseUtil;
import com.rbkmoney.geck.common.util.TypeUtil;
import com.rbkmoney.geck.serializer.kit.tbase.TErrorUtil;
import com.rbkmoney.reporter.domain.enums.FailureClass;
import com.rbkmoney.reporter.domain.enums.InvoiceEventType;
import com.rbkmoney.reporter.domain.enums.InvoicePaymentStatus;
import com.rbkmoney.reporter.domain.tables.pojos.Payment;
import com.rbkmoney.reporter.handle.stockevent.event.change.InvoiceChangeEventsHandler;
import com.rbkmoney.reporter.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class PaymentStatusChangedChangeEventHandler implements InvoiceChangeEventsHandler {

    private final PaymentService paymentService;

    @Override
    public boolean accept(InvoiceChange change) {
        return change.isSetInvoicePaymentChange()
                && change.getInvoicePaymentChange().getPayload().isSetInvoicePaymentStatusChanged();
    }

    @Override
    public void handle(InvoiceChange change, StockEvent stockEvent) {
        Event event = stockEvent.getSourceEvent().getProcessingEvent();

        InvoicePaymentChange invoicePaymentChange = change.getInvoicePaymentChange();
        InvoicePaymentStatusChanged invoicePaymentStatusChanged = getInvoicePaymentStatusChanged(invoicePaymentChange);
        com.rbkmoney.damsel.domain.InvoicePaymentStatus invoicePaymentStatusChangedStatus = invoicePaymentStatusChanged.getStatus();

        String paymentId = invoicePaymentChange.getId();
        String invoiceId = event.getSource().getInvoiceId();

        log.info("Start invoice payment status changed handling, paymentId={}, invoiceId={}", paymentId, invoiceId);

        Payment payment = paymentService.get(invoiceId, paymentId);

        payment.setId(null);
        payment.setWtime(null);
        payment.setEventId(event.getId());
        payment.setEventCreatedAt(TypeUtil.stringToLocalDateTime(event.getCreatedAt()));
        payment.setEventType(InvoiceEventType.INVOICE_PAYMENT_STATUS_CHANGED);
        payment.setSequenceId(event.getSequence());
        fillPaymentStatus(invoicePaymentStatusChangedStatus, payment);

        paymentService.updateNotCurrent(invoiceId, paymentId);
        paymentService.save(payment);
        log.info("Invoice payment status has been changed, paymentId={}, invoiceId={}", paymentId, invoiceId);

    }

    private void fillPaymentStatus(com.rbkmoney.damsel.domain.InvoicePaymentStatus invoicePaymentStatusChangedStatus, Payment payment) {
        payment.setPaymentStatus(TBaseUtil.unionFieldToEnum(invoicePaymentStatusChangedStatus, InvoicePaymentStatus.class));
        if (invoicePaymentStatusChangedStatus.isSetCaptured()) {
            InvoicePaymentCaptured invoicePaymentCaptured = invoicePaymentStatusChangedStatus.getCaptured();
            if (invoicePaymentCaptured.isSetCost()) {
                Cash cost = invoicePaymentCaptured.getCost();

                payment.setPaymentAmount(cost.getAmount());
                payment.setPaymentCurrencyCode(cost.getCurrency().getSymbolicCode());
            }
        } else if (invoicePaymentStatusChangedStatus.isSetFailed()) {
            OperationFailure operationFailure = invoicePaymentStatusChangedStatus.getFailed().getFailure();

            payment.setPaymentOperationFailureClass(TBaseUtil.unionFieldToEnum(operationFailure, FailureClass.class));
            if (operationFailure.isSetFailure()) {
                Failure failure = operationFailure.getFailure();

                payment.setPaymentExternalFailure(TErrorUtil.toStringVal(failure));
                payment.setPaymentExternalFailureReason(failure.getReason());
            }
        }
    }

    private InvoicePaymentStatusChanged getInvoicePaymentStatusChanged(InvoicePaymentChange invoicePaymentChange) {
        return invoicePaymentChange.getPayload().getInvoicePaymentStatusChanged();
    }
}
