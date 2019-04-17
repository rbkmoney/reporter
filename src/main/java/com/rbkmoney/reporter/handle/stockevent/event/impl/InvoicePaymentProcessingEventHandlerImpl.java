package com.rbkmoney.reporter.handle.stockevent.event.impl;

import com.rbkmoney.damsel.event_stock.StockEvent;
import com.rbkmoney.damsel.payment_processing.Event;
import com.rbkmoney.damsel.payment_processing.InvoiceChange;
import com.rbkmoney.reporter.handle.stockevent.event.PaymentProcessingEventHandler;
import com.rbkmoney.reporter.handle.stockevent.event.change.InvoiceChangeEventsHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class InvoicePaymentProcessingEventHandlerImpl implements PaymentProcessingEventHandler {

    private final List<InvoiceChangeEventsHandler> eventHandlers;

    @Override
    public boolean accept(Event specific) {
        return specific.getPayload().isSetInvoiceChanges();
    }

    @Override
    public void handle(Event specific, StockEvent stockEvent) {
        for (InvoiceChange change : specific.getPayload().getInvoiceChanges()) {
            for (InvoiceChangeEventsHandler eventHandler : eventHandlers) {
                if (eventHandler.accept(change)) {
                    eventHandler.handle(change, stockEvent);
                }
            }
        }
    }
}
