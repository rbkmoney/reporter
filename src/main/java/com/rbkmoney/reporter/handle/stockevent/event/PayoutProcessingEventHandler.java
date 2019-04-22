package com.rbkmoney.reporter.handle.stockevent.event;

import com.rbkmoney.damsel.event_stock.StockEvent;
import com.rbkmoney.damsel.payout_processing.Event;
import com.rbkmoney.reporter.handle.EventHandler;

public interface PayoutProcessingEventHandler extends EventHandler<Event, StockEvent> {

}
