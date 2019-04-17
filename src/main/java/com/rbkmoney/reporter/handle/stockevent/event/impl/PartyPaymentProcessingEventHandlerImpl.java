package com.rbkmoney.reporter.handle.stockevent.event.impl;

import com.rbkmoney.damsel.event_stock.StockEvent;
import com.rbkmoney.damsel.payment_processing.Event;
import com.rbkmoney.reporter.handle.stockevent.event.PaymentProcessingEventHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class PartyPaymentProcessingEventHandlerImpl implements PaymentProcessingEventHandler {

    @Override
    public boolean accept(Event specific) {
        return specific.getPayload().isSetPartyChanges();
    }

    @Override
    public void handle(Event specific, StockEvent stockEvent) {
    }
}
