package com.rbkmoney.reporter.handle.stockevent.event.impl;

import com.rbkmoney.damsel.event_stock.StockEvent;
import com.rbkmoney.damsel.payout_processing.Event;
import com.rbkmoney.damsel.payout_processing.PayoutChange;
import com.rbkmoney.reporter.handle.stockevent.event.PayoutProcessingEventHandler;
import com.rbkmoney.reporter.handle.stockevent.event.change.PayoutChangeEventsHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class PayoutChangePayoutProcessingEventHandlerImpl implements PayoutProcessingEventHandler {

    private final List<PayoutChangeEventsHandler> eventHandlers;

    @Override
    public boolean accept(Event payload) {
        return payload.getPayload().isSetPayoutChanges();
    }

    @Override
    public void handle(Event payload, StockEvent baseEvent) {
        for (PayoutChange change : payload.getPayload().getPayoutChanges()) {
            for (PayoutChangeEventsHandler eventHandler : eventHandlers) {
                if (eventHandler.accept(change)) {
                    eventHandler.handle(change, baseEvent);
                }
            }
        }
    }
}
