package com.rbkmoney.reporter.converter.impl;

import com.rbkmoney.damsel.payout_processing.EventPayload;
import com.rbkmoney.reporter.converter.BinaryConverter;
import lombok.extern.slf4j.Slf4j;
import org.apache.thrift.TDeserializer;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class PayoutProcessingEventPayloadConverter implements BinaryConverter<EventPayload> {

    private final ThreadLocal<TDeserializer> tDeserializerThreadLocal = ThreadLocal.withInitial(() -> new TDeserializer(new TBinaryProtocol.Factory()));

    @Override
    public EventPayload convert(byte[] bin, Class<EventPayload> clazz) throws Exception {
        EventPayload event = new EventPayload();
        try {
            tDeserializerThreadLocal.get().deserialize(event, bin);
        } catch (TException e) {
            log.error("PayoutProcessingEventPayloadConverterImpl e: ", e);
            throw new Exception(e);
        }
        return event;
    }
}
