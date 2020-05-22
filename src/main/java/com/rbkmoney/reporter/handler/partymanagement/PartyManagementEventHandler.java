package com.rbkmoney.reporter.handler.partymanagement;

import com.rbkmoney.damsel.payment_processing.*;
import com.rbkmoney.machinegun.eventsink.MachineEvent;
import com.rbkmoney.reporter.handler.EventHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class PartyManagementEventHandler implements EventHandler<PartyChange> {

    private final List<EventHandler<ContractEffectUnit>> eventHandlers;

    @Override
    public void handle(MachineEvent machineEvent, PartyChange change, int changeId) {
        ClaimAccepted accepted = change.getClaimStatusChanged().getStatus().getAccepted();
        accepted.getEffects().stream()
                .filter(effect -> effect.isSetContractEffect())
                .map(ClaimEffect::getContractEffect)
                .forEach(contractEffect -> handleContractEffect(machineEvent, contractEffect, changeId));
    }

    private void handleContractEffect(MachineEvent machineEvent, ContractEffectUnit contractEffect, int changeId) {
        for (EventHandler<ContractEffectUnit> handler : eventHandlers) {
            if (handler.isAccept(contractEffect)) {
                try {
                    handler.handle(machineEvent, contractEffect, changeId);
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }
            }
        }
    }

    @Override
    public boolean isAccept(PartyChange change) {
        return change.isSetClaimStatusChanged()
                && change.getClaimStatusChanged().isSetStatus()
                && change.getClaimStatusChanged().getStatus().isSetAccepted();
    }
}
