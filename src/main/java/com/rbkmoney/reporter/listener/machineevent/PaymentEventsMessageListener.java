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

@Slf4j
@RequiredArgsConstructor
public class PaymentEventsMessageListener {

    private final Parser<MachineEvent, EventPayload> parser;
    private final MachineEventHandler<EventPayload> handler;

    @KafkaListener(topics = "${kafka.topics.invoice.id}", containerFactory = "kafkaListenerContainerFactory")
    public void listen(SinkEvent event, Acknowledgment ack) {
        MachineEvent machineEvent = event.getEvent();

        EventPayload payload = parser.parse(machineEvent);

        handler.handle(payload, machineEvent);

        ack.acknowledge();
    }
}
