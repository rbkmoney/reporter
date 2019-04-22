package com.rbkmoney.reporter.handle.machineevent.processing.impl;

import com.rbkmoney.damsel.payment_processing.EventPayload;
import com.rbkmoney.damsel.payment_processing.PartyChange;
import com.rbkmoney.machinegun.eventsink.MachineEvent;
import com.rbkmoney.reporter.handle.machineevent.processing.PaymentProcessingMachineEventHandler;
import com.rbkmoney.reporter.handle.machineevent.processing.change.PartyChangeMachineEventHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class PartyChangePaymentProcessingMachineEventHandler implements PaymentProcessingMachineEventHandler {

    private final List<PartyChangeMachineEventHandler> eventHandlers;

    @Override
    public boolean accept(EventPayload payload) {
        return payload.isSetPartyChanges();
    }

    @Override
    public void handle(EventPayload payload, MachineEvent baseEvent) {
        for (PartyChange change : payload.getPartyChanges()) {
            for (PartyChangeMachineEventHandler eventHandler : eventHandlers) {
                if (eventHandler.accept(change)) {
                    eventHandler.handle(change, baseEvent);
                }
            }
        }
    }
}
