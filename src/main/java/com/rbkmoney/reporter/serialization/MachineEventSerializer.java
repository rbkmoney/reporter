package com.rbkmoney.reporter.serialization;

import com.rbkmoney.machinegun.eventsink.MachineEvent;
import org.apache.kafka.common.serialization.Serializer;

import java.util.Map;

public interface MachineEventSerializer extends Serializer<MachineEvent> {

    @Override
    default void configure(Map<String, ?> configs, boolean isKey) {
    }

    @Override
    default void close() {
    }
}
