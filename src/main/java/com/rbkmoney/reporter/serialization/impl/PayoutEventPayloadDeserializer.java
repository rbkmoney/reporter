package com.rbkmoney.reporter.serialization.impl;

import com.rbkmoney.damsel.payout_processing.EventPayload;
import org.springframework.stereotype.Component;

@Component
public class PayoutEventPayloadDeserializer extends AbstractThriftDeserializer<EventPayload> {

    @Override
    protected EventPayload getTerminalObject() {
        return new EventPayload();
    }
}
