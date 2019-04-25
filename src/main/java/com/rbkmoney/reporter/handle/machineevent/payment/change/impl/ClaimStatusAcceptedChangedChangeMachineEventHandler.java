package com.rbkmoney.reporter.handle.machineevent.payment.change.impl;

import com.rbkmoney.damsel.payment_processing.ClaimEffect;
import com.rbkmoney.damsel.payment_processing.PartyChange;
import com.rbkmoney.machinegun.eventsink.MachineEvent;
import com.rbkmoney.reporter.handle.machineevent.payment.change.PartyChangeMachineEventHandler;
import com.rbkmoney.reporter.handle.machineevent.payment.change.claimeffect.ClaimEffectMachineEventHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class ClaimStatusAcceptedChangedChangeMachineEventHandler implements PartyChangeMachineEventHandler {

    private final List<ClaimEffectMachineEventHandler> eventsHandlers;

    @Override
    public boolean accept(PartyChange payload) {
        return payload.isSetClaimStatusChanged()
                && payload.getClaimStatusChanged().getStatus().isSetAccepted()
                && payload.getClaimStatusChanged().getStatus().getAccepted().isSetEffects();
    }

    @Override
    public void handle(PartyChange payload, MachineEvent stockEvent) {
        for (int i = 0; i < payload.getClaimStatusChanged().getStatus().getAccepted().getEffects().size(); i++) {
            ClaimEffect effect = payload.getClaimStatusChanged().getStatus().getAccepted().getEffects().get(i);
            for (ClaimEffectMachineEventHandler eventsHandler : eventsHandlers) {
                if (eventsHandler.accept(effect)) {
                    eventsHandler.handle(effect, stockEvent, i);
                }
            }
        }
    }
}
