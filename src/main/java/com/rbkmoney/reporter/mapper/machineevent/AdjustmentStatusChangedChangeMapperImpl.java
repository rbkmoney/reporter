package com.rbkmoney.reporter.mapper.machineevent;

import com.rbkmoney.damsel.domain.InvoicePaymentAdjustmentStatus;
import com.rbkmoney.damsel.payment_processing.InvoiceChange;
import com.rbkmoney.damsel.payment_processing.InvoicePaymentAdjustmentChange;
import com.rbkmoney.damsel.payment_processing.InvoicePaymentChange;
import com.rbkmoney.geck.common.util.TBaseUtil;
import com.rbkmoney.geck.common.util.TypeUtil;
import com.rbkmoney.machinegun.eventsink.MachineEvent;
import com.rbkmoney.reporter.domain.enums.AdjustmentStatus;
import com.rbkmoney.reporter.domain.enums.InvoiceEventType;
import com.rbkmoney.reporter.domain.tables.pojos.Adjustment;
import com.rbkmoney.reporter.mapper.InvoiceChangeMapper;
import com.rbkmoney.reporter.mapper.MapperResult;
import com.rbkmoney.reporter.util.DamselUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@Slf4j
public class AdjustmentStatusChangedChangeMapperImpl implements InvoiceChangeMapper {

    @Override
    public String[] getIgnoreProperties() {
        return new String[]{"id", "wtime", "current", "eventCreatedAt", "eventType", "sequenceId", "changeId",
                "adjustmentStatus", "adjustmentStatusCreatedAt"};
    }

    @Override
    public boolean canMap(InvoiceChange payload) {
        return payload.isSetInvoicePaymentChange()
                && payload.getInvoicePaymentChange().getPayload().isSetInvoicePaymentAdjustmentChange()
                && payload.getInvoicePaymentChange().getPayload().getInvoicePaymentAdjustmentChange().getPayload().isSetInvoicePaymentAdjustmentStatusChanged();
    }

    @Override
    public MapperResult map(InvoiceChange payload, MachineEvent baseEvent, Integer changeId) {
        InvoicePaymentChange invoicePaymentChange = payload.getInvoicePaymentChange();
        InvoicePaymentAdjustmentChange invoicePaymentAdjustmentChange = getInvoicePaymentAdjustmentChange(invoicePaymentChange);
        InvoicePaymentAdjustmentStatus invoicePaymentAdjustmentStatus = getInvoicePaymentAdjustmentStatus(invoicePaymentAdjustmentChange);
        AdjustmentStatus adjustmentStatus = TBaseUtil.unionFieldToEnum(invoicePaymentAdjustmentStatus, AdjustmentStatus.class);

        String adjustmentId = invoicePaymentAdjustmentChange.getId();
        String paymentId = invoicePaymentChange.getId();
        String invoiceId = baseEvent.getSourceId();

        Adjustment adjustment = new Adjustment();

        adjustment.setId(null);
        adjustment.setWtime(null);
        adjustment.setEventCreatedAt(TypeUtil.stringToLocalDateTime(baseEvent.getCreatedAt()));
        adjustment.setEventType(InvoiceEventType.INVOICE_PAYMENT_ADJUSTMENT_STATUS_CHANGED);
        adjustment.setSequenceId(baseEvent.getEventId());
        adjustment.setChangeId(changeId);
        adjustment.setAdjustmentStatus(adjustmentStatus);
        adjustment.setAdjustmentStatusCreatedAt(getAdjustmentStatusCreatedAt(invoicePaymentAdjustmentStatus));

        log.info("Adjustment with eventType=statusChanged and status {} has been mapped, invoiceId={}, paymentId={}, adjustmentId={}", adjustmentStatus, invoiceId, paymentId, adjustmentId);

        return new MapperResult(adjustment);
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

    private LocalDateTime getAdjustmentStatusCreatedAt(InvoicePaymentAdjustmentStatus status) {
        return DamselUtil.getAdjustmentStatusCreatedAt(status);
    }
}
