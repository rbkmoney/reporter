package com.rbkmoney.reporter.mapper.machineevent.payment;

import com.rbkmoney.damsel.payment_processing.InvoiceChange;
import com.rbkmoney.damsel.payment_processing.InvoicePaymentChange;
import com.rbkmoney.geck.common.util.TypeUtil;
import com.rbkmoney.machinegun.eventsink.MachineEvent;
import com.rbkmoney.reporter.domain.enums.PaymentChangeType;
import com.rbkmoney.reporter.domain.tables.pojos.CashFlow;
import com.rbkmoney.reporter.mapper.InvoiceChangeMapper;
import com.rbkmoney.reporter.mapper.MapperResult;
import com.rbkmoney.reporter.util.CashFlowUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@Slf4j
public class PaymentCashFlowChangedChangeMapperImpl implements InvoiceChangeMapper {

    @Override
    public MapperResult map(InvoiceChange payload, MachineEvent baseEvent, Integer changeId) {
        InvoicePaymentChange invoicePaymentChange = payload.getInvoicePaymentChange();
        String paymentId = invoicePaymentChange.getId();
        String invoiceId = baseEvent.getSourceId();
        Long sequenceId = baseEvent.getEventId();
        LocalDateTime createdAt = TypeUtil.stringToLocalDateTime(baseEvent.getCreatedAt());

        List<CashFlow> cashFlows = CashFlowUtil.convertCashFlows(
                invoicePaymentChange.getPayload().getInvoicePaymentCashFlowChanged().getCashFlow(),
                invoiceId,
                sequenceId,
                changeId,
                paymentId,
                createdAt,
                PaymentChangeType.payment
        );
        log.info("Payment with eventType=cashFlowChanged has been mapped, invoiceId={}, paymentId={}", invoiceId, paymentId);

        return new MapperResult(cashFlows);
    }

    @Override
    public boolean canMap(InvoiceChange payload) {
        return payload.isSetInvoicePaymentChange()
                && payload.getInvoicePaymentChange().getPayload().isSetInvoicePaymentCashFlowChanged();
    }

    @Override
    public String[] getIgnoreProperties() {
        return new String[]{"id", "wtime", "current", "eventCreatedAt", "eventType", "sequenceId", "changeId",
                "paymentCashFlow"};
    }
}
