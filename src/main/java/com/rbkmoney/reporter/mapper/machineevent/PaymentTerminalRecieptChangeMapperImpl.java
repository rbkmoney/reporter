package com.rbkmoney.reporter.mapper.machineevent;

import com.rbkmoney.damsel.payment_processing.InvoiceChange;
import com.rbkmoney.damsel.payment_processing.InvoicePaymentChange;
import com.rbkmoney.damsel.user_interaction.PaymentTerminalReceipt;
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
public class PaymentTerminalRecieptChangeMapperImpl implements InvoiceChangeMapper {

    @Override
    public String[] getIgnoreProperties() {
        return new String[]{"id", "wtime", "current", "eventCreatedAt", "eventType", "sequenceId", "changeId",
                "paymentShortId"};
    }

    @Override
    public boolean canMap(InvoiceChange payload) {
        return payload.isSetInvoicePaymentChange()
                && payload.getInvoicePaymentChange().getPayload().isSetInvoicePaymentSessionChange()
                && payload.getInvoicePaymentChange().getPayload().getInvoicePaymentSessionChange().getPayload().isSetSessionInteractionRequested()
                && payload.getInvoicePaymentChange().getPayload().getInvoicePaymentSessionChange().getPayload().getSessionInteractionRequested().getInteraction().isSetPaymentTerminalReciept();
    }

    @Override
    public MapperResult map(InvoiceChange payload, MachineEvent baseEvent, Integer changeId) {
        InvoicePaymentChange invoicePaymentChange = payload.getInvoicePaymentChange();
        PaymentTerminalReceipt paymentTerminalReceipt = getPaymentTerminalReciept(invoicePaymentChange);

        String paymentId = invoicePaymentChange.getId();
        String invoiceId = baseEvent.getSourceId();

        Payment payment = new Payment();

        payment.setId(null);
        payment.setWtime(null);
        payment.setEventCreatedAt(TypeUtil.stringToLocalDateTime(baseEvent.getCreatedAt()));
        payment.setEventType(InvoiceEventType.PAYMENT_TERMINAL_RECIEPT);
        payment.setSequenceId(baseEvent.getEventId());
        payment.setChangeId(changeId);
        payment.setPaymentShortId(paymentTerminalReceipt.getShortPaymentId());

        log.info("Payment with eventType=terminalReciept has been mapped, invoiceId={}, paymentId={}", invoiceId, paymentId);

        return new MapperResult(payment);
    }

    private PaymentTerminalReceipt getPaymentTerminalReciept(InvoicePaymentChange invoicePaymentChange) {
        return invoicePaymentChange
                .getPayload().getInvoicePaymentSessionChange()
                .getPayload().getSessionInteractionRequested()
                .getInteraction()
                .getPaymentTerminalReciept();
    }
}
