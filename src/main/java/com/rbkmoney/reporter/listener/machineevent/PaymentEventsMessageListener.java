package com.rbkmoney.reporter.listener.machineevent;

import com.rbkmoney.damsel.payment_processing.EventPayload;
import com.rbkmoney.machinegun.eventsink.MachineEvent;
import com.rbkmoney.machinegun.eventsink.SinkEvent;
import com.rbkmoney.sink.common.handle.machineevent.MachineEventHandler;
import com.rbkmoney.sink.common.parser.Parser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class PaymentEventsMessageListener {

    private final Parser<MachineEvent, EventPayload> paymentEventPayloadMachineEventParser;
    private final MachineEventHandler<EventPayload> paymentEventMachineEventHandler;

    @KafkaListener(topics = "${kafka.topics.invoice.id}", containerFactory = "kafkaListenerContainerFactory")
    public void listen(SinkEvent event, Acknowledgment ack) {
        MachineEvent machineEvent = event.getEvent();

        EventPayload payload = paymentEventPayloadMachineEventParser.parse(machineEvent);

        paymentEventMachineEventHandler.handle(payload, machineEvent);

        ack.acknowledge();
    }
}
