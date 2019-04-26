package com.rbkmoney.reporter.handle.machineevent.payout.impl;

import com.rbkmoney.damsel.payout_processing.EventPayload;
import com.rbkmoney.damsel.payout_processing.PayoutChange;
import com.rbkmoney.machinegun.eventsink.MachineEvent;
import com.rbkmoney.reporter.handle.machineevent.payout.PayoutProcessingMachineEventHandler;
import com.rbkmoney.reporter.handle.machineevent.payout.change.PayoutChangeMachineEventHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class PayoutChangePayoutProcessingMachineEventHandler implements PayoutProcessingMachineEventHandler {

    private final List<PayoutChangeMachineEventHandler> eventHandlers;

    @Override
    public boolean accept(EventPayload payload) {
        return payload.isSetPayoutChanges();
    }

    @Override
    public void handle(EventPayload payload, MachineEvent baseEvent) {
        for (PayoutChange change : payload.getPayoutChanges()) {
            for (PayoutChangeMachineEventHandler eventHandler : eventHandlers) {
                if (eventHandler.accept(change)) {
                    eventHandler.handle(change, baseEvent);
                }
            }
        }
    }
}
