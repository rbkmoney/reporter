package com.rbkmoney.reporter.mapper.machineevent.adjustment;

import com.rbkmoney.damsel.domain.InvoicePaymentAdjustmentStatus;
import com.rbkmoney.damsel.payment_processing.InvoiceChange;
import com.rbkmoney.damsel.payment_processing.InvoicePaymentAdjustmentChange;
import com.rbkmoney.damsel.payment_processing.InvoicePaymentChange;
import com.rbkmoney.geck.common.util.TBaseUtil;
import com.rbkmoney.geck.common.util.TypeUtil;
import com.rbkmoney.machinegun.eventsink.MachineEvent;
import com.rbkmoney.reporter.domain.enums.AdjustmentStatus;
import com.rbkmoney.reporter.domain.tables.pojos.AdjustmentState;
import com.rbkmoney.reporter.mapper.InvoiceChangeMapper;
import com.rbkmoney.reporter.mapper.MapperResult;
import com.rbkmoney.reporter.util.DamselUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
public class AdjustmentStatusChangedChangeMapperImpl implements InvoiceChangeMapper {

    @Override
    public boolean canMap(InvoiceChange payload) {
        return payload.isSetInvoicePaymentChange()
                && payload.getInvoicePaymentChange().getPayload().isSetInvoicePaymentAdjustmentChange()
                && payload.getInvoicePaymentChange().getPayload().getInvoicePaymentAdjustmentChange().getPayload().isSetInvoicePaymentAdjustmentStatusChanged();
    }

    @Override
    public MapperResult map(InvoiceChange payload, MachineEvent baseEvent, Integer changeId) {
        InvoicePaymentChange damselPaymentChange = payload.getInvoicePaymentChange();
        InvoicePaymentAdjustmentChange damselAdjustmentChange = getInvoicePaymentAdjustmentChange(damselPaymentChange);
        InvoicePaymentAdjustmentStatus damselAdjustmentStatus = getInvoicePaymentAdjustmentStatus(damselAdjustmentChange);

        String invoiceId = baseEvent.getSourceId();
        long sequenceId = baseEvent.getEventId();
        LocalDateTime eventCreatedAt = TypeUtil.stringToLocalDateTime(baseEvent.getCreatedAt());
        String paymentId = damselPaymentChange.getId();
        String adjustmentId = damselAdjustmentChange.getId();
        AdjustmentStatus status = TBaseUtil.unionFieldToEnum(damselAdjustmentStatus, AdjustmentStatus.class);

        AdjustmentState adjustmentState = getAdjustmentState(damselAdjustmentStatus, invoiceId, sequenceId, changeId, eventCreatedAt, paymentId, adjustmentId, status);

        log.info("Adjustment with eventType=[statusChanged] and status {} has been mapped, invoiceId={}, paymentId={}, adjustmentId={}", status.getLiteral(), invoiceId, paymentId, adjustmentId);

        return new MapperResult(adjustmentState);
    }

    private AdjustmentState getAdjustmentState(InvoicePaymentAdjustmentStatus damselAdjustmentStatus, String invoiceId, long sequenceId, Integer changeId, LocalDateTime eventCreatedAt, String paymentId, String adjustmentId, AdjustmentStatus status) {
        AdjustmentState adjustmentState = new AdjustmentState();
        adjustmentState.setInvoiceId(invoiceId);
        adjustmentState.setSequenceId(sequenceId);
        adjustmentState.setChangeId(changeId);
        adjustmentState.setEventCreatedAt(eventCreatedAt);
        adjustmentState.setPaymentId(paymentId);
        adjustmentState.setAdjustmentId(adjustmentId);
        adjustmentState.setStatus(status);
        adjustmentState.setStatusCreatedAt(DamselUtil.getAdjustmentStatusCreatedAt(damselAdjustmentStatus));

        return adjustmentState;
    }

    private InvoicePaymentAdjustmentChange getInvoicePaymentAdjustmentChange(InvoicePaymentChange damselPaymentChange) {
        return damselPaymentChange
                .getPayload().getInvoicePaymentAdjustmentChange();
    }

    private InvoicePaymentAdjustmentStatus getInvoicePaymentAdjustmentStatus(InvoicePaymentAdjustmentChange damselAdjustmentChange) {
        return damselAdjustmentChange
                .getPayload().getInvoicePaymentAdjustmentStatusChanged()
                .getStatus();
    }
}
