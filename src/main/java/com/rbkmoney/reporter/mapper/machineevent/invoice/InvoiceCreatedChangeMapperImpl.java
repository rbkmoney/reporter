package com.rbkmoney.reporter.mapper.machineevent.invoice;

import com.rbkmoney.damsel.base.Content;
import com.rbkmoney.damsel.domain.InvoiceDetails;
import com.rbkmoney.damsel.payment_processing.InvoiceChange;
import com.rbkmoney.geck.common.util.TBaseUtil;
import com.rbkmoney.geck.common.util.TypeUtil;
import com.rbkmoney.machinegun.eventsink.MachineEvent;
import com.rbkmoney.reporter.domain.enums.InvoiceEventType;
import com.rbkmoney.reporter.domain.enums.InvoiceStatus;
import com.rbkmoney.reporter.domain.tables.pojos.Invoice;
import com.rbkmoney.reporter.domain.tables.pojos.InvoiceState;
import com.rbkmoney.reporter.mapper.InvoiceChangeMapper;
import com.rbkmoney.reporter.mapper.MapperResult;
import com.rbkmoney.reporter.util.DamselUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@Slf4j
public class InvoiceCreatedChangeMapperImpl implements InvoiceChangeMapper {

    @Override
    public MapperResult map(InvoiceChange payload, MachineEvent baseEvent, Integer changeId) {
        com.rbkmoney.damsel.domain.Invoice damselInvoice = payload.getInvoiceCreated().getInvoice();
        InvoiceDetails details = damselInvoice.getDetails();
        com.rbkmoney.damsel.domain.InvoiceStatus invoiceStatus = damselInvoice.getStatus();

        String invoiceId = baseEvent.getSourceId();

        Invoice invoice = new Invoice();
        invoice.setEventCreatedAt(TypeUtil.stringToLocalDateTime(baseEvent.getCreatedAt()));
        invoice.setEventType(InvoiceEventType.INVOICE_CREATED);
        invoice.setInvoiceId(invoiceId);
        invoice.setSequenceId(baseEvent.getEventId());
        invoice.setChangeId(changeId);
        invoice.setPartyId(UUID.fromString(damselInvoice.getOwnerId()));
        if (damselInvoice.isSetPartyRevision()) {
            invoice.setInvoicePartyRevision(damselInvoice.getPartyRevision());
        }
        invoice.setPartyShopId(damselInvoice.getShopId());
        invoice.setInvoiceCreatedAt(TypeUtil.stringToLocalDateTime(damselInvoice.getCreatedAt()));
        invoice.setInvoiceDue(TypeUtil.stringToLocalDateTime(damselInvoice.getDue()));
        fillInvoiceDetails(details, invoice);
        fillCash(damselInvoice, invoice);
        fillInvoiceContext(damselInvoice, invoice);
        if (damselInvoice.isSetTemplateId()) {
            invoice.setInvoiceTemplateId(damselInvoice.getTemplateId());
        }

        InvoiceState state = new InvoiceState();
        state.setInvoiceId(invoiceId);
        state.setSequenceId(baseEvent.getEventId());
        state.setChangeId(changeId);
        state.setCreatedAt(TypeUtil.stringToLocalDateTime(baseEvent.getCreatedAt()));
        state.setStatus(TBaseUtil.unionFieldToEnum(invoiceStatus, InvoiceStatus.class));
        state.setStatusDetails(DamselUtil.getInvoiceStatusDetails(invoiceStatus));

        log.info("Invoice with eventType=created has been mapped, invoiceId={}", invoiceId);

        return new MapperResult(invoice, state);
    }

    @Override
    public boolean canMap(InvoiceChange payload) {
        return payload.isSetInvoiceCreated();
    }

    @Override
    public String[] getIgnoreProperties() {
        return new String[0];
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
