package com.rbkmoney.reporter.parser.impl;

import com.rbkmoney.machinegun.eventsink.MachineEvent;
import com.rbkmoney.reporter.exception.ParseException;
import com.rbkmoney.reporter.parser.Parser;
import com.rbkmoney.reporter.serialization.BinaryDeserializer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public abstract class AbstractMachineEventParser<T> implements Parser<MachineEvent, T> {

    private final BinaryDeserializer<T> deserializer;

    @Override
    public T parse(MachineEvent data) {
        try {
            byte[] bin = data.getData().getBin();
            return deserializer.deserialize(bin);
        } catch (Exception e) {
            log.error("Exception when parse message e: ", e);
            throw new ParseException();
        }
    }
}
