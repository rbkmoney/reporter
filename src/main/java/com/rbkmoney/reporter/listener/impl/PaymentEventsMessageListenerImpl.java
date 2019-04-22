package com.rbkmoney.reporter.listener.impl;

import com.rbkmoney.damsel.payment_processing.EventPayload;
import com.rbkmoney.machinegun.eventsink.MachineEvent;
import com.rbkmoney.reporter.handle.machineevent.MachineEventHandler;
import com.rbkmoney.reporter.listener.MessageListener;
import com.rbkmoney.reporter.parser.Parser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;

@Slf4j
@RequiredArgsConstructor
public class PaymentEventsMessageListenerImpl implements MessageListener {

    private final Parser<MachineEvent, EventPayload> parser;
    private final MachineEventHandler<EventPayload> handler;

    @KafkaListener(topics = "${kafka.processing.payment.topic}", containerFactory = "kafkaListenerContainerFactory")
    @Override
    public void listen(MachineEvent machineEvent, Acknowledgment ack) {
        EventPayload payload = parser.parse(machineEvent);

        handler.handle(payload, machineEvent);

        ack.acknowledge();
    }
}
