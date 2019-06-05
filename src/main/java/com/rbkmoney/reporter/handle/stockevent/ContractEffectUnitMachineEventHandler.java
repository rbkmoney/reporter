package com.rbkmoney.reporter.handle.stockevent;

import com.rbkmoney.damsel.domain.Contract;
import com.rbkmoney.damsel.domain.ReportPreferences;
import com.rbkmoney.damsel.domain.ServiceAcceptanceActPreferences;
import com.rbkmoney.damsel.event_stock.StockEvent;
import com.rbkmoney.damsel.payment_processing.ClaimEffect;
import com.rbkmoney.damsel.payment_processing.ContractEffect;
import com.rbkmoney.damsel.payment_processing.ContractEffectUnit;
import com.rbkmoney.damsel.payment_processing.Event;
import com.rbkmoney.reporter.service.TaskService;
import com.rbkmoney.sink.common.handle.stockevent.event.change.claimeffect.ClaimEffectEventHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class ContractEffectUnitMachineEventHandler implements ClaimEffectEventHandler {

    private final TaskService taskService;

    @Override
    public boolean accept(ClaimEffect payload) {
        return payload.isSetContractEffect();
    }

    @Override
    public void handle(ClaimEffect payload, StockEvent baseEvent, Integer changeId) {
        Event event = baseEvent.getSourceEvent().getProcessingEvent();
        ContractEffectUnit contractEffectUnit = payload.getContractEffect();

        long eventId = event.getId();
        String partyId = event.getSource().getPartyId();
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
