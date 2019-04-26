package com.rbkmoney.reporter.serialization.impl;

import com.rbkmoney.machinegun.eventsink.SinkEvent;
import com.rbkmoney.reporter.serialization.MachineEventSerializer;
import lombok.extern.slf4j.Slf4j;
import org.apache.thrift.TSerializer;
import org.apache.thrift.protocol.TBinaryProtocol;

@Slf4j
public class MachineEventSerializerImpl implements MachineEventSerializer {

    private final ThreadLocal<TSerializer> tSerializerThreadLocal = ThreadLocal.withInitial(() -> new TSerializer(new TBinaryProtocol.Factory()));

    @Override
    public byte[] serialize(String topic, SinkEvent data) {
        log.debug("Serialize message, topic: {}, data: {}", topic, data);
        byte[] retVal = null;
        try {
            retVal = tSerializerThreadLocal.get().serialize(data);
        } catch (Exception e) {
            log.error("Error when serialize machinegun SinkEvent data: {} ", data, e);
        }
        return retVal;
    }
}
