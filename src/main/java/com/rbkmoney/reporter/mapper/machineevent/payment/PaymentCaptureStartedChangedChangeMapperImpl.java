package com.rbkmoney.reporter.mapper.machineevent.payment;

import com.rbkmoney.damsel.domain.Cash;
import com.rbkmoney.damsel.payment_processing.InvoiceChange;
import com.rbkmoney.damsel.payment_processing.InvoicePaymentCaptureStarted;
import com.rbkmoney.damsel.payment_processing.InvoicePaymentChange;
import com.rbkmoney.geck.common.util.TypeUtil;
import com.rbkmoney.machinegun.eventsink.MachineEvent;
import com.rbkmoney.reporter.domain.tables.pojos.PaymentCost;
import com.rbkmoney.reporter.mapper.InvoiceChangeMapper;
import com.rbkmoney.reporter.mapper.MapperResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

import static com.rbkmoney.reporter.util.MapperUtil.getPaymentCost;

@Component
@Slf4j
public class PaymentCaptureStartedChangedChangeMapperImpl implements InvoiceChangeMapper {

    @Override
    public boolean canMap(InvoiceChange payload) {
        return payload.isSetInvoicePaymentChange()
                && payload.getInvoicePaymentChange().getPayload().isSetInvoicePaymentCaptureStarted()
                && payload.getInvoicePaymentChange().getPayload().getInvoicePaymentCaptureStarted().getParams().isSetCash();
    }

    @Override
    public MapperResult map(InvoiceChange payload, MachineEvent baseEvent, Integer changeId) {
        InvoicePaymentChange damselPaymentChange = payload.getInvoicePaymentChange();
        InvoicePaymentCaptureStarted damselPaymentCaptureStarted = getInvoicePaymentCaptureStarted(damselPaymentChange);

        Cash cost = damselPaymentCaptureStarted.getParams().getCash();
        String invoiceId = baseEvent.getSourceId();
        long sequenceId = baseEvent.getEventId();
        LocalDateTime eventCreatedAt = TypeUtil.stringToLocalDateTime(baseEvent.getCreatedAt());
        String paymentId = damselPaymentChange.getId();

        PaymentCost paymentCost = getPaymentCost(invoiceId, sequenceId, changeId, eventCreatedAt, paymentId, cost.getAmount(), cost.getCurrency().getSymbolicCode());

        log.info("Payment with eventType=[captureStarted]  has been mapped, invoiceId={}, paymentId={}", invoiceId, paymentId);

        return new MapperResult(paymentCost);
    }

    private InvoicePaymentCaptureStarted getInvoicePaymentCaptureStarted(InvoicePaymentChange invoicePaymentChange) {
        return invoicePaymentChange.getPayload().getInvoicePaymentCaptureStarted();
    }
}
