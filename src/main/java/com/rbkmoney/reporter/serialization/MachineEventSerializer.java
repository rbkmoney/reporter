package com.rbkmoney.reporter.serialization;

import com.rbkmoney.machinegun.eventsink.SinkEvent;
import org.apache.kafka.common.serialization.Serializer;

import java.util.Map;

public interface MachineEventSerializer extends Serializer<SinkEvent> {

    @Override
    default void configure(Map<String, ?> configs, boolean isKey) {
    }

    @Override
    default void close() {
    }
}
