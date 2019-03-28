package com.rbkmoney.reporter.handle.stockevent.event.change.impl;

import com.rbkmoney.damsel.event_stock.StockEvent;
import com.rbkmoney.damsel.payment_processing.Event;
import com.rbkmoney.damsel.payment_processing.InvoiceChange;
import com.rbkmoney.damsel.payment_processing.InvoiceStatusChanged;
import com.rbkmoney.geck.common.util.TBaseUtil;
import com.rbkmoney.geck.common.util.TypeUtil;
import com.rbkmoney.reporter.domain.enums.InvoiceEventType;
import com.rbkmoney.reporter.domain.enums.InvoiceStatus;
import com.rbkmoney.reporter.domain.tables.pojos.Invoice;
import com.rbkmoney.reporter.handle.stockevent.event.change.InvoiceChangeEventsHandler;
import com.rbkmoney.reporter.service.InvoiceService;
import com.rbkmoney.reporter.util.DamselUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class InvoiceStatusChangedChangeEventHandler implements InvoiceChangeEventsHandler {

    private final InvoiceService invoiceService;

    @Override
    public boolean accept(InvoiceChange change) {
        return change.isSetInvoiceStatusChanged();
    }

    @Override
    public void handle(InvoiceChange change, StockEvent stockEvent) {
        Event event = stockEvent.getSourceEvent().getProcessingEvent();

        InvoiceStatusChanged invoiceStatusChanged = change.getInvoiceStatusChanged();
        com.rbkmoney.damsel.domain.InvoiceStatus invoiceStatus = invoiceStatusChanged.getStatus();

        String invoiceId = event.getSource().getInvoiceId();

        log.info("Start invoice status changed handling, invoiceId={}", invoiceId);

        Invoice invoice = invoiceService.get(invoiceId);

        invoice.setEventId(event.getId());
        invoice.setEventCreatedAt(TypeUtil.stringToLocalDateTime(event.getCreatedAt()));
        invoice.setEventType(InvoiceEventType.INVOICE_STATUS_CHANGED);
        invoice.setInvoiceStatus(TBaseUtil.unionFieldToEnum(invoiceStatus, InvoiceStatus.class));
        invoice.setInvoiceStatusDetails(DamselUtil.getInvoiceStatusDetails(invoiceStatus));

        invoiceService.updateNotCurrent(invoiceId);
        invoiceService.save(invoice);
        log.info("Invoice status has been changed, invoiceId={}", invoiceId);
    }
}
