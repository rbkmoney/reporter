package com.rbkmoney.reporter.mapper.machineevent.refund;

import com.rbkmoney.damsel.domain.Cash;
import com.rbkmoney.damsel.domain.FinalCashFlowPosting;
import com.rbkmoney.damsel.domain.InvoicePaymentRefund;
import com.rbkmoney.damsel.payment_processing.InvoiceChange;
import com.rbkmoney.damsel.payment_processing.InvoicePaymentChange;
import com.rbkmoney.damsel.payment_processing.InvoicePaymentRefundChange;
import com.rbkmoney.damsel.payment_processing.InvoicePaymentRefundCreated;
import com.rbkmoney.geck.common.util.TBaseUtil;
import com.rbkmoney.geck.common.util.TypeUtil;
import com.rbkmoney.machinegun.eventsink.MachineEvent;
import com.rbkmoney.reporter.domain.enums.InvoiceEventType;
import com.rbkmoney.reporter.domain.enums.PaymentChangeType;
import com.rbkmoney.reporter.domain.enums.RefundStatus;
import com.rbkmoney.reporter.domain.tables.pojos.CashFlow;
import com.rbkmoney.reporter.domain.tables.pojos.Refund;
import com.rbkmoney.reporter.domain.tables.pojos.RefundState;
import com.rbkmoney.reporter.mapper.InvoiceChangeMapper;
import com.rbkmoney.reporter.mapper.MapperResult;
import com.rbkmoney.reporter.util.CashFlowUtil;
import com.rbkmoney.reporter.util.json.FinalCashFlowUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@Slf4j
public class RefundCreatedChangeMapperImpl implements InvoiceChangeMapper {

    @Override
    public String[] getIgnoreProperties() {
        return new String[0];
    }

    @Override
    public boolean canMap(InvoiceChange payload) {
        return payload.isSetInvoicePaymentChange()
                && payload.getInvoicePaymentChange().getPayload().isSetInvoicePaymentRefundChange()
                && payload.getInvoicePaymentChange().getPayload().getInvoicePaymentRefundChange().getPayload().isSetInvoicePaymentRefundCreated();
    }

    @Override
    public MapperResult map(InvoiceChange payload, MachineEvent baseEvent, Integer changeId) {
        InvoicePaymentChange invoicePaymentChange = payload.getInvoicePaymentChange();
        InvoicePaymentRefundChange invoicePaymentRefundChange = getInvoicePaymentRefundChange(invoicePaymentChange);
        InvoicePaymentRefundCreated invoicePaymentRefundCreated = getInvoicePaymentRefundCreated(invoicePaymentRefundChange);
        InvoicePaymentRefund invoicePaymentRefund = invoicePaymentRefundCreated.getRefund();

        String refundId = invoicePaymentRefundChange.getId();
        String paymentId = invoicePaymentChange.getId();
        String invoiceId = baseEvent.getSourceId();

        Refund refund = new Refund();

        refund.setEventCreatedAt(TypeUtil.stringToLocalDateTime(baseEvent.getCreatedAt()));
        refund.setEventType(InvoiceEventType.INVOICE_PAYMENT_REFUND_CREATED);
        refund.setInvoiceId(invoiceId);
        refund.setSequenceId(baseEvent.getEventId());
        refund.setChangeId(changeId);
        refund.setPaymentId(paymentId);
        refund.setRefundId(refundId);
        refund.setRefundCreatedAt(getRefundCreatedAt(invoicePaymentRefund));
        refund.setRefundDomainRevision(invoicePaymentRefund.getDomainRevision());
        if (invoicePaymentRefund.isSetPartyRevision()) {
            refund.setRefundPartyRevision(invoicePaymentRefund.getPartyRevision());
        }
        if (invoicePaymentRefund.isSetCash()) {
            Cash cash = invoicePaymentRefund.getCash();

            refund.setRefundAmount(cash.getAmount());
            refund.setRefundCurrencyCode(cash.getCurrency().getSymbolicCode());
        }
        refund.setRefundReason(invoicePaymentRefund.getReason());

        RefundState refundState = new RefundState();
        refundState.setInvoiceId(invoiceId);
        refundState.setSequenceId(baseEvent.getEventId());
        refundState.setChangeId(changeId);
        refundState.setPaymentId(paymentId);
        refundState.setRefundId(refundId);
        refundState.setCreatedAt(TypeUtil.stringToLocalDateTime(baseEvent.getCreatedAt()));
        refundState.setRefundStatus(getRefundStatus(invoicePaymentRefund));

        List<CashFlow> cashFlowList = getCashFlowList(baseEvent, changeId, paymentId, refundId, invoicePaymentRefundCreated.getCashFlow());

        log.info("Refund with eventType=created has been mapped, invoiceId={}, paymentId={}, refundId={}", invoiceId, paymentId, refundId);

        return new MapperResult(refund, refundState, cashFlowList);
    }

    private List<CashFlow> getCashFlowList(MachineEvent baseEvent,
                                           Integer changeId,
                                           String paymentId,
                                           String refundId,
                                           List<FinalCashFlowPosting> cashFlow) {
        return CashFlowUtil.convertRefundCashFlows(
                cashFlow,
                baseEvent.getSourceId(),
                baseEvent.getEventId(),
                changeId,
                paymentId,
                refundId,
                TypeUtil.stringToLocalDateTime(baseEvent.getCreatedAt()),
                PaymentChangeType.refund
        );
    }

    private LocalDateTime getRefundCreatedAt(InvoicePaymentRefund invoicePaymentRefund) {
        return TypeUtil.stringToLocalDateTime(invoicePaymentRefund.getCreatedAt());
    }

    private RefundStatus getRefundStatus(InvoicePaymentRefund invoicePaymentRefund) {
        return TBaseUtil.unionFieldToEnum(invoicePaymentRefund.getStatus(), RefundStatus.class);
    }

    private InvoicePaymentRefundCreated getInvoicePaymentRefundCreated(InvoicePaymentRefundChange invoicePaymentRefundChange) {
        return invoicePaymentRefundChange
                .getPayload().getInvoicePaymentRefundCreated();
    }

    private InvoicePaymentRefundChange getInvoicePaymentRefundChange(InvoicePaymentChange invoicePaymentChange) {
        return invoicePaymentChange
                .getPayload().getInvoicePaymentRefundChange();
    }
}
