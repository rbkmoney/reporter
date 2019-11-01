package com.rbkmoney.reporter.mapper.machineevent.invoice;

import com.rbkmoney.damsel.base.Content;
import com.rbkmoney.damsel.domain.InvoiceDetails;
import com.rbkmoney.damsel.payment_processing.InvoiceChange;
import com.rbkmoney.geck.common.util.TBaseUtil;
import com.rbkmoney.geck.common.util.TypeUtil;
import com.rbkmoney.machinegun.eventsink.MachineEvent;
import com.rbkmoney.reporter.domain.enums.InvoiceStatus;
import com.rbkmoney.reporter.domain.tables.pojos.Invoice;
import com.rbkmoney.reporter.domain.tables.pojos.InvoiceState;
import com.rbkmoney.reporter.mapper.InvoiceChangeMapper;
import com.rbkmoney.reporter.mapper.MapperResult;
import com.rbkmoney.reporter.util.DamselUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

@Component
@Slf4j
public class InvoiceCreatedChangeMapperImpl implements InvoiceChangeMapper {

    @Override
    public boolean canMap(InvoiceChange payload) {
        return payload.isSetInvoiceCreated();
    }

    @Override
    public MapperResult map(InvoiceChange payload, MachineEvent baseEvent, Integer changeId) {
        var damselInvoice = payload.getInvoiceCreated().getInvoice();
        InvoiceDetails details = damselInvoice.getDetails();
        var invoiceStatus = damselInvoice.getStatus();

        String invoiceId = baseEvent.getSourceId();
        long sequenceId = baseEvent.getEventId();
        LocalDateTime eventCreatedAt = TypeUtil.stringToLocalDateTime(baseEvent.getCreatedAt());

        Invoice invoice = getInvoice(damselInvoice, invoiceId, details);

        InvoiceState state = getInvoiceState(invoiceStatus, invoiceId, sequenceId, changeId, eventCreatedAt);

        log.info("Invoice with eventType=[created] has been mapped, invoiceId={}", invoiceId);

        return new MapperResult(invoice, state);
    }

    private Invoice getInvoice(com.rbkmoney.damsel.domain.Invoice damselInvoice, String invoiceId, InvoiceDetails details) {
        Invoice invoice = new Invoice();
        invoice.setPartyId(UUID.fromString(damselInvoice.getOwnerId()));
        invoice.setPartyShopId(damselInvoice.getShopId());
        invoice.setInvoiceId(invoiceId);
        if (damselInvoice.isSetPartyRevision()) {
            invoice.setPartyRevision(damselInvoice.getPartyRevision());
        }
        invoice.setCreatedAt(TypeUtil.stringToLocalDateTime(damselInvoice.getCreatedAt()));
        fillInvoiceDetails(details, invoice);
        invoice.setDue(TypeUtil.stringToLocalDateTime(damselInvoice.getDue()));
        fillCash(damselInvoice, invoice);
        fillInvoiceContext(damselInvoice, invoice);
        if (damselInvoice.isSetTemplateId()) {
            invoice.setTemplateId(damselInvoice.getTemplateId());
        }

        return invoice;
    }

    private InvoiceState getInvoiceState(com.rbkmoney.damsel.domain.InvoiceStatus invoiceStatus, String invoiceId, long sequenceId, Integer changeId, LocalDateTime eventCreatedAt) {
        InvoiceState invoiceState = new InvoiceState();
        invoiceState.setInvoiceId(invoiceId);
        invoiceState.setSequenceId(sequenceId);
        invoiceState.setChangeId(changeId);
        invoiceState.setEventCreatedAt(eventCreatedAt);
        invoiceState.setStatus(TBaseUtil.unionFieldToEnum(invoiceStatus, InvoiceStatus.class));
        invoiceState.setStatusDetails(DamselUtil.getInvoiceStatusDetails(invoiceStatus));

        return invoiceState;
    }

    private void fillInvoiceDetails(InvoiceDetails details, Invoice invoice) {
        invoice.setProduct(details.getProduct());
        invoice.setDescription(details.getDescription());
        if (details.isSetCart()) {
            invoice.setCartJson(DamselUtil.toJsonString(details.getCart()));
        }
    }

    private void fillCash(com.rbkmoney.damsel.domain.Invoice damselInvoice, Invoice invoice) {
        invoice.setAmount(damselInvoice.getCost().getAmount());
        invoice.setCurrencyCode(damselInvoice.getCost().getCurrency().getSymbolicCode());
    }

    private void fillInvoiceContext(com.rbkmoney.damsel.domain.Invoice damselInvoice, Invoice invoice) {
        if (damselInvoice.isSetContext()) {
            Content content = damselInvoice.getContext();

            invoice.setContextType(content.getType());
            invoice.setContext(content.getData());
        }
    }
}
