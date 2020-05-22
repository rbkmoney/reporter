package com.rbkmoney.reporter.service.impl;

import com.rbkmoney.damsel.payment_processing.EventPayload;
import com.rbkmoney.damsel.payment_processing.InvoiceChange;
import com.rbkmoney.machinegun.eventsink.MachineEvent;
import com.rbkmoney.reporter.exception.StorageException;
import com.rbkmoney.reporter.handler.EventHandler;
import com.rbkmoney.reporter.service.EventService;
import com.rbkmoney.sink.common.parser.Parser;
import com.rbkmoney.woody.api.flow.error.WRuntimeException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class InvoicingService implements EventService {

    private final Parser<MachineEvent, EventPayload> paymentMachineEventParser;

    private final List<EventHandler<InvoiceChange>> payloadHandlers;

    @Override
    public void handleEvents(List<MachineEvent> events) throws Exception {
        for (MachineEvent machineEvent : events) {
            EventPayload eventPayload = paymentMachineEventParser.parse(machineEvent);
            if (eventPayload.isSetInvoiceChanges()) {
                List<InvoiceChange> invoiceChanges = eventPayload.getInvoiceChanges();
                for (int i = 0; i < invoiceChanges.size(); i++) {
                    handleIfAccept(machineEvent, invoiceChanges.get(i), i);
                }
            }
        }
    }

    private void handleIfAccept(MachineEvent machineEvent,
                                InvoiceChange change,
                                int changeId) throws Exception {
        try {
            for (EventHandler<InvoiceChange> handler : payloadHandlers) {
                if (handler.isAccept(change)) {
                    handler.handle(machineEvent, change, changeId);
                }
            }
        } catch (StorageException | WRuntimeException ex) {
            log.warn("Failed to handle event, retry", ex);
            throw ex;
        }
    }

}
