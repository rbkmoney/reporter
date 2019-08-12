package com.rbkmoney.reporter.mapper.machineevent;

import com.rbkmoney.damsel.domain.FinalCashFlowPosting;
import com.rbkmoney.damsel.payment_processing.InvoiceChange;
import com.rbkmoney.damsel.payment_processing.InvoicePaymentCashFlowChanged;
import com.rbkmoney.damsel.payment_processing.InvoicePaymentChange;
import com.rbkmoney.geck.common.util.TypeUtil;
import com.rbkmoney.machinegun.eventsink.MachineEvent;
import com.rbkmoney.reporter.domain.enums.InvoiceEventType;
import com.rbkmoney.reporter.domain.tables.pojos.Payment;
import com.rbkmoney.reporter.mapper.InvoiceChangeMapper;
import com.rbkmoney.reporter.mapper.MapperResult;
import com.rbkmoney.reporter.util.json.FinalCashFlowUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class PaymentCashFlowChangedChangeMapperImpl implements InvoiceChangeMapper {

    @Override
    public String[] getIgnoreProperties() {
        return new String[]{"id", "wtime", "current", "eventCreatedAt", "eventType", "sequenceId", "changeId",
                "paymentCashFlow"};
    }

    @Override
    public boolean canMap(InvoiceChange payload) {
        return payload.isSetInvoicePaymentChange()
                && payload.getInvoicePaymentChange().getPayload().isSetInvoicePaymentCashFlowChanged();
    }

    @Override
    public MapperResult map(InvoiceChange payload, MachineEvent baseEvent, Integer changeId) {
        InvoicePaymentChange invoicePaymentChange = payload.getInvoicePaymentChange();
        InvoicePaymentCashFlowChanged invoicePaymentCashFlowChanged = getInvoicePaymentCashFlowChanged(invoicePaymentChange);
        List<FinalCashFlowPosting> finalCashFlowPostings = invoicePaymentCashFlowChanged.getCashFlow();

        String paymentId = invoicePaymentChange.getId();
        String invoiceId = baseEvent.getSourceId();

        Payment payment = new Payment();

        payment.setId(null);
        payment.setWtime(null);
        payment.setEventCreatedAt(TypeUtil.stringToLocalDateTime(baseEvent.getCreatedAt()));
        payment.setEventType(InvoiceEventType.INVOICE_PAYMENT_CASH_FLOW_CHANGED);
        payment.setSequenceId(baseEvent.getEventId());
        payment.setChangeId(changeId);
        payment.setPaymentCashFlow(FinalCashFlowUtil.toDtoFinalCashFlow(finalCashFlowPostings));

        log.info("Payment with eventType=cashFlowChanged has been mapped, invoiceId={}, paymentId={}", invoiceId, paymentId);

        return new MapperResult(payment);
    }

    private InvoicePaymentCashFlowChanged getInvoicePaymentCashFlowChanged(InvoicePaymentChange invoicePaymentChange) {
        return invoicePaymentChange.getPayload().getInvoicePaymentCashFlowChanged();
    }
}
