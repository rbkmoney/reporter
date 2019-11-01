package com.rbkmoney.reporter.mapper.machineevent.adjustment;

import com.rbkmoney.damsel.domain.InvoicePaymentAdjustment;
import com.rbkmoney.damsel.payment_processing.InvoiceChange;
import com.rbkmoney.damsel.payment_processing.InvoicePaymentAdjustmentChange;
import com.rbkmoney.damsel.payment_processing.InvoicePaymentChange;
import com.rbkmoney.geck.common.util.TBaseUtil;
import com.rbkmoney.geck.common.util.TypeUtil;
import com.rbkmoney.machinegun.eventsink.MachineEvent;
import com.rbkmoney.reporter.domain.enums.AdjustmentStatus;
import com.rbkmoney.reporter.domain.tables.pojos.Adjustment;
import com.rbkmoney.reporter.domain.tables.pojos.AdjustmentState;
import com.rbkmoney.reporter.mapper.InvoiceChangeMapper;
import com.rbkmoney.reporter.mapper.MapperResult;
import com.rbkmoney.reporter.util.DamselUtil;
import com.rbkmoney.reporter.util.FeeType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;

@Component
@Slf4j
public class AdjustmentCreatedChangeMapperImpl implements InvoiceChangeMapper {

    @Override
    public boolean canMap(InvoiceChange payload) {
        return payload.isSetInvoicePaymentChange()
                && payload.getInvoicePaymentChange().getPayload().isSetInvoicePaymentAdjustmentChange()
                && payload.getInvoicePaymentChange().getPayload().getInvoicePaymentAdjustmentChange().getPayload().isSetInvoicePaymentAdjustmentCreated();
    }

    @Override
    public MapperResult map(InvoiceChange payload, MachineEvent baseEvent, Integer changeId) {
        InvoicePaymentChange damselPaymentChange = payload.getInvoicePaymentChange();
        InvoicePaymentAdjustmentChange damselAdjustmentChange = getInvoicePaymentAdjustmentChange(damselPaymentChange);
        InvoicePaymentAdjustment damselAdjustment = getInvoicePaymentAdjustment(damselAdjustmentChange);

        String invoiceId = baseEvent.getSourceId();
        long sequenceId = baseEvent.getEventId();
        LocalDateTime eventCreatedAt = TypeUtil.stringToLocalDateTime(baseEvent.getCreatedAt());
        String paymentId = damselPaymentChange.getId();
        String adjustmentId = damselAdjustmentChange.getId();

        Adjustment adjustment = getAdjustment(damselAdjustment, invoiceId, paymentId, adjustmentId);

        AdjustmentState adjustmentState = getAdjustmentState(damselAdjustment, invoiceId, sequenceId, changeId, eventCreatedAt, paymentId, adjustmentId);

        log.info("Adjustment with eventType=[created] has been mapped, invoiceId={}, paymentId={}, adjustmentId={}", invoiceId, paymentId, adjustmentId);

        return new MapperResult(adjustment, adjustmentState);
    }

    private Adjustment getAdjustment(InvoicePaymentAdjustment damselAdjustment, String invoiceId, String paymentId, String adjustmentId) {
        Map<FeeType, Long> fees = DamselUtil.getFees(damselAdjustment.getNewCashFlow());
        Map<FeeType, String> currencies = DamselUtil.getCurrency(damselAdjustment.getNewCashFlow());
        Map<FeeType, Long> oldFees = DamselUtil.getFees(damselAdjustment.getOldCashFlowInverse());
        Map<FeeType, String> oldCurrencies = DamselUtil.getCurrency(damselAdjustment.getOldCashFlowInverse());

        Adjustment adjustment = new Adjustment();
        adjustment.setInvoiceId(invoiceId);
        adjustment.setPaymentId(paymentId);
        adjustment.setAdjustmentId(adjustmentId);
        adjustment.setCreatedAt(getAdjustmentCreatedAt(damselAdjustment));
        adjustment.setDomainRevision(damselAdjustment.getDomainRevision());
        adjustment.setReason(damselAdjustment.getReason());
        if (damselAdjustment.isSetPartyRevision()) {
            adjustment.setPartyRevision(damselAdjustment.getPartyRevision());
        }
        adjustment.setFee(fees.get(FeeType.FEE));
        adjustment.setFeeCurrencyCode(currencies.get(FeeType.FEE));
        adjustment.setProviderFee(fees.get(FeeType.PROVIDER_FEE));
        adjustment.setProviderFeeCurrencyCode(currencies.get(FeeType.PROVIDER_FEE));
        adjustment.setExternalFee(fees.get(FeeType.EXTERNAL_FEE));
        adjustment.setExternalFeeCurrencyCode(currencies.get(FeeType.EXTERNAL_FEE));
        adjustment.setOldFee(oldFees.get(FeeType.FEE));
        adjustment.setOldFeeCurrencyCode(oldCurrencies.get(FeeType.FEE));
        adjustment.setOldProviderFee(oldFees.get(FeeType.PROVIDER_FEE));
        adjustment.setOldProviderFeeCurrencyCode(oldCurrencies.get(FeeType.PROVIDER_FEE));
        adjustment.setOldExternalFee(oldFees.get(FeeType.EXTERNAL_FEE));
        adjustment.setOldExternalFeeCurrencyCode(oldCurrencies.get(FeeType.EXTERNAL_FEE));

        return adjustment;
    }

    private AdjustmentState getAdjustmentState(InvoicePaymentAdjustment damselAdjustment, String invoiceId, long sequenceId, Integer changeId, LocalDateTime eventCreatedAt, String paymentId, String adjustmentId) {
        AdjustmentState adjustmentState = new AdjustmentState();
        adjustmentState.setInvoiceId(invoiceId);
        adjustmentState.setSequenceId(sequenceId);
        adjustmentState.setChangeId(changeId);
        adjustmentState.setEventCreatedAt(eventCreatedAt);
        adjustmentState.setPaymentId(paymentId);
        adjustmentState.setAdjustmentId(adjustmentId);
        adjustmentState.setStatus(TBaseUtil.unionFieldToEnum(damselAdjustment.getStatus(), AdjustmentStatus.class));
        adjustmentState.setStatusCreatedAt(DamselUtil.getAdjustmentStatusCreatedAt(damselAdjustment.getStatus()));

        return adjustmentState;
    }

    private InvoicePaymentAdjustmentChange getInvoicePaymentAdjustmentChange(InvoicePaymentChange damselPaymentChange) {
        return damselPaymentChange
                .getPayload().getInvoicePaymentAdjustmentChange();
    }

    private InvoicePaymentAdjustment getInvoicePaymentAdjustment(InvoicePaymentAdjustmentChange damselAdjustmentChange) {
        return damselAdjustmentChange
                .getPayload().getInvoicePaymentAdjustmentCreated()
                .getAdjustment();
    }

    private LocalDateTime getAdjustmentCreatedAt(InvoicePaymentAdjustment damselAdjustment) {
        return TypeUtil.stringToLocalDateTime(damselAdjustment.getCreatedAt());
    }
}
