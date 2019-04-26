package com.rbkmoney.reporter.serialization.impl;

import com.rbkmoney.reporter.serialization.BinaryDeserializer;
import lombok.extern.slf4j.Slf4j;
import org.apache.thrift.TBase;
import org.apache.thrift.TDeserializer;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;

@Slf4j
public abstract class AbstractThriftDeserializer<T extends TBase> implements BinaryDeserializer<T> {

    private final ThreadLocal<TDeserializer> tDeserializerThreadLocal = ThreadLocal.withInitial(() -> new TDeserializer(new TBinaryProtocol.Factory()));

    protected abstract T getTerminalObject();

    @Override
    public final T deserialize(byte[] bin) throws Exception {
        log.debug("Deserialize, bin[]: {}", bin.length);
        T terminalObject = getTerminalObject();
        try {
            tDeserializerThreadLocal.get().deserialize(terminalObject, bin);
        } catch (TException e) {
            log.error("Error when deserialize " + terminalObject.getClass() + " bin[]: {} ", bin.length, e);
            throw new Exception(e);
        }
        return terminalObject;
    }
}
