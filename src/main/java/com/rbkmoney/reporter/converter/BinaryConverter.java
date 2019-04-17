package com.rbkmoney.reporter.converter;

public interface BinaryConverter<T> {

    T convert(byte[] bin, Class<T> clazz) throws Exception;

}
