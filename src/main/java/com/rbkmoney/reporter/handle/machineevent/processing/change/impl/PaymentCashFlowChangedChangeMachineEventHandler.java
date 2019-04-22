package com.rbkmoney.reporter.handle.machineevent.processing.change.impl;

import com.rbkmoney.damsel.domain.FinalCashFlowPosting;
import com.rbkmoney.damsel.payment_processing.InvoiceChange;
import com.rbkmoney.damsel.payment_processing.InvoicePaymentCashFlowChanged;
import com.rbkmoney.damsel.payment_processing.InvoicePaymentChange;
import com.rbkmoney.geck.common.util.TypeUtil;
import com.rbkmoney.machinegun.eventsink.MachineEvent;
import com.rbkmoney.reporter.domain.enums.InvoiceEventType;
import com.rbkmoney.reporter.domain.tables.pojos.Payment;
import com.rbkmoney.reporter.handle.machineevent.processing.change.InvoiceChangeMachineEventHandler;
import com.rbkmoney.reporter.service.PaymentService;
import com.rbkmoney.reporter.util.json.FinalCashFlowUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class PaymentCashFlowChangedChangeMachineEventHandler implements InvoiceChangeMachineEventHandler {

    private final PaymentService paymentService;

    @Override
    public boolean accept(InvoiceChange payload) {
        return payload.isSetInvoicePaymentChange()
                && payload.getInvoicePaymentChange().getPayload().isSetInvoicePaymentCashFlowChanged();
    }

    @Override
    public void handle(InvoiceChange payload, MachineEvent baseEvent) {
        InvoicePaymentChange invoicePaymentChange = payload.getInvoicePaymentChange();
        InvoicePaymentCashFlowChanged invoicePaymentCashFlowChanged = getInvoicePaymentCashFlowChanged(invoicePaymentChange);
        List<FinalCashFlowPosting> finalCashFlowPostings = invoicePaymentCashFlowChanged.getCashFlow();

        String paymentId = invoicePaymentChange.getId();
        String invoiceId = baseEvent.getSourceId();

        log.info("Start invoice payment cash flow changed handling, paymentId={}, invoiceId={}", paymentId, invoiceId);

        Payment payment = paymentService.get(invoiceId, paymentId);

        payment.setId(null);
        payment.setWtime(null);
        payment.setEventCreatedAt(TypeUtil.stringToLocalDateTime(baseEvent.getCreatedAt()));
        payment.setEventType(InvoiceEventType.INVOICE_PAYMENT_CASH_FLOW_CHANGED);
        payment.setSequenceId(baseEvent.getEventId());
        payment.setPaymentCashFlow(FinalCashFlowUtil.toDtoFinalCashFlow(finalCashFlowPostings));

        paymentService.updateNotCurrent(invoiceId, paymentId);
        paymentService.save(payment);
        log.info("Invoice payment cash flow has been changed, paymentId={}, invoiceId={}", paymentId, invoiceId);
    }

    private InvoicePaymentCashFlowChanged getInvoicePaymentCashFlowChanged(InvoicePaymentChange invoicePaymentChange) {
        return invoicePaymentChange
                .getPayload().getInvoicePaymentCashFlowChanged();
    }
}
