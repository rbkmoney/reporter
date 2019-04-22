package com.rbkmoney.reporter.handle.machineevent;

import com.rbkmoney.machinegun.eventsink.MachineEvent;
import com.rbkmoney.reporter.handle.EventHandler;

public interface MachineEventHandler<T> extends EventHandler<T, MachineEvent> {

    @Override
    default boolean accept(T payload) {
        return true;
    }
}
