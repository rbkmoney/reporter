package com.rbkmoney.reporter.service.impl;

import com.rbkmoney.damsel.payment_processing.EventPayload;
import com.rbkmoney.damsel.payment_processing.InvoiceChange;
import com.rbkmoney.machinegun.eventsink.MachineEvent;
import com.rbkmoney.reporter.handler.EventHandler;
import com.rbkmoney.reporter.service.EventService;
import com.rbkmoney.sink.common.parser.Parser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class InvoicingService implements EventService<InvoiceChange> {

    private final Parser<MachineEvent, EventPayload> paymentMachineEventParser;

    private final List<EventHandler<InvoiceChange>> payloadHandlers;

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void handleEvents(List<MachineEvent> events) throws Exception {
        for (MachineEvent machineEvent : events) {
            EventPayload eventPayload = paymentMachineEventParser.parse(machineEvent);
            if (eventPayload.isSetInvoiceChanges()) {
                List<InvoiceChange> invoiceChanges = eventPayload.getInvoiceChanges();
                for (int i = 0; i < invoiceChanges.size(); i++) {
                    handleIfAccept(payloadHandlers, machineEvent, invoiceChanges.get(i), i);
                }
            }
        }
    }

}