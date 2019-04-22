package com.rbkmoney.reporter.serialization.impl;

import com.rbkmoney.machinegun.eventsink.SinkEvent;
import com.rbkmoney.reporter.serialization.MachineEventSerializer;
import lombok.extern.slf4j.Slf4j;
import org.apache.thrift.TSerializer;
import org.apache.thrift.protocol.TBinaryProtocol;

@Slf4j
public class MachineEventSerializerImpl implements MachineEventSerializer {

    private final ThreadLocal<TSerializer> tSerializerThreadLocal = getTSerializerThreadLocal();

    @Override
    public byte[] serialize(String topic, SinkEvent data) {
        log.debug("Message, topic: {}, data: {}", topic, data);
        byte[] retVal = null;
        try {
            retVal = tSerializerThreadLocal.get().serialize(data);
        } catch (Exception e) {
            log.error("Error when serialize RuleTemplate data: {} ", data, e);
        }
        return retVal;
    }

    private ThreadLocal<TSerializer> getTSerializerThreadLocal() {
        return ThreadLocal.withInitial(() -> new TSerializer(new TBinaryProtocol.Factory()));
    }
}
