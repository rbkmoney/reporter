package com.rbkmoney.reporter.mapper.machineevent.adjustment;

import com.rbkmoney.damsel.domain.InvoicePaymentAdjustmentStatus;
import com.rbkmoney.damsel.payment_processing.InvoiceChange;
import com.rbkmoney.damsel.payment_processing.InvoicePaymentAdjustmentChange;
import com.rbkmoney.damsel.payment_processing.InvoicePaymentChange;
import com.rbkmoney.geck.common.util.TBaseUtil;
import com.rbkmoney.machinegun.eventsink.MachineEvent;
import com.rbkmoney.reporter.domain.enums.AdjustmentStatus;
import com.rbkmoney.reporter.domain.tables.pojos.AdjustmentState;
import com.rbkmoney.reporter.mapper.InvoiceChangeMapper;
import com.rbkmoney.reporter.mapper.MapperResult;
import com.rbkmoney.reporter.util.DamselUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class AdjustmentStatusChangedChangeMapperImpl implements InvoiceChangeMapper {

    @Override
    public MapperResult map(InvoiceChange payload, MachineEvent baseEvent, Integer changeId) {
        InvoicePaymentChange invoicePaymentChange = payload.getInvoicePaymentChange();
        InvoicePaymentAdjustmentChange invoicePaymentAdjustmentChange =
                getInvoicePaymentAdjustmentChange(invoicePaymentChange);
        InvoicePaymentAdjustmentStatus invoicePaymentAdjustmentStatus =
                getInvoicePaymentAdjustmentStatus(invoicePaymentAdjustmentChange);
        AdjustmentStatus status = TBaseUtil.unionFieldToEnum(invoicePaymentAdjustmentStatus, AdjustmentStatus.class);

        String invoiceId = baseEvent.getSourceId();
        Long sequenceId = baseEvent.getEventId();

        AdjustmentState adjustmentState = new AdjustmentState();
        adjustmentState.setInvoiceId(invoiceId);
        adjustmentState.setSequenceId(sequenceId);
        adjustmentState.setChangeId(changeId);
        adjustmentState.setCreatedAt(DamselUtil.getAdjustmentStatusCreatedAt(invoicePaymentAdjustmentStatus));
        adjustmentState.setStatus(status);

        log.info("Adjustment with eventType=statusChanged and status {} has been mapped, invoiceId={}, " +
                "paymentId={}, adjustmentId={}", status, invoiceId, invoicePaymentChange.getId(),
                invoicePaymentAdjustmentChange.getId());

        return new MapperResult(adjustmentState);
    }

    @Override
    public boolean canMap(InvoiceChange payload) {
        return payload.isSetInvoicePaymentChange()
                && payload.getInvoicePaymentChange().getPayload().isSetInvoicePaymentAdjustmentChange()
                && payload.getInvoicePaymentChange().getPayload().getInvoicePaymentAdjustmentChange().getPayload().isSetInvoicePaymentAdjustmentStatusChanged();
    }

    @Override
    public String[] getIgnoreProperties() {
        return new String[]{"id", "wtime", "current", "eventCreatedAt", "eventType", "sequenceId", "changeId",
                "adjustmentStatus", "adjustmentStatusCreatedAt"};
    }

    private InvoicePaymentAdjustmentChange getInvoicePaymentAdjustmentChange(InvoicePaymentChange invoicePaymentChange) {
        return invoicePaymentChange
                .getPayload().getInvoicePaymentAdjustmentChange();
    }

    private InvoicePaymentAdjustmentStatus getInvoicePaymentAdjustmentStatus(InvoicePaymentAdjustmentChange invoicePaymentAdjustmentChange) {
        return invoicePaymentAdjustmentChange
                .getPayload().getInvoicePaymentAdjustmentStatusChanged()
                .getStatus();
    }

}
