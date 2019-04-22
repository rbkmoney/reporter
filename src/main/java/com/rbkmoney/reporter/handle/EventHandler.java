package com.rbkmoney.reporter.handle;

public interface EventHandler<E, B> {

    boolean accept(E payload);

    void handle(E payload, B baseEvent);

}
