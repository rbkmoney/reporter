package com.rbkmoney.reporter.mapper.machineevent.adjustment;

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
import com.rbkmoney.reporter.domain.tables.pojos.AdjustmentState;
import com.rbkmoney.reporter.mapper.InvoiceChangeMapper;
import com.rbkmoney.reporter.mapper.MapperResult;
import com.rbkmoney.reporter.util.DamselUtil;
import com.rbkmoney.reporter.util.json.FinalCashFlowUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@Slf4j
public class AdjustmentCreatedChangeMapperImpl implements InvoiceChangeMapper {

    @Override
    public MapperResult map(InvoiceChange payload, MachineEvent baseEvent, Integer changeId) {
        InvoicePaymentChange invoicePaymentChange = payload.getInvoicePaymentChange();
        InvoicePaymentAdjustmentChange invoicePaymentAdjustmentChange = getInvoicePaymentAdjustmentChange(invoicePaymentChange);
        InvoicePaymentAdjustment invoicePaymentAdjustment = getInvoicePaymentAdjustment(invoicePaymentAdjustmentChange);

        String adjustmentId = invoicePaymentAdjustmentChange.getId();
        String paymentId = invoicePaymentChange.getId();
        String invoiceId = baseEvent.getSourceId();

        Adjustment adjustment = new Adjustment();

        adjustment.setEventCreatedAt(TypeUtil.stringToLocalDateTime(baseEvent.getCreatedAt()));
        adjustment.setEventType(InvoiceEventType.INVOICE_PAYMENT_ADJUSTMENT_CREATED);
        adjustment.setInvoiceId(invoiceId);
        adjustment.setSequenceId(baseEvent.getEventId());
        adjustment.setChangeId(changeId);
        adjustment.setPaymentId(paymentId);
        adjustment.setAdjustmentId(adjustmentId);
        adjustment.setAdjustmentCreatedAt(getAdjustmentCreatedAt(invoicePaymentAdjustment));
        adjustment.setAdjustmentDomainRevision(invoicePaymentAdjustment.getDomainRevision());
        adjustment.setAdjustmentReason(invoicePaymentAdjustment.getReason());
        adjustment.setAdjustmentCashFlow(FinalCashFlowUtil.toDtoFinalCashFlow(invoicePaymentAdjustment.getNewCashFlow()));
        adjustment.setAdjustmentCashFlowInverseOld(FinalCashFlowUtil.toDtoFinalCashFlow(invoicePaymentAdjustment.getOldCashFlowInverse()));
        if (invoicePaymentAdjustment.isSetPartyRevision()) {
            adjustment.setAdjustmentPartyRevision(invoicePaymentAdjustment.getPartyRevision());
        }

        AdjustmentState adjustmentState = new AdjustmentState();
        adjustmentState.setInvoiceId(invoiceId);
        adjustmentState.setSequenceId(baseEvent.getEventId());
        adjustmentState.setChangeId(changeId);
        adjustmentState.setCreatedAt(DamselUtil.getAdjustmentStatusCreatedAt(invoicePaymentAdjustment.getStatus()));
        adjustmentState.setStatus(TBaseUtil.unionFieldToEnum(invoicePaymentAdjustment.getStatus(), AdjustmentStatus.class));

        log.info("Adjustment with eventType=created has been mapped, invoiceId={}, paymentId={}, adjustmentId={}", invoiceId, paymentId, adjustmentId);

        return new MapperResult(adjustment, adjustmentState);
    }

    @Override
    public boolean canMap(InvoiceChange payload) {
        return payload.isSetInvoicePaymentChange()
                && payload.getInvoicePaymentChange().getPayload().isSetInvoicePaymentAdjustmentChange()
                && payload.getInvoicePaymentChange().getPayload().getInvoicePaymentAdjustmentChange().getPayload().isSetInvoicePaymentAdjustmentCreated();
    }

    @Override
    public String[] getIgnoreProperties() {
        return new String[0];
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
