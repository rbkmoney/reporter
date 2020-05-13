package com.rbkmoney.reporter.mapper.machineevent.invoice;

import com.rbkmoney.damsel.payment_processing.InvoiceChange;
import com.rbkmoney.geck.common.util.TBaseUtil;
import com.rbkmoney.geck.common.util.TypeUtil;
import com.rbkmoney.machinegun.eventsink.MachineEvent;
import com.rbkmoney.reporter.domain.enums.InvoiceStatus;
import com.rbkmoney.reporter.domain.tables.pojos.InvoiceState;
import com.rbkmoney.reporter.mapper.InvoiceChangeMapper;
import com.rbkmoney.reporter.mapper.MapperResult;
import com.rbkmoney.reporter.util.DamselUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@Slf4j
public class InvoiceStatusChangedChangeMapperImpl implements InvoiceChangeMapper {

    @Override
    public boolean canMap(InvoiceChange payload) {
        return payload.isSetInvoiceStatusChanged();
    }

    @Override
    public MapperResult map(InvoiceChange payload, MachineEvent baseEvent, Integer changeId) {
        var damselInvoiceStatus = getStatus(payload);

        String invoiceId = baseEvent.getSourceId();
        long sequenceId = baseEvent.getEventId();
        LocalDateTime eventCreatedAt = TypeUtil.stringToLocalDateTime(baseEvent.getCreatedAt());
        InvoiceStatus invoiceStatus = TBaseUtil.unionFieldToEnum(damselInvoiceStatus, InvoiceStatus.class);

        InvoiceState invoiceState = getInvoiceState(changeId, damselInvoiceStatus, invoiceId, sequenceId, eventCreatedAt, invoiceStatus);

        log.info("Invoice with eventType=[statusChanged] and status {} has been mapped, invoiceId={}",
                invoiceStatus, invoiceId);

        return new MapperResult(invoiceState);
    }

    private InvoiceState getInvoiceState(Integer changeId, com.rbkmoney.damsel.domain.InvoiceStatus damselInvoiceStatus, String invoiceId, long sequenceId, LocalDateTime eventCreatedAt, InvoiceStatus invoiceStatus) {
        InvoiceState invoiceState = new InvoiceState();
        invoiceState.setInvoiceId(invoiceId);
        invoiceState.setSequenceId(sequenceId);
        invoiceState.setChangeId(changeId);
        invoiceState.setEventCreatedAt(eventCreatedAt);
        invoiceState.setStatus(invoiceStatus);
        invoiceState.setStatusDetails(DamselUtil.getInvoiceStatusDetails(damselInvoiceStatus));

        return invoiceState;
    }

    private com.rbkmoney.damsel.domain.InvoiceStatus getStatus(InvoiceChange payload) {
        return payload.getInvoiceStatusChanged().getStatus();
    }
}
