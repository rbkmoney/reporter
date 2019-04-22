package com.rbkmoney.reporter.listener.impl;

import com.rbkmoney.damsel.payout_processing.EventPayload;
import com.rbkmoney.machinegun.eventsink.MachineEvent;
import com.rbkmoney.reporter.converter.SourceEventParser;
import com.rbkmoney.reporter.handle.machineevent.MachineEventHandler;
import com.rbkmoney.reporter.listener.MessageListener;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;

@Slf4j
@RequiredArgsConstructor
public class PayoutEventsMessageListenerImpl implements MessageListener {

    private final SourceEventParser eventParser;
    private final MachineEventHandler<EventPayload> handler;

    @KafkaListener(topics = "${kafka.processing.payout.topic}", containerFactory = "kafkaListenerContainerFactory")
    @Override
    public void listen(MachineEvent machineEvent, Acknowledgment ack) {
        EventPayload payload = eventParser.parsePayoutProcessingEvent(machineEvent);

        handler.handle(payload, machineEvent);

        ack.acknowledge();
    }
}
