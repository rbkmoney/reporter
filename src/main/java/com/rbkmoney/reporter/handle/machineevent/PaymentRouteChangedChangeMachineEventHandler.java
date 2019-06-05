package com.rbkmoney.reporter.handle.machineevent;

import com.rbkmoney.damsel.domain.PaymentRoute;
import com.rbkmoney.damsel.payment_processing.InvoiceChange;
import com.rbkmoney.damsel.payment_processing.InvoicePaymentChange;
import com.rbkmoney.damsel.payment_processing.InvoicePaymentRouteChanged;
import com.rbkmoney.geck.common.util.TypeUtil;
import com.rbkmoney.machinegun.eventsink.MachineEvent;
import com.rbkmoney.reporter.domain.enums.InvoiceEventType;
import com.rbkmoney.reporter.domain.tables.pojos.Payment;
import com.rbkmoney.reporter.service.PaymentService;
import com.rbkmoney.sink.common.handle.machineevent.eventpayload.change.InvoiceChangeEventHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class PaymentRouteChangedChangeMachineEventHandler implements InvoiceChangeEventHandler {

    private final PaymentService paymentService;

    @Override
    public boolean accept(InvoiceChange payload) {
        return payload.isSetInvoicePaymentChange()
                && payload.getInvoicePaymentChange().getPayload().isSetInvoicePaymentRouteChanged();
    }

    @Override
    public void handle(InvoiceChange payload, MachineEvent baseEvent, Integer changeId) {
        InvoicePaymentChange invoicePaymentChange = payload.getInvoicePaymentChange();
        InvoicePaymentRouteChanged invoicePaymentRouteChanged = invoicePaymentChange.getPayload().getInvoicePaymentRouteChanged();
        PaymentRoute paymentRoute = invoicePaymentRouteChanged.getRoute();

        String paymentId = invoicePaymentChange.getId();
        String invoiceId = baseEvent.getSourceId();

        log.info("Start invoice payment route changed handling, paymentId={}, invoiceId={}", paymentId, invoiceId);

        Payment payment = paymentService.get(invoiceId, paymentId);

        payment.setId(null);
        payment.setWtime(null);
        payment.setEventCreatedAt(TypeUtil.stringToLocalDateTime(baseEvent.getCreatedAt()));
        payment.setEventType(InvoiceEventType.INVOICE_PAYMENT_ROUTE_CHANGED);
        payment.setInvoiceId(invoiceId);
        payment.setSequenceId(baseEvent.getEventId());
        payment.setChangeId(changeId);
        payment.setPaymentProviderId(paymentRoute.getProvider().getId());
        payment.setPaymentTerminalId(paymentRoute.getTerminal().getId());

        paymentService.updateNotCurrent(invoiceId, paymentId);
        paymentService.save(payment);
        log.info("Invoice payment route has been changed, paymentId={}, invoiceId={}", paymentId, invoiceId);
    }
}
