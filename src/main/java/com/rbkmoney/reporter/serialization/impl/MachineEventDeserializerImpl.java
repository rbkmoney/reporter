package com.rbkmoney.reporter.serialization.impl;

import com.rbkmoney.machinegun.eventsink.MachineEvent;
import com.rbkmoney.machinegun.eventsink.SinkEvent;
import com.rbkmoney.reporter.serialization.MachineEventDeserializer;
import lombok.extern.slf4j.Slf4j;
import org.apache.thrift.TDeserializer;
import org.apache.thrift.protocol.TBinaryProtocol;

@Slf4j
public class MachineEventDeserializerImpl implements MachineEventDeserializer {

    private final ThreadLocal<TDeserializer> tDeserializerThreadLocal = getTDeserializerThreadLocal();

    @Override
    public MachineEvent deserialize(String topic, byte[] data) {
        log.debug("Message, topic: {}, byteLength: {}", topic, data.length);
        SinkEvent machineEvent = new SinkEvent();
        try {
            tDeserializerThreadLocal.get().deserialize(machineEvent, data);
        } catch (Exception e) {
            log.error("Error when deserialize ruleTemplate data: {} ", data, e);
        }
        return machineEvent.getEvent();
    }

    private ThreadLocal<TDeserializer> getTDeserializerThreadLocal() {
        return ThreadLocal.withInitial(() -> new TDeserializer(new TBinaryProtocol.Factory()));
    }
}
