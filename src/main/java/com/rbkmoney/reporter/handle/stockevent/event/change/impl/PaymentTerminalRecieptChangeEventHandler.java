package com.rbkmoney.reporter.handle.stockevent.event.change.impl;

import com.rbkmoney.damsel.event_stock.StockEvent;
import com.rbkmoney.damsel.payment_processing.Event;
import com.rbkmoney.damsel.payment_processing.InvoiceChange;
import com.rbkmoney.damsel.payment_processing.InvoicePaymentChange;
import com.rbkmoney.damsel.user_interaction.PaymentTerminalReceipt;
import com.rbkmoney.geck.common.util.TypeUtil;
import com.rbkmoney.reporter.domain.enums.InvoiceEventType;
import com.rbkmoney.reporter.domain.tables.pojos.Payment;
import com.rbkmoney.reporter.handle.stockevent.event.change.InvoiceChangeEventsHandler;
import com.rbkmoney.reporter.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class PaymentTerminalRecieptChangeEventHandler implements InvoiceChangeEventsHandler {

    private final PaymentService paymentService;

    @Override
    public boolean accept(InvoiceChange change) {
        return change.isSetInvoicePaymentChange()
                && change.getInvoicePaymentChange().getPayload().isSetInvoicePaymentSessionChange()
                && change.getInvoicePaymentChange().getPayload().getInvoicePaymentSessionChange().getPayload().isSetSessionInteractionRequested()
                && change.getInvoicePaymentChange().getPayload().getInvoicePaymentSessionChange().getPayload().getSessionInteractionRequested().getInteraction().isSetPaymentTerminalReciept();
    }

    @Override
    public void handle(InvoiceChange change, StockEvent stockEvent) {
        Event event = stockEvent.getSourceEvent().getProcessingEvent();

        InvoicePaymentChange invoicePaymentChange = change.getInvoicePaymentChange();
        PaymentTerminalReceipt paymentTerminalReceipt = getPaymentTerminalReciept(invoicePaymentChange);

        String paymentId = invoicePaymentChange.getId();
        String invoiceId = event.getSource().getInvoiceId();

        log.info("Start invoice payment terminal reciept changed handling, paymentId={}, invoiceId={}", paymentId, invoiceId);

        Payment payment = paymentService.get(invoiceId, paymentId);

        payment.setId(null);
        payment.setWtime(null);
        payment.setEventId(event.getId());
        payment.setEventCreatedAt(TypeUtil.stringToLocalDateTime(event.getCreatedAt()));
        payment.setEventType(InvoiceEventType.PAYMENT_TERMINAL_RECIEPT);
        payment.setSequenceId(event.getSequence());
        payment.setPaymentShortId(paymentTerminalReceipt.getShortPaymentId());

        paymentService.updateNotCurrent(invoiceId, paymentId);
        paymentService.save(payment);
        log.info("Invoice payment terminal reciept has been changed, paymentId={}, invoiceId={}", paymentId, invoiceId);
    }

    private PaymentTerminalReceipt getPaymentTerminalReciept(InvoicePaymentChange invoicePaymentChange) {
        return invoicePaymentChange
                .getPayload().getInvoicePaymentSessionChange()
                .getPayload().getSessionInteractionRequested()
                .getInteraction()
                .getPaymentTerminalReciept();
    }
}
