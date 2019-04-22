package com.rbkmoney.reporter.converter;

import com.rbkmoney.machinegun.eventsink.MachineEvent;
import com.rbkmoney.reporter.converter.impl.PaymentProcessingEventPayloadConverter;
import com.rbkmoney.reporter.converter.impl.PayoutProcessingEventPayloadConverter;
import com.rbkmoney.reporter.exception.ParseException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SourceEventParser {

    private final PaymentProcessingEventPayloadConverter converterPaymentProcessingEvent;
    private final PayoutProcessingEventPayloadConverter converterPayoutProcessingEvent;

    public com.rbkmoney.damsel.payment_processing.EventPayload parsePaymentProcessingEvent(MachineEvent message) {
        try {
            byte[] bin = message.getData().getBin();
            return converterPaymentProcessingEvent.convert(bin, com.rbkmoney.damsel.payment_processing.EventPayload.class);
        } catch (Exception e) {
            log.error("Exception when parse message e: ", e);
            throw new ParseException();
        }
    }

    public com.rbkmoney.damsel.payout_processing.EventPayload parsePayoutProcessingEvent(MachineEvent message) {
        try {
            byte[] bin = message.getData().getBin();
            return converterPayoutProcessingEvent.convert(bin, com.rbkmoney.damsel.payout_processing.EventPayload.class);
        } catch (Exception e) {
            log.error("Exception when parse message e: ", e);
            throw new ParseException();
        }
    }
}
