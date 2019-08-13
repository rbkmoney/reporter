package com.rbkmoney.reporter.mapper.machineevent;

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
import com.rbkmoney.reporter.domain.enums.InvoiceEventType;
import com.rbkmoney.reporter.domain.enums.InvoicePaymentStatus;
import com.rbkmoney.reporter.domain.tables.pojos.Payment;
import com.rbkmoney.reporter.mapper.InvoiceChangeMapper;
import com.rbkmoney.reporter.mapper.MapperResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class PaymentStatusChangedChangeMapperImpl implements InvoiceChangeMapper {

    @Override
    public String[] getIgnoreProperties() {
        return new String[]{"id", "wtime", "current", "eventCreatedAt", "eventType", "sequenceId", "changeId",
                "paymentAmount", "paymentCurrencyCode", "paymentStatus", "paymentOperationFailureClass", "paymentExternalFailure",
                "paymentExternalFailureReason"};
    }

    @Override
    public boolean canMap(InvoiceChange payload) {
        return payload.isSetInvoicePaymentChange()
                && payload.getInvoicePaymentChange().getPayload().isSetInvoicePaymentStatusChanged();
    }

    @Override
    public MapperResult map(InvoiceChange payload, MachineEvent baseEvent, Integer changeId) {
        InvoicePaymentChange invoicePaymentChange = payload.getInvoicePaymentChange();
        InvoicePaymentStatusChanged invoicePaymentStatusChanged = getInvoicePaymentStatusChanged(invoicePaymentChange);
        com.rbkmoney.damsel.domain.InvoicePaymentStatus invoicePaymentStatusChangedStatus = invoicePaymentStatusChanged.getStatus();

        String paymentId = invoicePaymentChange.getId();
        String invoiceId = baseEvent.getSourceId();

        Payment payment = new Payment();

        payment.setId(null);
        payment.setWtime(null);
        payment.setEventCreatedAt(TypeUtil.stringToLocalDateTime(baseEvent.getCreatedAt()));
        payment.setEventType(InvoiceEventType.INVOICE_PAYMENT_STATUS_CHANGED);
        payment.setSequenceId(baseEvent.getEventId());
        payment.setChangeId(changeId);
        fillPaymentStatus(invoicePaymentStatusChangedStatus, payment);

        log.info("Payment with eventType=statusChanged and status {} has been mapped, invoiceId={}, paymentId={}", TBaseUtil.unionFieldToEnum(invoicePaymentStatusChangedStatus, InvoicePaymentStatus.class), invoiceId, paymentId);

        return new MapperResult(payment);
    }

    private void fillPaymentStatus(com.rbkmoney.damsel.domain.InvoicePaymentStatus invoicePaymentStatusChangedStatus, Payment payment) {
        payment.setPaymentStatus(TBaseUtil.unionFieldToEnum(invoicePaymentStatusChangedStatus, InvoicePaymentStatus.class));
        if (invoicePaymentStatusChangedStatus.isSetCaptured()) {
            InvoicePaymentCaptured invoicePaymentCaptured = invoicePaymentStatusChangedStatus.getCaptured();
            if (invoicePaymentCaptured.isSetCost()) {
                Cash cost = invoicePaymentCaptured.getCost();

                payment.setPaymentAmount(cost.getAmount());
                payment.setPaymentCurrencyCode(cost.getCurrency().getSymbolicCode());
            }
        } else if (invoicePaymentStatusChangedStatus.isSetFailed()) {
            OperationFailure operationFailure = invoicePaymentStatusChangedStatus.getFailed().getFailure();

            payment.setPaymentOperationFailureClass(TBaseUtil.unionFieldToEnum(operationFailure, FailureClass.class));
            if (operationFailure.isSetFailure()) {
                Failure failure = operationFailure.getFailure();

                payment.setPaymentExternalFailure(TErrorUtil.toStringVal(failure));
                payment.setPaymentExternalFailureReason(failure.getReason());
            }
        }
    }

    private InvoicePaymentStatusChanged getInvoicePaymentStatusChanged(InvoicePaymentChange invoicePaymentChange) {
        return invoicePaymentChange.getPayload().getInvoicePaymentStatusChanged();
    }
}
