package com.rbkmoney.reporter.serialization.impl;

import com.rbkmoney.damsel.payment_processing.EventPayload;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class PayoutEventPayloadSerializer extends AbstractThriftSerializer<EventPayload> {

}

