package com.rbkmoney.reporter.handle.stockevent.event.change.impl;

import com.rbkmoney.damsel.domain.FinalCashFlowPosting;
import com.rbkmoney.damsel.event_stock.StockEvent;
import com.rbkmoney.damsel.payment_processing.Event;
import com.rbkmoney.damsel.payment_processing.InvoiceChange;
import com.rbkmoney.damsel.payment_processing.InvoicePaymentCashFlowChanged;
import com.rbkmoney.damsel.payment_processing.InvoicePaymentChange;
import com.rbkmoney.geck.common.util.TypeUtil;
import com.rbkmoney.reporter.domain.enums.InvoiceEventType;
import com.rbkmoney.reporter.domain.tables.pojos.Payment;
import com.rbkmoney.reporter.handle.stockevent.event.change.InvoiceChangeEventsHandler;
import com.rbkmoney.reporter.service.PaymentService;
import com.rbkmoney.reporter.util.CashFlowUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class PaymentCashFlowChangedChangeEventHandler implements InvoiceChangeEventsHandler {

    private final PaymentService paymentService;

    @Override
    public boolean accept(InvoiceChange change) {
        return change.isSetInvoicePaymentChange()
                && change.getInvoicePaymentChange().getPayload().isSetInvoicePaymentCashFlowChanged();
    }

    @Override
    public void handle(InvoiceChange change, StockEvent stockEvent) {
        Event event = stockEvent.getSourceEvent().getProcessingEvent();

        InvoicePaymentChange invoicePaymentChange = change.getInvoicePaymentChange();
        InvoicePaymentCashFlowChanged invoicePaymentCashFlowChanged = getInvoicePaymentCashFlowChanged(invoicePaymentChange);
        List<FinalCashFlowPosting> finalCashFlowPostings = invoicePaymentCashFlowChanged.getCashFlow();

        String paymentId = invoicePaymentChange.getId();
        String invoiceId = event.getSource().getInvoiceId();

        log.info("Start invoice payment cash flow changed handling, paymentId={}, invoiceId={}", paymentId, invoiceId);

        Payment payment = paymentService.get(invoiceId, paymentId);

        payment.setEventId(event.getId());
        payment.setEventCreatedAt(TypeUtil.stringToLocalDateTime(event.getCreatedAt()));
        payment.setEventType(InvoiceEventType.INVOICE_PAYMENT_CASH_FLOW_CHANGED);
        payment.setSequenceId(event.getSequence());
        payment.setPaymentCashFlow(CashFlowUtil.toDtoFinalCashFlow(finalCashFlowPostings));

        paymentService.updateNotCurrent(invoiceId, paymentId);
        paymentService.save(payment);
        log.info("Invoice payment cash flow has been changed, paymentId={}, invoiceId={}", paymentId, invoiceId);
    }

    private InvoicePaymentCashFlowChanged getInvoicePaymentCashFlowChanged(InvoicePaymentChange invoicePaymentChange) {
        return invoicePaymentChange
                .getPayload().getInvoicePaymentCashFlowChanged();
    }
}
