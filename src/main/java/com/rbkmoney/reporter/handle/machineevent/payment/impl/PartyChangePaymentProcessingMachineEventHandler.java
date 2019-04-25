package com.rbkmoney.reporter.handle.machineevent.payment.impl;

import com.rbkmoney.damsel.payment_processing.EventPayload;
import com.rbkmoney.damsel.payment_processing.PartyChange;
import com.rbkmoney.machinegun.eventsink.MachineEvent;
import com.rbkmoney.reporter.handle.machineevent.payment.PaymentProcessingMachineEventHandler;
import com.rbkmoney.reporter.handle.machineevent.payment.change.PartyChangeMachineEventHandler;
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
        for (int i = 0; i < payload.getPartyChanges().size(); i++) {
            PartyChange change = payload.getPartyChanges().get(i);
            for (PartyChangeMachineEventHandler eventHandler : eventHandlers) {
                if (eventHandler.accept(change)) {
                    eventHandler.handle(change, baseEvent, i);
                }
            }
        }
    }
}
