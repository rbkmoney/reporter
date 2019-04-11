package com.rbkmoney.reporter.handle.stockevent.event.change.impl;

import com.rbkmoney.damsel.domain.Failure;
import com.rbkmoney.damsel.domain.InvoicePaymentRefundStatus;
import com.rbkmoney.damsel.domain.OperationFailure;
import com.rbkmoney.damsel.event_stock.StockEvent;
import com.rbkmoney.damsel.payment_processing.*;
import com.rbkmoney.geck.common.util.TBaseUtil;
import com.rbkmoney.geck.common.util.TypeUtil;
import com.rbkmoney.geck.serializer.kit.tbase.TErrorUtil;
import com.rbkmoney.reporter.domain.enums.FailureClass;
import com.rbkmoney.reporter.domain.enums.InvoiceEventType;
import com.rbkmoney.reporter.domain.enums.RefundStatus;
import com.rbkmoney.reporter.domain.tables.pojos.Refund;
import com.rbkmoney.reporter.handle.stockevent.event.change.InvoiceChangeEventsHandler;
import com.rbkmoney.reporter.service.RefundService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class RefundStatusChangedChangeEventHandler implements InvoiceChangeEventsHandler {

    private final RefundService refundService;

    @Override
    public boolean accept(InvoiceChange change) {
        return change.isSetInvoicePaymentChange()
                && change.getInvoicePaymentChange().getPayload().isSetInvoicePaymentRefundChange()
                && change.getInvoicePaymentChange().getPayload().getInvoicePaymentRefundChange().getPayload().isSetInvoicePaymentRefundStatusChanged();
    }

    @Override
    public void handle(InvoiceChange change, StockEvent stockEvent) {
        Event event = stockEvent.getSourceEvent().getProcessingEvent();

        InvoicePaymentChange invoicePaymentChange = change.getInvoicePaymentChange();
        InvoicePaymentRefundChange invoicePaymentRefundChange = getInvoicePaymentRefundChange(invoicePaymentChange);
        InvoicePaymentRefundStatusChanged invoicePaymentRefundStatusChanged = getInvoicePaymentRefundStatusChanged(invoicePaymentRefundChange);
        InvoicePaymentRefundStatus invoicePaymentRefundStatus = invoicePaymentRefundStatusChanged.getStatus();

        String refundId = invoicePaymentRefundChange.getId();
        String paymentId = invoicePaymentChange.getId();
        String invoiceId = event.getSource().getInvoiceId();

        log.info("Start invoice payment refund status changed handling, refundId={}, invoiceId={}, paymentId={}", refundId, invoiceId, paymentId);

        Refund refund = refundService.get(invoiceId, paymentId, refundId);

        refund.setId(null);
        refund.setWtime(null);
        refund.setEventId(event.getId());
        refund.setEventCreatedAt(TypeUtil.stringToLocalDateTime(event.getCreatedAt()));
        refund.setEventType(InvoiceEventType.INVOICE_PAYMENT_REFUND_STATUS_CHANGED);
        refund.setSequenceId(event.getSequence());
        refund.setRefundStatus(TBaseUtil.unionFieldToEnum(invoicePaymentRefundStatus, RefundStatus.class));
        if (invoicePaymentRefundStatus.isSetFailed()) {
            OperationFailure operationFailure = invoicePaymentRefundStatus.getFailed().getFailure();

            refund.setRefundOperationFailureClass(TBaseUtil.unionFieldToEnum(operationFailure, FailureClass.class));
            if (operationFailure.isSetFailure()) {
                Failure failure = operationFailure.getFailure();

                refund.setRefundExternalFailure(TErrorUtil.toStringVal(failure));
                refund.setRefundExternalFailureReason(failure.getReason());
            }
        }

        refundService.updateNotCurrent(invoiceId, paymentId, refundId);
        refundService.save(refund);
        log.info("Invoice payment refund status has been changed, refundId={}, invoiceId={}, paymentId={}", refundId, invoiceId, paymentId);
    }

    private InvoicePaymentRefundStatusChanged getInvoicePaymentRefundStatusChanged(InvoicePaymentRefundChange invoicePaymentRefundChange) {
        return invoicePaymentRefundChange
                .getPayload().getInvoicePaymentRefundStatusChanged();
    }

    private InvoicePaymentRefundChange getInvoicePaymentRefundChange(InvoicePaymentChange invoicePaymentChange) {
        return invoicePaymentChange
                .getPayload().getInvoicePaymentRefundChange();
    }
}
