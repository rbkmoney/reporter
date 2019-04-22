package com.rbkmoney.reporter.handle.machineevent.impl;

import com.rbkmoney.damsel.payout_processing.EventPayload;
import com.rbkmoney.machinegun.eventsink.MachineEvent;
import com.rbkmoney.reporter.handle.machineevent.MachineEventHandler;
import com.rbkmoney.reporter.handle.machineevent.payout.PayoutProcessingMachineEventHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class PayoutMachineEventHandler implements MachineEventHandler<EventPayload> {

    private final List<PayoutProcessingMachineEventHandler> handlers;

    @Override
    public void handle(EventPayload payload, MachineEvent baseEvent) {
        log.info("Start handling payout processing MachineEvent, payload='{}'", payload);
        try {
            for (PayoutProcessingMachineEventHandler handler : handlers) {
                if (handler.accept(payload)) {
                    handler.handle(payload, baseEvent);
                }
            }
        } catch (Exception ex) {
            log.error("Failed to handling payout processing MachineEvent, payload='{}'", payload, ex);
            throw ex;
        }
        log.info("End handling payout processing MachineEvent, payload='{}'", payload);
    }
}
