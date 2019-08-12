package com.rbkmoney.reporter.mapper.machineevent;

import com.rbkmoney.damsel.domain.Failure;
import com.rbkmoney.damsel.domain.InvoicePaymentRefundStatus;
import com.rbkmoney.damsel.domain.OperationFailure;
import com.rbkmoney.damsel.payment_processing.InvoiceChange;
import com.rbkmoney.damsel.payment_processing.InvoicePaymentChange;
import com.rbkmoney.damsel.payment_processing.InvoicePaymentRefundChange;
import com.rbkmoney.damsel.payment_processing.InvoicePaymentRefundStatusChanged;
import com.rbkmoney.geck.common.util.TBaseUtil;
import com.rbkmoney.geck.common.util.TypeUtil;
import com.rbkmoney.geck.serializer.kit.tbase.TErrorUtil;
import com.rbkmoney.machinegun.eventsink.MachineEvent;
import com.rbkmoney.reporter.domain.enums.FailureClass;
import com.rbkmoney.reporter.domain.enums.InvoiceEventType;
import com.rbkmoney.reporter.domain.enums.RefundStatus;
import com.rbkmoney.reporter.domain.tables.pojos.Refund;
import com.rbkmoney.reporter.mapper.InvoiceChangeMapper;
import com.rbkmoney.reporter.mapper.MapperResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class RefundStatusChangedChangeMapperImpl implements InvoiceChangeMapper {

    @Override
    public String[] getIgnoreProperties() {
        return new String[]{"id", "wtime", "current", "eventCreatedAt", "eventType", "sequenceId", "changeId",
                "refundStatus", "refundOperationFailureClass", "refundExternalFailure", "refundExternalFailureReason"};
    }

    @Override
    public boolean canMap(InvoiceChange payload) {
        return payload.isSetInvoicePaymentChange()
                && payload.getInvoicePaymentChange().getPayload().isSetInvoicePaymentRefundChange()
                && payload.getInvoicePaymentChange().getPayload().getInvoicePaymentRefundChange().getPayload().isSetInvoicePaymentRefundStatusChanged();
    }

    @Override
    public MapperResult map(InvoiceChange payload, MachineEvent baseEvent, Integer changeId) {
        InvoicePaymentChange invoicePaymentChange = payload.getInvoicePaymentChange();
        InvoicePaymentRefundChange invoicePaymentRefundChange = getInvoicePaymentRefundChange(invoicePaymentChange);
        InvoicePaymentRefundStatusChanged invoicePaymentRefundStatusChanged = getInvoicePaymentRefundStatusChanged(invoicePaymentRefundChange);
        InvoicePaymentRefundStatus invoicePaymentRefundStatus = invoicePaymentRefundStatusChanged.getStatus();
        RefundStatus refundStatus = TBaseUtil.unionFieldToEnum(invoicePaymentRefundStatus, RefundStatus.class);

        String refundId = invoicePaymentRefundChange.getId();
        String paymentId = invoicePaymentChange.getId();
        String invoiceId = baseEvent.getSourceId();

        Refund refund = new Refund();

        refund.setId(null);
        refund.setWtime(null);
        refund.setEventCreatedAt(TypeUtil.stringToLocalDateTime(baseEvent.getCreatedAt()));
        refund.setEventType(InvoiceEventType.INVOICE_PAYMENT_REFUND_STATUS_CHANGED);
        refund.setSequenceId(baseEvent.getEventId());
        refund.setChangeId(changeId);
        refund.setRefundStatus(refundStatus);
        if (invoicePaymentRefundStatus.isSetFailed()) {
            OperationFailure operationFailure = invoicePaymentRefundStatus.getFailed().getFailure();

            refund.setRefundOperationFailureClass(TBaseUtil.unionFieldToEnum(operationFailure, FailureClass.class));
            if (operationFailure.isSetFailure()) {
                Failure failure = operationFailure.getFailure();

                refund.setRefundExternalFailure(TErrorUtil.toStringVal(failure));
                refund.setRefundExternalFailureReason(failure.getReason());
            }
        }

        log.info("Refund with eventType=statusChanged and status {} has been mapped, invoiceId={}, paymentId={}, refundId={}", refundStatus, invoiceId, paymentId, refundId);

        return new MapperResult(refund);
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
