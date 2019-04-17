package com.rbkmoney.reporter.handle.stockevent.event.change.impl;

import com.rbkmoney.damsel.base.Content;
import com.rbkmoney.damsel.domain.InvoiceDetails;
import com.rbkmoney.damsel.event_stock.StockEvent;
import com.rbkmoney.damsel.payment_processing.Event;
import com.rbkmoney.damsel.payment_processing.InvoiceChange;
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

import java.util.UUID;

@Component
@Slf4j
@RequiredArgsConstructor
public class InvoiceCreatedChangeEventHandler implements InvoiceChangeEventsHandler {

    private final InvoiceService invoiceService;

    @Override
    public boolean accept(InvoiceChange change) {
        return change.isSetInvoiceCreated();
    }

    @Override
    public void handle(InvoiceChange change, StockEvent stockEvent) {
        Event event = stockEvent.getSourceEvent().getProcessingEvent();

        com.rbkmoney.damsel.domain.Invoice damselInvoice = change.getInvoiceCreated().getInvoice();
        com.rbkmoney.damsel.domain.InvoiceDetails details = damselInvoice.getDetails();
        com.rbkmoney.damsel.domain.InvoiceStatus invoiceStatus = damselInvoice.getStatus();

        String invoiceId = event.getSource().getInvoiceId();

        log.info("Start invoice created handling, invoiceId={}", invoiceId);

        Invoice invoice = new Invoice();
        invoice.setEventId(event.getId());
        invoice.setEventCreatedAt(TypeUtil.stringToLocalDateTime(event.getCreatedAt()));
        invoice.setEventType(InvoiceEventType.INVOICE_CREATED);
        invoice.setSequenceId(event.getSequence());
        invoice.setInvoiceId(invoiceId);
        invoice.setPartyId(UUID.fromString(damselInvoice.getOwnerId()));
        if (damselInvoice.isSetPartyRevision()) {
            invoice.setInvoicePartyRevision(damselInvoice.getPartyRevision());
        }
        invoice.setPartyShopId(damselInvoice.getShopId());
        invoice.setInvoiceCreatedAt(TypeUtil.stringToLocalDateTime(damselInvoice.getCreatedAt()));
        fillInvoiceStatus(invoiceStatus, invoice);
        fillInvoiceDetails(details, invoice);
        invoice.setInvoiceDue(TypeUtil.stringToLocalDateTime(damselInvoice.getDue()));
        fillCash(damselInvoice, invoice);
        fillInvoiceContext(damselInvoice, invoice);
        if (damselInvoice.isSetTemplateId()) {
            invoice.setInvoiceTemplateId(damselInvoice.getTemplateId());
        }

        invoiceService.save(invoice);
        log.info("Invoice has been created, invoiceId={}", invoiceId);
    }

    private void fillInvoiceStatus(com.rbkmoney.damsel.domain.InvoiceStatus invoiceStatus, Invoice invoice) {
        invoice.setInvoiceStatus(TBaseUtil.unionFieldToEnum(invoiceStatus, InvoiceStatus.class));
        invoice.setInvoiceStatusDetails(DamselUtil.getInvoiceStatusDetails(invoiceStatus));
    }

    private void fillInvoiceDetails(InvoiceDetails details, Invoice invoice) {
        invoice.setInvoiceProduct(details.getProduct());
        invoice.setInvoiceDescription(details.getDescription());
        if (details.isSetCart()) {
            invoice.setInvoiceCartJson(DamselUtil.toJsonString(details.getCart()));
        }
    }

    private void fillCash(com.rbkmoney.damsel.domain.Invoice damselInvoice, Invoice invoice) {
        invoice.setInvoiceAmount(damselInvoice.getCost().getAmount());
        invoice.setInvoiceCurrencyCode(damselInvoice.getCost().getCurrency().getSymbolicCode());
    }

    private void fillInvoiceContext(com.rbkmoney.damsel.domain.Invoice damselInvoice, Invoice invoice) {
        if (damselInvoice.isSetContext()) {
            Content content = damselInvoice.getContext();

            invoice.setInvoiceContextType(content.getType());
            invoice.setInvoiceContext(content.getData());
        }
    }
}
