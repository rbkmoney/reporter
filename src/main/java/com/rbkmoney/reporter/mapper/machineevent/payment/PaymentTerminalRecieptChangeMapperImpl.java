package com.rbkmoney.reporter.mapper.machineevent.payment;

import com.rbkmoney.damsel.payment_processing.InvoiceChange;
import com.rbkmoney.damsel.payment_processing.InvoicePaymentChange;
import com.rbkmoney.geck.common.util.TypeUtil;
import com.rbkmoney.machinegun.eventsink.MachineEvent;
import com.rbkmoney.reporter.domain.tables.pojos.PaymentTerminalReceipt;
import com.rbkmoney.reporter.mapper.InvoiceChangeMapper;
import com.rbkmoney.reporter.mapper.MapperResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

import static com.rbkmoney.reporter.util.MapperUtil.getPaymentTerminalReceipt;

@Component
@Slf4j
public class PaymentTerminalRecieptChangeMapperImpl implements InvoiceChangeMapper {

    @Override
    public boolean canMap(InvoiceChange payload) {
        return payload.isSetInvoicePaymentChange()
                && payload.getInvoicePaymentChange().getPayload().isSetInvoicePaymentSessionChange()
                && payload.getInvoicePaymentChange().getPayload().getInvoicePaymentSessionChange().getPayload().isSetSessionInteractionRequested()
                && payload.getInvoicePaymentChange().getPayload().getInvoicePaymentSessionChange().getPayload().getSessionInteractionRequested().getInteraction().isSetPaymentTerminalReciept();
    }

    @Override
    public MapperResult map(InvoiceChange payload, MachineEvent baseEvent, Integer changeId) {
        InvoicePaymentChange damselPaymentChange = payload.getInvoicePaymentChange();
        var damselPaymentTerminalReceipt = getPaymentTerminalReciept(damselPaymentChange);

        String invoiceId = baseEvent.getSourceId();
        long sequenceId = baseEvent.getEventId();
        LocalDateTime eventCreatedAt = TypeUtil.stringToLocalDateTime(baseEvent.getCreatedAt());
        String paymentId = damselPaymentChange.getId();

        PaymentTerminalReceipt paymentTerminalReceipt = getPaymentTerminalReceipt(invoiceId, changeId, sequenceId, eventCreatedAt, paymentId, damselPaymentTerminalReceipt);

        log.info("Payment with eventType=[paymentTerminalReceipt] has been mapped, invoiceId={}, paymentId={}", invoiceId, paymentId);

        return new MapperResult(paymentTerminalReceipt);
    }

    private com.rbkmoney.damsel.user_interaction.PaymentTerminalReceipt getPaymentTerminalReciept(InvoicePaymentChange invoicePaymentChange) {
        return invoicePaymentChange
                .getPayload().getInvoicePaymentSessionChange()
                .getPayload().getSessionInteractionRequested()
                .getInteraction()
                .getPaymentTerminalReciept();
    }
}
