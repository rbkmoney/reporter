package com.rbkmoney.reporter.handle.stockevent.impl;

import com.rbkmoney.damsel.event_stock.StockEvent;
import com.rbkmoney.reporter.handle.stockevent.StockEventHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class PayoutProcessingStockEventHandler implements StockEventHandler {

    @Override
    public boolean accept(StockEvent specific) {
        return specific.getSourceEvent().isSetPayoutEvent();
    }

    @Override
    public void handle(StockEvent specific, StockEvent stockEvent) {
    }
}
