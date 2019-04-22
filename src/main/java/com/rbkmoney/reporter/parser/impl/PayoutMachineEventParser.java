package com.rbkmoney.reporter.parser.impl;

import com.rbkmoney.damsel.payout_processing.EventPayload;
import com.rbkmoney.reporter.serialization.BinaryDeserializer;
import org.springframework.stereotype.Component;

@Component
public class PayoutMachineEventParser extends AbstractMachineEventParser<EventPayload> {

    public PayoutMachineEventParser(BinaryDeserializer<EventPayload> deserializer) {
        super(deserializer);
    }
}
