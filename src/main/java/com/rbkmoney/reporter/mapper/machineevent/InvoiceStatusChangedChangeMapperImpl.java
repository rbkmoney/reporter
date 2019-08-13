package com.rbkmoney.reporter.mapper.machineevent;

import com.rbkmoney.damsel.payment_processing.InvoiceChange;
import com.rbkmoney.damsel.payment_processing.InvoiceStatusChanged;
import com.rbkmoney.geck.common.util.TBaseUtil;
import com.rbkmoney.geck.common.util.TypeUtil;
import com.rbkmoney.machinegun.eventsink.MachineEvent;
import com.rbkmoney.reporter.domain.enums.InvoiceEventType;
import com.rbkmoney.reporter.domain.enums.InvoiceStatus;
import com.rbkmoney.reporter.domain.tables.pojos.Invoice;
import com.rbkmoney.reporter.mapper.InvoiceChangeMapper;
import com.rbkmoney.reporter.mapper.MapperResult;
import com.rbkmoney.reporter.util.DamselUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class InvoiceStatusChangedChangeMapperImpl implements InvoiceChangeMapper {

    @Override
    public String[] getIgnoreProperties() {
        return new String[]{"id", "wtime", "current", "eventCreatedAt", "eventType", "sequenceId", "changeId",
                "invoiceStatus", "invoiceStatusDetails"};
    }

    @Override
    public boolean canMap(InvoiceChange payload) {
        return payload.isSetInvoiceStatusChanged();
    }

    @Override
    public MapperResult map(InvoiceChange payload, MachineEvent baseEvent, Integer changeId) {
        InvoiceStatusChanged invoiceStatusChanged = payload.getInvoiceStatusChanged();
        com.rbkmoney.damsel.domain.InvoiceStatus damselInvoiceStatus = invoiceStatusChanged.getStatus();
        InvoiceStatus invoiceStatus = TBaseUtil.unionFieldToEnum(damselInvoiceStatus, InvoiceStatus.class);

        String invoiceId = baseEvent.getSourceId();

        Invoice invoice = new Invoice();

        invoice.setId(null);
        invoice.setWtime(null);
        invoice.setEventCreatedAt(TypeUtil.stringToLocalDateTime(baseEvent.getCreatedAt()));
        invoice.setEventType(InvoiceEventType.INVOICE_STATUS_CHANGED);
        invoice.setSequenceId(baseEvent.getEventId());
        invoice.setChangeId(changeId);
        invoice.setInvoiceStatus(invoiceStatus);
        invoice.setInvoiceStatusDetails(DamselUtil.getInvoiceStatusDetails(damselInvoiceStatus));

        log.info("Invoice with eventType=statusChanged and status {} has been mapped, invoiceId={}", invoiceStatus, invoiceId);

        return new MapperResult(invoice);
    }
}
