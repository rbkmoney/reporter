package com.rbkmoney.reporter.serialization.impl;

import com.rbkmoney.machinegun.eventsink.SinkEvent;
import com.rbkmoney.reporter.serialization.MachineEventDeserializer;
import lombok.extern.slf4j.Slf4j;
import org.apache.thrift.TDeserializer;
import org.apache.thrift.protocol.TBinaryProtocol;

@Slf4j
public class MachineEventDeserializerImpl implements MachineEventDeserializer {

    private final ThreadLocal<TDeserializer> tDeserializerThreadLocal = getTDeserializerThreadLocal();

    @Override
    public SinkEvent deserialize(String topic, byte[] data) {
        log.debug("Message, topic: {}, byteLength: {}", topic, data.length);
        SinkEvent event = new SinkEvent();
        try {
            tDeserializerThreadLocal.get().deserialize(event, data);
        } catch (Exception e) {
            log.error("Error when deserialize ruleTemplate data: {} ", data, e);
        }
        return event;
    }

    private ThreadLocal<TDeserializer> getTDeserializerThreadLocal() {
        return ThreadLocal.withInitial(() -> new TDeserializer(new TBinaryProtocol.Factory()));
    }
}
