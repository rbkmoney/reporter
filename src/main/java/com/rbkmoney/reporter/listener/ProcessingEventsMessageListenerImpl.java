package com.rbkmoney.reporter.listener;

import com.rbkmoney.damsel.event_stock.SourceEvent;
import com.rbkmoney.damsel.event_stock.StockEvent;
import com.rbkmoney.damsel.payment_processing.Event;
import com.rbkmoney.damsel.payment_processing.EventPayload;
import com.rbkmoney.damsel.payment_processing.EventSource;
import com.rbkmoney.machinegun.eventsink.MachineEvent;
import com.rbkmoney.reporter.converter.SourceEventParser;
import com.rbkmoney.reporter.handler.EventStockEventHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;

@Slf4j
@RequiredArgsConstructor
public class ProcessingEventsMessageListenerImpl implements MessageListener {

    private final SourceEventParser eventParser;
    private final EventStockEventHandler handler;

    @KafkaListener(topics = "${kafka.processing.topic.name}", containerFactory = "kafkaListenerContainerFactory")
    @Override
    public void listen(MachineEvent machineEvent, Acknowledgment ack) throws Exception {
        EventPayload payload = eventParser.parseEvent(machineEvent);

        StockEvent stockEvent = wrap(machineEvent, payload);
        try {
            handler.handle(stockEvent, "");
        } catch (Exception ex) {
            log.error("Failed to handle event payload, payload='{}'", payload, ex);
            throw ex;
        }

        ack.acknowledge();
    }

    private StockEvent wrap(MachineEvent machineEvent, EventPayload payload) {
        Event event = new Event();
        event.setId(machineEvent.getEventId());
        event.setCreatedAt(machineEvent.getCreatedAt());
        event.setSource(EventSource.invoice_id(""));
        event.setPayload(payload);
        event.setSequence((int) machineEvent.getEventId());

        return new StockEvent(SourceEvent.processing_event(event));
    }
}
