package com.rbkmoney.reporter.mapper.machineevent.payment;

import com.rbkmoney.damsel.domain.FinalCashFlowPosting;
import com.rbkmoney.damsel.payment_processing.InvoiceChange;
import com.rbkmoney.damsel.payment_processing.InvoicePaymentChange;
import com.rbkmoney.geck.common.util.TypeUtil;
import com.rbkmoney.machinegun.eventsink.MachineEvent;
import com.rbkmoney.reporter.domain.tables.pojos.PaymentCost;
import com.rbkmoney.reporter.domain.tables.pojos.PaymentFee;
import com.rbkmoney.reporter.mapper.InvoiceChangeMapper;
import com.rbkmoney.reporter.mapper.MapperResult;
import com.rbkmoney.reporter.util.DamselUtil;
import com.rbkmoney.reporter.util.FeeType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static com.rbkmoney.reporter.util.FeeTypeMapUtil.isContainsAmount;
import static com.rbkmoney.reporter.util.FeeTypeMapUtil.isContainsAnyFee;
import static com.rbkmoney.reporter.util.MapperUtil.getPaymentCost;
import static com.rbkmoney.reporter.util.MapperUtil.getPaymentFee;

@Component
@Slf4j
public class PaymentCashFlowChangedChangeMapperImpl implements InvoiceChangeMapper {

    @Override
    public boolean canMap(InvoiceChange payload) {
        return payload.isSetInvoicePaymentChange()
                && payload.getInvoicePaymentChange().getPayload().isSetInvoicePaymentCashFlowChanged();
    }

    @Override
    public MapperResult map(InvoiceChange payload, MachineEvent baseEvent, Integer changeId) {
        InvoicePaymentChange damselPaymentChange = payload.getInvoicePaymentChange();

        String invoiceId = baseEvent.getSourceId();
        long sequenceId = baseEvent.getEventId();
        LocalDateTime eventCreatedAt = TypeUtil.stringToLocalDateTime(baseEvent.getCreatedAt());
        String paymentId = damselPaymentChange.getId();
        List<FinalCashFlowPosting> cashFlowPostings = damselPaymentChange.getPayload().getInvoicePaymentCashFlowChanged().getCashFlow();
        Map<FeeType, Long> fees = DamselUtil.getFees(cashFlowPostings);
        Map<FeeType, String> currencies = DamselUtil.getCurrency(cashFlowPostings);

        PaymentCost paymentCost = null;
        if (isContainsAmount(fees)) {
            paymentCost = getPaymentCost(invoiceId, sequenceId, changeId, eventCreatedAt, paymentId, fees.get(FeeType.AMOUNT), currencies.get(FeeType.AMOUNT));
        }

        PaymentFee paymentFee = null;
        if (isContainsAnyFee(fees)) {
            paymentFee = getPaymentFee(invoiceId, changeId, sequenceId, eventCreatedAt, paymentId, fees, currencies);
        }

        log.info("Payment with eventType=[cashFlowChanged] has been mapped, invoiceId={}, paymentId={}", invoiceId, paymentId);

        return new MapperResult(paymentCost, paymentFee);
    }
}
