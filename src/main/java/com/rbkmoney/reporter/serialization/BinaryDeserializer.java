package com.rbkmoney.reporter.serialization;

public interface BinaryDeserializer<T> {

    T deserialize(byte[] bin) throws Exception;

}
