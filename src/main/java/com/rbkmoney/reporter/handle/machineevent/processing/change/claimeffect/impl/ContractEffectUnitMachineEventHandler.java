package com.rbkmoney.reporter.handle.machineevent.processing.change.claimeffect.impl;

import com.rbkmoney.damsel.domain.Contract;
import com.rbkmoney.damsel.domain.ReportPreferences;
import com.rbkmoney.damsel.domain.ServiceAcceptanceActPreferences;
import com.rbkmoney.damsel.payment_processing.ClaimEffect;
import com.rbkmoney.damsel.payment_processing.ContractEffect;
import com.rbkmoney.damsel.payment_processing.ContractEffectUnit;
import com.rbkmoney.machinegun.eventsink.MachineEvent;
import com.rbkmoney.reporter.handle.machineevent.processing.change.claimeffect.ClaimEffectMachineEventHandler;
import com.rbkmoney.reporter.service.TaskService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class ContractEffectUnitMachineEventHandler implements ClaimEffectMachineEventHandler {

    private final TaskService taskService;

    @Override
    public boolean accept(ClaimEffect payload) {
        return payload.isSetContractEffect();
    }

    @Override
    public void handle(ClaimEffect payload, MachineEvent baseEvent) {
        ContractEffectUnit contractEffectUnit = payload.getContractEffect();

        long eventId = baseEvent.getEventId();
        String partyId = baseEvent.getSourceId();
        String contractId = contractEffectUnit.getContractId();
        ContractEffect contractEffect = contractEffectUnit.getEffect();

        if (contractEffect.isSetCreated()) {
            Contract contract = contractEffect.getCreated();
            if (contract.isSetReportPreferences()) {
                ReportPreferences preferences = contract.getReportPreferences();
                if (preferences.isSetServiceAcceptanceActPreferences()) {
                    handlePreferences(partyId, contractId, eventId, preferences.getServiceAcceptanceActPreferences());
                }
            }
        } else if (contractEffect.isSetReportPreferencesChanged()) {
            ReportPreferences reportPreferences = contractEffect.getReportPreferencesChanged();
            if (reportPreferences.isSetServiceAcceptanceActPreferences()) {
                ServiceAcceptanceActPreferences preferences = reportPreferences.getServiceAcceptanceActPreferences();
                handlePreferences(partyId, contractId, eventId, preferences);
            } else {
                taskService.deregisterProvisionOfServiceJob(partyId, contractId);
            }
        }
    }

    private void handlePreferences(String partyId, String contractId, long eventId, ServiceAcceptanceActPreferences preferences) {
        taskService.registerProvisionOfServiceJob(partyId, contractId, eventId, preferences.getSchedule(), preferences.getSigner());
    }
}
