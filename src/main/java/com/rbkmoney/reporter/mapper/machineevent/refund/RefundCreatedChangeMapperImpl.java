package com.rbkmoney.reporter.mapper.machineevent.refund;

import com.rbkmoney.damsel.domain.Cash;
import com.rbkmoney.damsel.domain.InvoicePaymentRefund;
import com.rbkmoney.damsel.payment_processing.InvoiceChange;
import com.rbkmoney.damsel.payment_processing.InvoicePaymentChange;
import com.rbkmoney.damsel.payment_processing.InvoicePaymentRefundChange;
import com.rbkmoney.damsel.payment_processing.InvoicePaymentRefundCreated;
import com.rbkmoney.geck.common.util.TBaseUtil;
import com.rbkmoney.geck.common.util.TypeUtil;
import com.rbkmoney.machinegun.eventsink.MachineEvent;
import com.rbkmoney.reporter.domain.enums.RefundStatus;
import com.rbkmoney.reporter.domain.tables.pojos.Refund;
import com.rbkmoney.reporter.domain.tables.pojos.RefundState;
import com.rbkmoney.reporter.mapper.InvoiceChangeMapper;
import com.rbkmoney.reporter.mapper.MapperResult;
import com.rbkmoney.reporter.util.DamselUtil;
import com.rbkmoney.reporter.util.FeeType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;

import static com.rbkmoney.reporter.util.FeeTypeMapUtil.isContainsAmount;

@Component
@Slf4j
public class RefundCreatedChangeMapperImpl implements InvoiceChangeMapper {

    @Override
    public boolean canMap(InvoiceChange payload) {
        return payload.isSetInvoicePaymentChange()
                && payload.getInvoicePaymentChange().getPayload().isSetInvoicePaymentRefundChange()
                && payload.getInvoicePaymentChange().getPayload().getInvoicePaymentRefundChange().getPayload().isSetInvoicePaymentRefundCreated();
    }

    @Override
    public MapperResult map(InvoiceChange payload, MachineEvent baseEvent, Integer changeId) {
        InvoicePaymentChange damselPaymentChange = payload.getInvoicePaymentChange();
        InvoicePaymentRefundChange damselRefundChange = getInvoicePaymentRefundChange(damselPaymentChange);
        InvoicePaymentRefundCreated damselRefundChangeCreated = getInvoicePaymentRefundCreated(damselRefundChange);
        InvoicePaymentRefund damselRefund = damselRefundChangeCreated.getRefund();

        String invoiceId = baseEvent.getSourceId();
        long sequenceId = baseEvent.getEventId();
        LocalDateTime eventCreatedAt = TypeUtil.stringToLocalDateTime(baseEvent.getCreatedAt());
        String paymentId = damselPaymentChange.getId();
        String refundId = damselRefundChange.getId();

        Refund refund = getRefund(damselRefundChangeCreated, invoiceId, paymentId, refundId);

        RefundState refundState = getRefundState(changeId, damselRefund, invoiceId, sequenceId, eventCreatedAt, paymentId, refundId);

        log.info("Refund with eventType=[created] has been mapped, invoiceId={}, paymentId={}, refundId={}", invoiceId, paymentId, refundId);

        return new MapperResult(refund, refundState);
    }

    private Refund getRefund(InvoicePaymentRefundCreated damselRefundChangeCreated, String invoiceId, String paymentId, String refundId) {
        InvoicePaymentRefund damselRefund = damselRefundChangeCreated.getRefund();

        Map<FeeType, Long> fees = DamselUtil.getFees(damselRefundChangeCreated.getCashFlow());
        Map<FeeType, String> currencies = DamselUtil.getCurrency(damselRefundChangeCreated.getCashFlow());

        Refund refund = new Refund();
        refund.setInvoiceId(invoiceId);
        refund.setPaymentId(paymentId);
        refund.setRefundId(refundId);
        refund.setCreatedAt(getRefundCreatedAt(damselRefund));
        refund.setDomainRevision(damselRefund.getDomainRevision());
        if (damselRefund.isSetPartyRevision()) {
            refund.setPartyRevision(damselRefund.getPartyRevision());
        }
        if (damselRefund.isSetCash()) {
            Cash cash = damselRefund.getCash();

            refund.setAmount(cash.getAmount());
            refund.setCurrencyCode(cash.getCurrency().getSymbolicCode());
        }
        refund.setReason(damselRefund.getReason());
        if (refund.getAmount() == null && isContainsAmount(fees)) {
            refund.setAmount(fees.get(FeeType.AMOUNT));
            refund.setCurrencyCode(currencies.get(FeeType.AMOUNT));
        }
        refund.setFee(fees.get(FeeType.FEE));
        refund.setFeeCurrencyCode(currencies.get(FeeType.FEE));
        refund.setProviderFee(fees.get(FeeType.PROVIDER_FEE));
        refund.setProviderFeeCurrencyCode(currencies.get(FeeType.PROVIDER_FEE));
        refund.setExternalFee(fees.get(FeeType.EXTERNAL_FEE));
        refund.setExternalFeeCurrencyCode(currencies.get(FeeType.EXTERNAL_FEE));

        return refund;
    }

    private RefundState getRefundState(Integer changeId, InvoicePaymentRefund damselRefund, String invoiceId, long sequenceId, LocalDateTime eventCreatedAt, String paymentId, String refundId) {
        RefundState refundState = new RefundState();
        refundState.setInvoiceId(invoiceId);
        refundState.setSequenceId(sequenceId);
        refundState.setChangeId(changeId);
        refundState.setEventCreatedAt(eventCreatedAt);
        refundState.setPaymentId(paymentId);
        refundState.setRefundId(refundId);
        refundState.setStatus(getRefundStatus(damselRefund));

        return refundState;
    }

    private LocalDateTime getRefundCreatedAt(InvoicePaymentRefund invoicePaymentRefund) {
        return TypeUtil.stringToLocalDateTime(invoicePaymentRefund.getCreatedAt());
    }

    private RefundStatus getRefundStatus(InvoicePaymentRefund invoicePaymentRefund) {
        return TBaseUtil.unionFieldToEnum(invoicePaymentRefund.getStatus(), RefundStatus.class);
    }

    private InvoicePaymentRefundCreated getInvoicePaymentRefundCreated(InvoicePaymentRefundChange damselRefundChange) {
        return damselRefundChange
                .getPayload().getInvoicePaymentRefundCreated();
    }

    private InvoicePaymentRefundChange getInvoicePaymentRefundChange(InvoicePaymentChange damselPaymentChange) {
        return damselPaymentChange
                .getPayload().getInvoicePaymentRefundChange();
    }
}
