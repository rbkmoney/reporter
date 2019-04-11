package com.rbkmoney.reporter.handle.stockevent.event.change.impl;

import com.rbkmoney.damsel.domain.PaymentRoute;
import com.rbkmoney.damsel.event_stock.StockEvent;
import com.rbkmoney.damsel.payment_processing.Event;
import com.rbkmoney.damsel.payment_processing.InvoiceChange;
import com.rbkmoney.damsel.payment_processing.InvoicePaymentChange;
import com.rbkmoney.damsel.payment_processing.InvoicePaymentRouteChanged;
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
public class PaymentRouteChangedChangeEventHandler implements InvoiceChangeEventsHandler {

    private final PaymentService paymentService;

    @Override
    public boolean accept(InvoiceChange change) {
        return change.isSetInvoicePaymentChange()
                && change.getInvoicePaymentChange().getPayload().isSetInvoicePaymentRouteChanged();
    }

    @Override
    public void handle(InvoiceChange change, StockEvent stockEvent) {
        Event event = stockEvent.getSourceEvent().getProcessingEvent();

        InvoicePaymentChange invoicePaymentChange = change.getInvoicePaymentChange();
        InvoicePaymentRouteChanged invoicePaymentRouteChanged = invoicePaymentChange.getPayload().getInvoicePaymentRouteChanged();
        PaymentRoute paymentRoute = invoicePaymentRouteChanged.getRoute();

        String paymentId = invoicePaymentChange.getId();
        String invoiceId = event.getSource().getInvoiceId();

        log.info("Start invoice payment route changed handling, paymentId={}, invoiceId={}", paymentId, invoiceId);

        Payment payment = paymentService.get(invoiceId, paymentId);

        payment.setId(null);
        payment.setWtime(null);
        payment.setEventId(event.getId());
        payment.setEventCreatedAt(TypeUtil.stringToLocalDateTime(event.getCreatedAt()));
        payment.setEventType(InvoiceEventType.INVOICE_PAYMENT_ROUTE_CHANGED);
        payment.setInvoiceId(event.getSource().getInvoiceId());
        payment.setSequenceId(event.getSequence());
        payment.setPaymentProviderId(paymentRoute.getProvider().getId());
        payment.setPaymentTerminalId(paymentRoute.getTerminal().getId());

        paymentService.updateNotCurrent(invoiceId, paymentId);
        paymentService.save(payment);
        log.info("Invoice payment route has been changed, paymentId={}, invoiceId={}", paymentId, invoiceId);
    }
}
