package com.rbkmoney.reporter.config;

import com.rbkmoney.damsel.payment_processing.EventPayload;
import com.rbkmoney.reporter.listener.machineevent.PaymentEventsMessageListener;
import com.rbkmoney.sink.common.handle.machineevent.MachineEventHandler;
import com.rbkmoney.sink.common.handle.machineevent.eventpayload.PaymentEventHandler;
import com.rbkmoney.sink.common.handle.machineevent.eventpayload.change.InvoiceChangeEventHandler;
import com.rbkmoney.sink.common.handle.machineevent.eventpayload.impl.InvoiceChangePaymentMachineEventHandler;
import com.rbkmoney.sink.common.handle.machineevent.impl.PaymentEventMachineEventHandler;
import com.rbkmoney.sink.common.parser.impl.MachineEventParser;
import com.rbkmoney.sink.common.parser.impl.PaymentEventPayloadMachineEventParser;
import com.rbkmoney.sink.common.serialization.BinaryDeserializer;
import com.rbkmoney.sink.common.serialization.impl.PaymentEventPayloadDeserializer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;

import java.util.List;

@Configuration
@EnableKafka
public class KafkaConsumerBeanEnableConfig {

    @Bean
    public PaymentEventHandler invoiceChangePaymentStockEventHandler(List<InvoiceChangeEventHandler> eventHandlers) {
        return new InvoiceChangePaymentMachineEventHandler(eventHandlers);
    }

    @Bean
    public MachineEventHandler<EventPayload> paymentEventMachineEventHandler(List<PaymentEventHandler> eventHandlers) {
        return new PaymentEventMachineEventHandler(eventHandlers);
    }

    @Bean
    public BinaryDeserializer<EventPayload> paymentEventPayloadDeserializer() {
        return new PaymentEventPayloadDeserializer();
    }

    @Bean
    public MachineEventParser<EventPayload> paymentEventPayloadMachineEventParser(BinaryDeserializer<EventPayload> paymentEventPayloadDeserializer) {
        return new PaymentEventPayloadMachineEventParser(paymentEventPayloadDeserializer);
    }

    @Bean
    @ConditionalOnProperty(value = "kafka.topics.invoice.enabled", havingValue = "true")
    public PaymentEventsMessageListener paymentEventsKafkaListener(MachineEventParser<EventPayload> paymentEventPayloadMachineEventParser,
                                                                   MachineEventHandler<EventPayload> paymentEventMachineEventHandler) {
        return new PaymentEventsMessageListener(paymentEventPayloadMachineEventParser, paymentEventMachineEventHandler);
    }
}
