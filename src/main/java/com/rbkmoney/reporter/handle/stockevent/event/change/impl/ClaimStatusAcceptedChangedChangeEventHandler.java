package com.rbkmoney.reporter.handle.stockevent.event.change.impl;

import com.rbkmoney.damsel.event_stock.StockEvent;
import com.rbkmoney.damsel.payment_processing.ClaimEffect;
import com.rbkmoney.damsel.payment_processing.PartyChange;
import com.rbkmoney.reporter.handle.stockevent.event.change.PartyChangeEventsHandler;
import com.rbkmoney.reporter.handle.stockevent.event.change.claimeffect.ClaimEffectEventsHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class ClaimStatusAcceptedChangedChangeEventHandler implements PartyChangeEventsHandler {

    private final List<ClaimEffectEventsHandler> eventsHandlers;

    @Override
    public boolean accept(PartyChange specific) {
        return specific.isSetClaimStatusChanged()
                && specific.getClaimStatusChanged().getStatus().isSetAccepted()
                && specific.getClaimStatusChanged().getStatus().getAccepted().isSetEffects();
    }

    @Override
    public void handle(PartyChange specific, StockEvent stockEvent) {
        for (ClaimEffect effect : specific.getClaimStatusChanged().getStatus().getAccepted().getEffects()) {
            for (ClaimEffectEventsHandler eventsHandler : eventsHandlers) {
                if (eventsHandler.accept(effect)) {
                    eventsHandler.handle(effect, stockEvent);
                }
            }
        }
    }
}
