package com.rbkmoney.reporter.handle.machineevent.payment.change.impl;

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
import com.rbkmoney.reporter.handle.machineevent.payment.change.InvoiceChangeMachineEventHandler;
import com.rbkmoney.reporter.service.AdjustmentService;
import com.rbkmoney.reporter.util.DamselUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@Slf4j
@RequiredArgsConstructor
public class AdjustmentStatusChangedChangeMachineEventHandler implements InvoiceChangeMachineEventHandler {

    private final AdjustmentService adjustmentService;

    @Override
    public boolean accept(InvoiceChange payload) {
        return payload.isSetInvoicePaymentChange()
                && payload.getInvoicePaymentChange().getPayload().isSetInvoicePaymentAdjustmentChange()
                && payload.getInvoicePaymentChange().getPayload().getInvoicePaymentAdjustmentChange().getPayload().isSetInvoicePaymentAdjustmentStatusChanged();
    }

    @Override
    public void handle(InvoiceChange payload, MachineEvent baseEvent, Integer changeId) {
        InvoicePaymentChange invoicePaymentChange = payload.getInvoicePaymentChange();
        InvoicePaymentAdjustmentChange invoicePaymentAdjustmentChange = getInvoicePaymentAdjustmentChange(invoicePaymentChange);
        InvoicePaymentAdjustmentStatus invoicePaymentAdjustmentStatus = getInvoicePaymentAdjustmentStatus(invoicePaymentAdjustmentChange);

        String adjustmentId = invoicePaymentAdjustmentChange.getId();
        String paymentId = invoicePaymentChange.getId();
        String invoiceId = baseEvent.getSourceId();

        log.info("Start invoice payment adjustment status changed handling, adjustmentId={}, invoiceId={}, paymentId={}", adjustmentId, invoiceId, paymentId);

        Adjustment adjustment = adjustmentService.get(invoiceId, paymentId, adjustmentId);

        adjustment.setId(null);
        adjustment.setWtime(null);
        adjustment.setEventCreatedAt(TypeUtil.stringToLocalDateTime(baseEvent.getCreatedAt()));
        adjustment.setEventType(InvoiceEventType.INVOICE_PAYMENT_ADJUSTMENT_STATUS_CHANGED);
        adjustment.setSequenceId(baseEvent.getEventId());
        adjustment.setChangeId(changeId);
        adjustment.setAdjustmentStatus(TBaseUtil.unionFieldToEnum(invoicePaymentAdjustmentStatus, AdjustmentStatus.class));
        adjustment.setAdjustmentStatusCreatedAt(getAdjustmentStatusCreatedAt(invoicePaymentAdjustmentStatus));

        adjustmentService.updateNotCurrent(invoiceId, paymentId, adjustmentId);
        adjustmentService.save(adjustment);
        log.info("Invoice payment adjustment status has been changed, adjustmentId={}, invoiceId={}, paymentId={}", adjustmentId, invoiceId, paymentId);
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
