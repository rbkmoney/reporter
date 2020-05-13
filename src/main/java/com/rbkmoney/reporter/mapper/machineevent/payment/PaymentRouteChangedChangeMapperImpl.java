package com.rbkmoney.reporter.mapper.machineevent.payment;

import com.rbkmoney.damsel.domain.PaymentRoute;
import com.rbkmoney.damsel.payment_processing.InvoiceChange;
import com.rbkmoney.damsel.payment_processing.InvoicePaymentChange;
import com.rbkmoney.damsel.payment_processing.InvoicePaymentRouteChanged;
import com.rbkmoney.geck.common.util.TypeUtil;
import com.rbkmoney.machinegun.eventsink.MachineEvent;
import com.rbkmoney.reporter.domain.tables.pojos.PaymentRouting;
import com.rbkmoney.reporter.mapper.InvoiceChangeMapper;
import com.rbkmoney.reporter.mapper.MapperResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

import static com.rbkmoney.reporter.util.MapperUtil.getPaymentRouting;

@Component
@Slf4j
public class PaymentRouteChangedChangeMapperImpl implements InvoiceChangeMapper {

    @Override
    public boolean canMap(InvoiceChange payload) {
        return payload.isSetInvoicePaymentChange()
                && payload.getInvoicePaymentChange().getPayload().isSetInvoicePaymentRouteChanged();
    }

    @Override
    public MapperResult map(InvoiceChange payload, MachineEvent baseEvent, Integer changeId) {
        InvoicePaymentChange damselPaymentChange = payload.getInvoicePaymentChange();
        InvoicePaymentRouteChanged damselPaymentRouteChanged = damselPaymentChange.getPayload().getInvoicePaymentRouteChanged();
        PaymentRoute paymentRoute = damselPaymentRouteChanged.getRoute();

        String invoiceId = baseEvent.getSourceId();
        long sequenceId = baseEvent.getEventId();
        LocalDateTime eventCreatedAt = TypeUtil.stringToLocalDateTime(baseEvent.getCreatedAt());
        String paymentId = damselPaymentChange.getId();

        PaymentRouting paymentRouting = getPaymentRouting(invoiceId, changeId, sequenceId, eventCreatedAt, paymentId, paymentRoute);

        log.info("Payment with eventType=[routeChanged] has been mapped, invoiceId={}, paymentId={}", invoiceId, paymentId);

        return new MapperResult(paymentRouting);
    }
}
