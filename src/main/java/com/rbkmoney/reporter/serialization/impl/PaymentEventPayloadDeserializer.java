package com.rbkmoney.reporter.serialization.impl;

import com.rbkmoney.damsel.payment_processing.EventPayload;
import org.springframework.stereotype.Component;

@Component
public class PaymentEventPayloadDeserializer extends AbstractThriftDeserializer<EventPayload> {

    @Override
    protected EventPayload getTerminalObject() {
        return new EventPayload();
    }
}
