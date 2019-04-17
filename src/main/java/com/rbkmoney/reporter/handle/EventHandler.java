package com.rbkmoney.reporter.handle;

import com.rbkmoney.damsel.event_stock.StockEvent;

public interface EventHandler<T> {

    boolean accept(T specific);

    void handle(T specific, StockEvent stockEvent);

}
