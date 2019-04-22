package com.rbkmoney.reporter.serialization;

public interface BinarySerializer<T> {

    byte[] serialize(T data) throws Exception;

}
