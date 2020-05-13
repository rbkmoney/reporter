package com.rbkmoney.reporter.config;

import com.rbkmoney.damsel.payment_processing.EventPayload;
import com.rbkmoney.damsel.payment_processing.PartyEventData;
import com.rbkmoney.machinegun.eventsink.MachineEvent;
import com.rbkmoney.reporter.batch.InvoiceBatchManager;
import com.rbkmoney.reporter.dao.mapper.dto.PartyData;
import com.rbkmoney.reporter.dao.mapper.dto.PaymentPartyData;
import com.rbkmoney.reporter.handler.invoicing.InvoiceBatchHandler;
import com.rbkmoney.reporter.listener.InvoicingListener;
import com.rbkmoney.reporter.listener.PartyManagementListener;
import com.rbkmoney.reporter.service.BatchService;
import com.rbkmoney.reporter.service.PartyManagementService;
import com.rbkmoney.sink.common.parser.Parser;
import com.rbkmoney.sink.common.parser.impl.MachineEventParser;
import com.rbkmoney.sink.common.parser.impl.PartyEventDataMachineEventParser;
import com.rbkmoney.sink.common.parser.impl.PaymentEventPayloadMachineEventParser;
import com.rbkmoney.sink.common.serialization.BinaryDeserializer;
import com.rbkmoney.sink.common.serialization.impl.PartyEventDataDeserializer;
import com.rbkmoney.sink.common.serialization.impl.PaymentEventPayloadDeserializer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;

@Configuration
@EnableKafka
public class KafkaConsumerBeanEnableConfig {

    @Bean
    @ConditionalOnProperty(value = "kafka.topics.invoicing.enabled", havingValue = "true")
    public InvoicingListener invoicingListener(Parser<MachineEvent, EventPayload> paymentEventPayloadMachineEventParser,
                                               InvoiceBatchManager invoiceBatchManager,
                                               BatchService batchService,
                                               InvoiceBatchHandler<PartyData, Void> invoiceBatchHandler,
                                               InvoiceBatchHandler<PaymentPartyData, PartyData> paymentInvoiceBatchHandler,
                                               InvoiceBatchHandler<Void, PaymentPartyData> adjustmentInvoiceBatchHandler,
                                               InvoiceBatchHandler<Void, PaymentPartyData> refundInvoiceBatchHandler) {
        return new InvoicingListener(
                paymentEventPayloadMachineEventParser,
                invoiceBatchManager,
                batchService,
                invoiceBatchHandler,
                paymentInvoiceBatchHandler,
                adjustmentInvoiceBatchHandler,
                refundInvoiceBatchHandler
        );
    }

    @Bean
    @ConditionalOnProperty(value = "kafka.topics.party-management.enabled", havingValue = "true")
    public PartyManagementListener partyManagementListener(PartyManagementService partyManagementService) {
        return new PartyManagementListener(partyManagementService);
    }

    @Bean
    public BinaryDeserializer<EventPayload> paymentEventPayloadDeserializer() {
        return new PaymentEventPayloadDeserializer();
    }

    @Bean
    public MachineEventParser<EventPayload> paymentMachineEventParser(BinaryDeserializer<EventPayload> paymentEventPayloadDeserializer) {
        return new PaymentEventPayloadMachineEventParser(paymentEventPayloadDeserializer);
    }

    @Bean
    public MachineEventParser<PartyEventData> partyMachineEventParser(BinaryDeserializer<PartyEventData> partyEventDataBinaryDeserializer) {
        return new PartyEventDataMachineEventParser(partyEventDataBinaryDeserializer);
    }

    @Bean
    public BinaryDeserializer<PartyEventData> partyEventDataBinaryDeserializer() {
        return new PartyEventDataDeserializer();
    }

}
