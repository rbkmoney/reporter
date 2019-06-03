package com.rbkmoney.reporter.handle.machineevent;

import com.rbkmoney.damsel.domain.InvoicePaymentAdjustment;
import com.rbkmoney.damsel.payment_processing.InvoiceChange;
import com.rbkmoney.damsel.payment_processing.InvoicePaymentAdjustmentChange;
import com.rbkmoney.damsel.payment_processing.InvoicePaymentChange;
import com.rbkmoney.geck.common.util.TBaseUtil;
import com.rbkmoney.geck.common.util.TypeUtil;
import com.rbkmoney.machinegun.eventsink.MachineEvent;
import com.rbkmoney.reporter.domain.enums.AdjustmentStatus;
import com.rbkmoney.reporter.domain.enums.InvoiceEventType;
import com.rbkmoney.reporter.domain.tables.pojos.Adjustment;
import com.rbkmoney.reporter.domain.tables.pojos.Payment;
import com.rbkmoney.reporter.service.AdjustmentService;
import com.rbkmoney.reporter.service.PaymentService;
import com.rbkmoney.reporter.util.DamselUtil;
import com.rbkmoney.reporter.util.json.FinalCashFlowUtil;
import com.rbkmoney.sink.common.handle.machineevent.eventpayload.change.InvoiceChangeEventHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@Slf4j
@RequiredArgsConstructor
public class AdjustmentCreatedChangeMachineEventHandler implements InvoiceChangeEventHandler {

    private final AdjustmentService adjustmentService;
    private final PaymentService paymentService;

    @Override
    public boolean accept(InvoiceChange payload) {
        return payload.isSetInvoicePaymentChange()
                && payload.getInvoicePaymentChange().getPayload().isSetInvoicePaymentAdjustmentChange()
                && payload.getInvoicePaymentChange().getPayload().getInvoicePaymentAdjustmentChange().getPayload().isSetInvoicePaymentAdjustmentCreated();
    }

    @Override
    public void handle(InvoiceChange payload, MachineEvent baseEvent, Integer changeId) {
        InvoicePaymentChange invoicePaymentChange = payload.getInvoicePaymentChange();
        InvoicePaymentAdjustmentChange invoicePaymentAdjustmentChange = getInvoicePaymentAdjustmentChange(invoicePaymentChange);
        InvoicePaymentAdjustment invoicePaymentAdjustment = getInvoicePaymentAdjustment(invoicePaymentAdjustmentChange);

        String adjustmentId = invoicePaymentAdjustmentChange.getId();
        String paymentId = invoicePaymentChange.getId();
        String invoiceId = baseEvent.getSourceId();

        Payment payment = paymentService.get(invoiceId, paymentId);

        log.info("Start invoice payment adjustment created handling, adjustmentId={}, invoiceId={}, paymentId={}", adjustmentId, invoiceId, paymentId);

        Adjustment adjustment = new Adjustment();
        adjustment.setEventCreatedAt(TypeUtil.stringToLocalDateTime(baseEvent.getCreatedAt()));
        adjustment.setEventType(InvoiceEventType.INVOICE_PAYMENT_ADJUSTMENT_CREATED);
        adjustment.setInvoiceId(invoiceId);
        adjustment.setSequenceId(baseEvent.getEventId());
        adjustment.setChangeId(changeId);
        adjustment.setPaymentId(paymentId);
        adjustment.setPartyId(payment.getPartyId());
        adjustment.setPartyShopId(payment.getPartyShopId());
        adjustment.setAdjustmentId(adjustmentId);
        fillAdjustmentStatus(invoicePaymentAdjustment, adjustment);
        adjustment.setAdjustmentCreatedAt(getAdjustmentCreatedAt(invoicePaymentAdjustment));
        adjustment.setAdjustmentDomainRevision(invoicePaymentAdjustment.getDomainRevision());
        adjustment.setAdjustmentReason(invoicePaymentAdjustment.getReason());
        adjustment.setAdjustmentCashFlow(FinalCashFlowUtil.toDtoFinalCashFlow(invoicePaymentAdjustment.getNewCashFlow()));
        adjustment.setAdjustmentCashFlowInverseOld(FinalCashFlowUtil.toDtoFinalCashFlow(invoicePaymentAdjustment.getOldCashFlowInverse()));
        if (invoicePaymentAdjustment.isSetPartyRevision()) {
            adjustment.setAdjustmentPartyRevision(invoicePaymentAdjustment.getPartyRevision());
        }

        adjustmentService.save(adjustment);
        log.info("Invoice payment adjustment has been created, adjustmentId={}, invoiceId={}, paymentId={}", adjustmentId, invoiceId, paymentId);
    }

    private void fillAdjustmentStatus(InvoicePaymentAdjustment invoicePaymentAdjustment, Adjustment adjustment) {
        adjustment.setAdjustmentStatus(TBaseUtil.unionFieldToEnum(invoicePaymentAdjustment.getStatus(), AdjustmentStatus.class));
        adjustment.setAdjustmentStatusCreatedAt(getAdjustmentStatusCreatedAt(invoicePaymentAdjustment));
    }

    private LocalDateTime getAdjustmentStatusCreatedAt(InvoicePaymentAdjustment invoicePaymentAdjustment) {
        return DamselUtil.getAdjustmentStatusCreatedAt(invoicePaymentAdjustment.getStatus());
    }

    private LocalDateTime getAdjustmentCreatedAt(InvoicePaymentAdjustment invoicePaymentAdjustment) {
        return TypeUtil.stringToLocalDateTime(invoicePaymentAdjustment.getCreatedAt());
    }

    private InvoicePaymentAdjustmentChange getInvoicePaymentAdjustmentChange(InvoicePaymentChange invoicePaymentChange) {
        return invoicePaymentChange
                .getPayload().getInvoicePaymentAdjustmentChange();
    }

    private InvoicePaymentAdjustment getInvoicePaymentAdjustment(InvoicePaymentAdjustmentChange invoicePaymentAdjustmentChange) {
        return invoicePaymentAdjustmentChange
                .getPayload().getInvoicePaymentAdjustmentCreated()
                .getAdjustment();
    }
}
