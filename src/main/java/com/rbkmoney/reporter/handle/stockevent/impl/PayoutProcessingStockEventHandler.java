package com.rbkmoney.reporter.handle.stockevent.impl;

import com.rbkmoney.damsel.event_stock.StockEvent;
import com.rbkmoney.damsel.payout_processing.Event;
import com.rbkmoney.reporter.handle.stockevent.StockEventHandler;
import com.rbkmoney.reporter.handle.stockevent.event.PayoutProcessingEventHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class PayoutProcessingStockEventHandler implements StockEventHandler {

    private final List<PayoutProcessingEventHandler> eventHandlers;

    @Override
    public boolean accept(StockEvent specific) {
        return specific.getSourceEvent().isSetPayoutEvent();
    }

    @Override
    public void handle(StockEvent specific, StockEvent stockEvent) {
        Event payoutEvent = specific.getSourceEvent().getPayoutEvent();
        for (PayoutProcessingEventHandler eventHandler : eventHandlers) {
            if (eventHandler.accept(payoutEvent)) {
                eventHandler.handle(payoutEvent, stockEvent);
            }
        }
    }
}
