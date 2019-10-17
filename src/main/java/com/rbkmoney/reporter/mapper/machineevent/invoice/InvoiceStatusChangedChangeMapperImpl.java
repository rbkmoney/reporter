package com.rbkmoney.reporter.mapper.machineevent.invoice;

import com.rbkmoney.damsel.payment_processing.InvoiceChange;
import com.rbkmoney.damsel.payment_processing.InvoiceStatusChanged;
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

@Component
@Slf4j
public class InvoiceStatusChangedChangeMapperImpl implements InvoiceChangeMapper {

    @Override
    public MapperResult map(InvoiceChange payload, MachineEvent baseEvent, Integer changeId) {
        InvoiceStatusChanged invoiceStatusChanged = payload.getInvoiceStatusChanged();
        com.rbkmoney.damsel.domain.InvoiceStatus damselInvoiceStatus = invoiceStatusChanged.getStatus();
        InvoiceStatus invoiceStatus = TBaseUtil.unionFieldToEnum(damselInvoiceStatus, InvoiceStatus.class);
        String invoiceId = baseEvent.getSourceId();

        InvoiceState state = new InvoiceState();
        state.setInvoiceId(invoiceId);
        state.setSequenceId(baseEvent.getEventId());
        state.setChangeId(changeId);
        state.setCreatedAt(TypeUtil.stringToLocalDateTime(baseEvent.getCreatedAt()));
        state.setStatus(invoiceStatus);
        state.setStatusDetails(DamselUtil.getInvoiceStatusDetails(damselInvoiceStatus));
        log.info("Invoice with eventType=statusChanged and status {} has been mapped, invoiceId={}",
                invoiceStatus, invoiceId);
        return new MapperResult(state);
    }

    @Override
    public boolean canMap(InvoiceChange payload) {
        return payload.isSetInvoiceStatusChanged();
    }

    @Override
    public String[] getIgnoreProperties() {
        return new String[]{"id", "wtime", "current", "eventCreatedAt", "eventType", "sequenceId", "changeId",
                "invoiceStatus", "invoiceStatusDetails"};
    }

}
