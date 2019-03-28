package com.rbkmoney.reporter.handle.stockevent.event.change.impl;

import com.rbkmoney.damsel.domain.InvoicePaymentAdjustment;
import com.rbkmoney.damsel.event_stock.StockEvent;
import com.rbkmoney.damsel.payment_processing.Event;
import com.rbkmoney.damsel.payment_processing.InvoiceChange;
import com.rbkmoney.damsel.payment_processing.InvoicePaymentAdjustmentChange;
import com.rbkmoney.damsel.payment_processing.InvoicePaymentChange;
import com.rbkmoney.geck.common.util.TBaseUtil;
import com.rbkmoney.geck.common.util.TypeUtil;
import com.rbkmoney.reporter.domain.enums.AdjustmentStatus;
import com.rbkmoney.reporter.domain.enums.InvoiceEventType;
import com.rbkmoney.reporter.domain.tables.pojos.Adjustment;
import com.rbkmoney.reporter.domain.tables.pojos.Payment;
import com.rbkmoney.reporter.handle.stockevent.event.change.InvoiceChangeEventsHandler;
import com.rbkmoney.reporter.service.AdjustmentService;
import com.rbkmoney.reporter.service.PaymentService;
import com.rbkmoney.reporter.util.CashFlowUtil;
import com.rbkmoney.reporter.util.DamselUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@Slf4j
@RequiredArgsConstructor
public class AdjustmentCreatedChangeEventHandler implements InvoiceChangeEventsHandler {

    private final AdjustmentService adjustmentService;
    private final PaymentService paymentService;

    @Override
    public boolean accept(InvoiceChange change) {
        return change.isSetInvoicePaymentChange()
                && change.getInvoicePaymentChange().getPayload().isSetInvoicePaymentAdjustmentChange()
                && change.getInvoicePaymentChange().getPayload().getInvoicePaymentAdjustmentChange().getPayload().isSetInvoicePaymentAdjustmentCreated();
    }

    @Override
    public void handle(InvoiceChange change, StockEvent stockEvent) {
        Event event = stockEvent.getSourceEvent().getProcessingEvent();

        InvoicePaymentChange invoicePaymentChange = change.getInvoicePaymentChange();
        InvoicePaymentAdjustmentChange invoicePaymentAdjustmentChange = getInvoicePaymentAdjustmentChange(invoicePaymentChange);
        InvoicePaymentAdjustment invoicePaymentAdjustment = getInvoicePaymentAdjustment(invoicePaymentAdjustmentChange);

        String adjustmentId = invoicePaymentAdjustmentChange.getId();
        String paymentId = invoicePaymentChange.getId();
        String invoiceId = event.getSource().getInvoiceId();

        Payment payment = paymentService.get(invoiceId, paymentId);

        log.info("Start invoice payment adjustment created handling, adjustmentId={}, invoiceId={}, paymentId={}", adjustmentId, invoiceId, paymentId);

        Adjustment adjustment = new Adjustment();
        adjustment.setEventId(event.getId());
        adjustment.setEventCreatedAt(TypeUtil.stringToLocalDateTime(event.getCreatedAt()));
        adjustment.setEventType(InvoiceEventType.INVOICE_PAYMENT_ADJUSTMENT_CREATED);
        adjustment.setSequenceId(event.getSequence());
        adjustment.setInvoiceId(invoiceId);
        adjustment.setPaymentId(paymentId);
        adjustment.setPartyId(payment.getPaymentId());
        adjustment.setPartyShopId(payment.getPartyShopId());
        adjustment.setAdjustmentId(adjustmentId);
        fillAdjustmentStatus(invoicePaymentAdjustment, adjustment);
        adjustment.setAdjustmentCreatedAt(getAdjustmentCreatedAt(invoicePaymentAdjustment));
        adjustment.setAdjustmentDomainRevision(invoicePaymentAdjustment.getDomainRevision());
        adjustment.setAdjustmentReason(invoicePaymentAdjustment.getReason());
        adjustment.setAdjustmentCashFlow(CashFlowUtil.toDtoFinalCashFlow(invoicePaymentAdjustment.getNewCashFlow()));
        adjustment.setAdjustmentCashFlowInverseOld(CashFlowUtil.toDtoFinalCashFlow(invoicePaymentAdjustment.getOldCashFlowInverse()));
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
