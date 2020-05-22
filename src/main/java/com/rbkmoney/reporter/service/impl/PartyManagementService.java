package com.rbkmoney.reporter.service.impl;

import com.rbkmoney.damsel.payment_processing.PartyChange;
import com.rbkmoney.damsel.payment_processing.PartyEventData;
import com.rbkmoney.machinegun.eventsink.MachineEvent;
import com.rbkmoney.reporter.exception.StorageException;
import com.rbkmoney.reporter.handler.EventHandler;
import com.rbkmoney.reporter.service.EventService;
import com.rbkmoney.sink.common.parser.impl.MachineEventParser;
import com.rbkmoney.woody.api.flow.error.WRuntimeException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PartyManagementService implements EventService {

    private final MachineEventParser<PartyEventData> parser;

    private final List<EventHandler<PartyChange>> partyEventHandlers;

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void handleEvents(List<MachineEvent> machineEvents) throws Exception {
        for (MachineEvent machineEvent : machineEvents) {
            PartyEventData partyEventData = parser.parse(machineEvent);
            if (partyEventData.isSetChanges()) {
                List<PartyChange> changes = partyEventData.getChanges();
                for (int i = 0; i < changes.size(); i++) {
                    handleIfAccept(machineEvent, changes.get(i), i);
                }
            }
        }
    }

    private void handleIfAccept(MachineEvent machineEvent,
                                PartyChange change,
                                int changeId) throws Exception {
        try {
            for (EventHandler<PartyChange> handler : partyEventHandlers) {
                if (handler.isAccept(change)) {
                    handler.handle(machineEvent, change, changeId);
                }
            }
        } catch (StorageException | WRuntimeException ex) {
            log.warn("Failed to handle event, retry", ex);
            throw ex;
        }
    }

}
