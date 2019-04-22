package com.rbkmoney.reporter.serialization.impl;

import com.rbkmoney.reporter.serialization.BinarySerializer;
import lombok.extern.slf4j.Slf4j;
import org.apache.thrift.TBase;
import org.apache.thrift.TException;
import org.apache.thrift.TSerializer;
import org.apache.thrift.protocol.TBinaryProtocol;

@Slf4j
public abstract class AbstractThriftSerializer<T extends TBase> implements BinarySerializer<T> {

    private final ThreadLocal<TSerializer> tSerializerThreadLocal = ThreadLocal.withInitial(() -> new TSerializer(new TBinaryProtocol.Factory()));

    @Override
    public final byte[] serialize(T data) throws Exception {
        byte[] bin;
        try {
            bin = tSerializerThreadLocal.get().serialize(data);
        } catch (TException e) {
            log.error(this.getClass() + " e: ", e);
            throw new Exception(e);
        }
        return bin;
    }
}
