package com.rbkmoney.reporter.handle;

public interface EventHandler<E, B> {

    boolean accept(E payload);

    default void handle(E payload, B baseEvent) {

    }

    default void handle(E payload, B baseEvent, Integer changeId) {

    }
}
