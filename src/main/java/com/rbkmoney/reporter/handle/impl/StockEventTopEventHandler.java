package com.rbkmoney.reporter.handle.impl;

import com.rbkmoney.damsel.event_stock.StockEvent;
import com.rbkmoney.eventstock.client.EventAction;
import com.rbkmoney.eventstock.client.EventHandler;
import com.rbkmoney.reporter.handle.stockevent.StockEventHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class StockEventTopEventHandler implements EventHandler<StockEvent> {

    private final List<StockEventHandler> eventHandlers;

    @Override
    public EventAction handle(StockEvent stockEvent, String s) throws Exception {
        try {
            log.info("Start handling event stock, eventId={}", stockEvent.getId());
            for (StockEventHandler eventHandler : eventHandlers) {
                if (eventHandler.accept(stockEvent)) {
                    eventHandler.handle(stockEvent, stockEvent);
                }
            }
            return EventAction.CONTINUE;
        } catch (Exception ex) {
            log.warn("Failed to handle event, retry", ex);
            return EventAction.DELAYED_RETRY;
        }
    }
}
