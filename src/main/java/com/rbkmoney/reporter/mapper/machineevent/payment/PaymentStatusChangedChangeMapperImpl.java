package com.rbkmoney.reporter.mapper.machineevent.payment;

import com.rbkmoney.damsel.domain.Cash;
import com.rbkmoney.damsel.domain.Failure;
import com.rbkmoney.damsel.domain.InvoicePaymentCaptured;
import com.rbkmoney.damsel.domain.OperationFailure;
import com.rbkmoney.damsel.payment_processing.InvoiceChange;
import com.rbkmoney.damsel.payment_processing.InvoicePaymentChange;
import com.rbkmoney.damsel.payment_processing.InvoicePaymentStatusChanged;
import com.rbkmoney.geck.common.util.TBaseUtil;
import com.rbkmoney.geck.common.util.TypeUtil;
import com.rbkmoney.geck.serializer.kit.tbase.TErrorUtil;
import com.rbkmoney.machinegun.eventsink.MachineEvent;
import com.rbkmoney.reporter.domain.enums.FailureClass;
import com.rbkmoney.reporter.domain.enums.InvoicePaymentStatus;
import com.rbkmoney.reporter.domain.tables.pojos.PaymentCost;
import com.rbkmoney.reporter.domain.tables.pojos.PaymentState;
import com.rbkmoney.reporter.mapper.InvoiceChangeMapper;
import com.rbkmoney.reporter.mapper.MapperResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

import static com.rbkmoney.reporter.util.MapperUtil.getPaymentCost;
import static com.rbkmoney.reporter.util.MapperUtil.getPaymentState;

@Component
@Slf4j
public class PaymentStatusChangedChangeMapperImpl implements InvoiceChangeMapper {

    @Override
    public boolean canMap(InvoiceChange payload) {
        return payload.isSetInvoicePaymentChange()
                && payload.getInvoicePaymentChange().getPayload().isSetInvoicePaymentStatusChanged();
    }

    @Override
    public MapperResult map(InvoiceChange payload, MachineEvent baseEvent, Integer changeId) {
        InvoicePaymentChange damselPaymentChange = payload.getInvoicePaymentChange();
        InvoicePaymentStatusChanged damselPaymentStatusChanged = getInvoicePaymentStatusChanged(damselPaymentChange);
        var status = damselPaymentStatusChanged.getStatus();

        String invoiceId = baseEvent.getSourceId();
        long sequenceId = baseEvent.getEventId();
        LocalDateTime eventCreatedAt = TypeUtil.stringToLocalDateTime(baseEvent.getCreatedAt());
        String paymentId = damselPaymentChange.getId();

        PaymentState paymentState = getPaymentState(invoiceId, changeId, sequenceId, eventCreatedAt, paymentId, status);

        PaymentCost paymentCost = null;
        if (status.isSetCaptured()) {
            InvoicePaymentCaptured invoicePaymentCaptured = status.getCaptured();
            if (invoicePaymentCaptured.isSetCost()) {
                Cash cost = invoicePaymentCaptured.getCost();

                paymentCost = getPaymentCost(invoiceId, sequenceId, changeId, eventCreatedAt, paymentId, cost.getAmount(), cost.getCurrency().getSymbolicCode());
            }
        } else if (status.isSetFailed()) {
            OperationFailure operationFailure = status.getFailed().getFailure();
            paymentState.setOperationFailureClass(TBaseUtil.unionFieldToEnum(operationFailure, FailureClass.class));
            if (operationFailure.isSetFailure()) {
                Failure failure = operationFailure.getFailure();
                paymentState.setExternalFailure(TErrorUtil.toStringVal(failure));
                paymentState.setExternalFailureReason(failure.getReason());
            }
        }

        log.info("Payment with eventType=[statusChanged] and status {} has been mapped, invoiceId={}, paymentId={}", TBaseUtil.unionFieldToEnum(status, InvoicePaymentStatus.class), invoiceId, paymentId);

        return new MapperResult(paymentState, paymentCost);
    }

    private InvoicePaymentStatusChanged getInvoicePaymentStatusChanged(InvoicePaymentChange invoicePaymentChange) {
        return invoicePaymentChange.getPayload().getInvoicePaymentStatusChanged();
    }
}
