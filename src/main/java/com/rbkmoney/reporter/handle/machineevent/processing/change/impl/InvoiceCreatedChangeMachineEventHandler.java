package com.rbkmoney.reporter.handle.machineevent.processing.change.impl;

import com.rbkmoney.damsel.base.Content;
import com.rbkmoney.damsel.domain.InvoiceDetails;
import com.rbkmoney.damsel.payment_processing.InvoiceChange;
import com.rbkmoney.geck.common.util.TBaseUtil;
import com.rbkmoney.geck.common.util.TypeUtil;
import com.rbkmoney.machinegun.eventsink.MachineEvent;
import com.rbkmoney.reporter.domain.enums.InvoiceEventType;
import com.rbkmoney.reporter.domain.enums.InvoiceStatus;
import com.rbkmoney.reporter.domain.tables.pojos.Invoice;
import com.rbkmoney.reporter.handle.machineevent.processing.change.InvoiceChangeMachineEventHandler;
import com.rbkmoney.reporter.service.InvoiceService;
import com.rbkmoney.reporter.util.DamselUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@Slf4j
@RequiredArgsConstructor
public class InvoiceCreatedChangeMachineEventHandler implements InvoiceChangeMachineEventHandler {

    private final InvoiceService invoiceService;

    @Override
    public boolean accept(InvoiceChange payload) {
        return payload.isSetInvoiceCreated();
    }

    @Override
    public void handle(InvoiceChange payload, MachineEvent baseEvent) {
        com.rbkmoney.damsel.domain.Invoice damselInvoice = payload.getInvoiceCreated().getInvoice();
        InvoiceDetails details = damselInvoice.getDetails();
        com.rbkmoney.damsel.domain.InvoiceStatus invoiceStatus = damselInvoice.getStatus();

        String invoiceId = baseEvent.getSourceId();

        log.info("Start invoice created handling, invoiceId={}", invoiceId);

        Invoice invoice = new Invoice();
        invoice.setEventCreatedAt(TypeUtil.stringToLocalDateTime(baseEvent.getCreatedAt()));
        invoice.setEventType(InvoiceEventType.INVOICE_CREATED);
        invoice.setSequenceId(baseEvent.getEventId());
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
