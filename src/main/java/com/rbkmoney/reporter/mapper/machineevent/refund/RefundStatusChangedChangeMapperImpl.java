package com.rbkmoney.reporter.mapper.machineevent.refund;

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
import com.rbkmoney.reporter.domain.enums.RefundStatus;
import com.rbkmoney.reporter.domain.tables.pojos.RefundState;
import com.rbkmoney.reporter.mapper.InvoiceChangeMapper;
import com.rbkmoney.reporter.mapper.MapperResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@Slf4j
public class RefundStatusChangedChangeMapperImpl implements InvoiceChangeMapper {

    @Override
    public boolean canMap(InvoiceChange payload) {
        return payload.isSetInvoicePaymentChange()
                && payload.getInvoicePaymentChange().getPayload().isSetInvoicePaymentRefundChange()
                && payload.getInvoicePaymentChange().getPayload().getInvoicePaymentRefundChange().getPayload().isSetInvoicePaymentRefundStatusChanged();
    }

    @Override
    public MapperResult map(InvoiceChange payload, MachineEvent baseEvent, Integer changeId) {
        InvoicePaymentChange damselPaymentChange = payload.getInvoicePaymentChange();
        InvoicePaymentRefundChange damselRefundChange = getInvoicePaymentRefundChange(damselPaymentChange);
        InvoicePaymentRefundStatusChanged damselRefundStatusChange = getInvoicePaymentRefundStatusChanged(damselRefundChange);
        InvoicePaymentRefundStatus damselRefundStatus = damselRefundStatusChange.getStatus();

        String invoiceId = baseEvent.getSourceId();
        long sequenceId = baseEvent.getEventId();
        LocalDateTime eventCreatedAt = TypeUtil.stringToLocalDateTime(baseEvent.getCreatedAt());
        String paymentId = damselPaymentChange.getId();
        String refundId = damselRefundChange.getId();
        RefundStatus refundStatus = TBaseUtil.unionFieldToEnum(damselRefundStatus, RefundStatus.class);

        RefundState refundState = getRefundState(damselRefundStatus, invoiceId, sequenceId, changeId, eventCreatedAt, paymentId, refundId, refundStatus);

        log.info("Refund with eventType=[statusChanged] and status {} has been mapped, invoiceId={}, paymentId={}, refundId={}", refundStatus, invoiceId, paymentId, refundId);

        return new MapperResult(refundState);
    }

    private RefundState getRefundState(InvoicePaymentRefundStatus damselRefundStatus, String invoiceId, long sequenceId, Integer changeId, LocalDateTime eventCreatedAt, String paymentId, String refundId, RefundStatus refundStatus) {
        RefundState refundState = new RefundState();
        refundState.setInvoiceId(invoiceId);
        refundState.setSequenceId(sequenceId);
        refundState.setChangeId(changeId);
        refundState.setEventCreatedAt(eventCreatedAt);
        refundState.setPaymentId(paymentId);
        refundState.setRefundId(refundId);
        refundState.setStatus(refundStatus);

        if (damselRefundStatus.isSetFailed()) {
            OperationFailure operationFailure = damselRefundStatus.getFailed().getFailure();
            refundState.setOperationFailureClass(TBaseUtil.unionFieldToEnum(operationFailure, FailureClass.class));

            if (operationFailure.isSetFailure()) {
                Failure failure = operationFailure.getFailure();
                refundState.setExternalFailure(TErrorUtil.toStringVal(failure));
                refundState.setExternalFailureReason(failure.getReason());
            }
        }

        return refundState;
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
