package com.rbkmoney.reporter.serialization.impl;

import com.rbkmoney.damsel.payment_processing.EventPayload;
import org.springframework.stereotype.Component;

@Component
public class PaymentEventPayloadSerializer extends AbstractThriftSerializer<EventPayload> {

}
