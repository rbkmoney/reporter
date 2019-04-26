package com.rbkmoney.reporter.handle.machineevent.payment.impl;

import com.rbkmoney.damsel.payment_processing.EventPayload;
import com.rbkmoney.damsel.payment_processing.InvoiceChange;
import com.rbkmoney.machinegun.eventsink.MachineEvent;
import com.rbkmoney.reporter.handle.machineevent.payment.PaymentProcessingMachineEventHandler;
import com.rbkmoney.reporter.handle.machineevent.payment.change.InvoiceChangeMachineEventHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class InvoiceChangePaymentProcessingMachineEventHandler implements PaymentProcessingMachineEventHandler {

    private final List<InvoiceChangeMachineEventHandler> eventHandlers;

    @Override
    public boolean accept(EventPayload payload) {
        return payload.isSetInvoiceChanges();
    }

    @Override
    public void handle(EventPayload payload, MachineEvent baseEvent) {
        for (int i = 0; i < payload.getInvoiceChanges().size(); i++) {
            InvoiceChange change = payload.getInvoiceChanges().get(i);
            for (InvoiceChangeMachineEventHandler eventHandler : eventHandlers) {
                if (eventHandler.accept(change)) {
                    eventHandler.handle(change, baseEvent, i);
                }
            }
        }
    }
}
