package com.rbkmoney.reporter.parser.impl;

import com.rbkmoney.damsel.payment_processing.EventPayload;
import com.rbkmoney.reporter.serialization.BinaryDeserializer;
import org.springframework.stereotype.Component;

@Component
public class PaymentMachineEventParser extends AbstractMachineEventParser<EventPayload> {

    public PaymentMachineEventParser(BinaryDeserializer<EventPayload> deserializer) {
        super(deserializer);
    }
}
