package com.rbkmoney.reporter.handle.machineevent.impl;

import com.rbkmoney.damsel.payment_processing.EventPayload;
import com.rbkmoney.machinegun.eventsink.MachineEvent;
import com.rbkmoney.reporter.handle.machineevent.MachineEventHandler;
import com.rbkmoney.reporter.handle.machineevent.payment.PaymentProcessingMachineEventHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class PaymentMachineEventHandler implements MachineEventHandler<EventPayload> {

    private final List<PaymentProcessingMachineEventHandler> handlers;

    @Override
    public void handle(EventPayload payload, MachineEvent baseEvent) {
        log.info("Start handling payment processing MachineEvent, payload='{}'", payload);
        try {
            for (PaymentProcessingMachineEventHandler handler : handlers) {
                if (handler.accept(payload)) {
                    handler.handle(payload, baseEvent);
                }
            }
        } catch (Exception ex) {
            log.error("Failed to handling payment processing MachineEvent, payload='{}'", payload, ex);
            throw ex;
        }
        log.info("End handling payment processing MachineEvent, payload='{}'", payload);
    }
}
