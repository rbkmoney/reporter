package com.rbkmoney.reporter.mapper.machineevent;

import com.rbkmoney.damsel.domain.PaymentRoute;
import com.rbkmoney.damsel.payment_processing.InvoiceChange;
import com.rbkmoney.damsel.payment_processing.InvoicePaymentChange;
import com.rbkmoney.damsel.payment_processing.InvoicePaymentRouteChanged;
import com.rbkmoney.geck.common.util.TypeUtil;
import com.rbkmoney.machinegun.eventsink.MachineEvent;
import com.rbkmoney.reporter.domain.enums.InvoiceEventType;
import com.rbkmoney.reporter.domain.tables.pojos.Payment;
import com.rbkmoney.reporter.mapper.InvoiceChangeMapper;
import com.rbkmoney.reporter.mapper.MapperResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class PaymentRouteChangedChangeMapperImpl implements InvoiceChangeMapper {

    @Override
    public String[] getIgnoreProperties() {
        return new String[]{"id", "wtime", "current", "eventCreatedAt", "eventType", "sequenceId", "changeId",
                "paymentProviderId", "paymentTerminalId"};
    }

    @Override
    public boolean canMap(InvoiceChange payload) {
        return payload.isSetInvoicePaymentChange()
                && payload.getInvoicePaymentChange().getPayload().isSetInvoicePaymentRouteChanged();
    }

    @Override
    public MapperResult map(InvoiceChange payload, MachineEvent baseEvent, Integer changeId) {
        InvoicePaymentChange invoicePaymentChange = payload.getInvoicePaymentChange();
        InvoicePaymentRouteChanged invoicePaymentRouteChanged = invoicePaymentChange.getPayload().getInvoicePaymentRouteChanged();
        PaymentRoute paymentRoute = invoicePaymentRouteChanged.getRoute();

        String paymentId = invoicePaymentChange.getId();
        String invoiceId = baseEvent.getSourceId();

        Payment payment = new Payment();

        payment.setId(null);
        payment.setWtime(null);
        payment.setEventCreatedAt(TypeUtil.stringToLocalDateTime(baseEvent.getCreatedAt()));
        payment.setEventType(InvoiceEventType.INVOICE_PAYMENT_ROUTE_CHANGED);
        payment.setSequenceId(baseEvent.getEventId());
        payment.setChangeId(changeId);
        payment.setPaymentProviderId(paymentRoute.getProvider().getId());
        payment.setPaymentTerminalId(paymentRoute.getTerminal().getId());

        log.info("Payment with eventType=routeChanged has been mapped, invoiceId={}, paymentId={}", invoiceId, paymentId);

        return new MapperResult(payment);
    }
}
