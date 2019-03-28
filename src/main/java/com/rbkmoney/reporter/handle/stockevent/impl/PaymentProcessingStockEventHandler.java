package com.rbkmoney.reporter.handle.stockevent.impl;

import com.rbkmoney.damsel.event_stock.StockEvent;
import com.rbkmoney.damsel.payment_processing.Event;
import com.rbkmoney.reporter.handle.stockevent.StockEventHandler;
import com.rbkmoney.reporter.handle.stockevent.event.PaymentProcessingEventHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class PaymentProcessingStockEventHandler implements StockEventHandler {

    private final List<PaymentProcessingEventHandler> eventHandlers;

    @Override
    public boolean accept(StockEvent specific) {
        return specific.getSourceEvent().isSetProcessingEvent();
    }

    @Override
    public void handle(StockEvent specific, StockEvent stockEvent) {
        Event processingEvent = specific.getSourceEvent().getProcessingEvent();
        for (PaymentProcessingEventHandler eventHandler : eventHandlers) {
            if (eventHandler.accept(processingEvent)) {
                eventHandler.handle(processingEvent, stockEvent);
            }
        }
    }
}
