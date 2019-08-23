package com.rbkmoney.reporter.config;

import com.rbkmoney.damsel.payment_processing.EventPayload;
import com.rbkmoney.machinegun.eventsink.MachineEvent;
import com.rbkmoney.reporter.batch.InvoiceBatchManager;
import com.rbkmoney.reporter.domain.tables.pojos.Adjustment;
import com.rbkmoney.reporter.domain.tables.pojos.Invoice;
import com.rbkmoney.reporter.domain.tables.pojos.Payment;
import com.rbkmoney.reporter.domain.tables.pojos.Refund;
import com.rbkmoney.reporter.handle.InvoiceBatchHandler;
import com.rbkmoney.reporter.listener.machineevent.PaymentEventsMessageListener;
import com.rbkmoney.reporter.service.BatchService;
import com.rbkmoney.sink.common.parser.Parser;
import com.rbkmoney.sink.common.parser.impl.MachineEventParser;
import com.rbkmoney.sink.common.parser.impl.PaymentEventPayloadMachineEventParser;
import com.rbkmoney.sink.common.serialization.BinaryDeserializer;
import com.rbkmoney.sink.common.serialization.impl.PaymentEventPayloadDeserializer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;

@Configuration
@EnableKafka
public class KafkaPaymentMachineEventConfig {

    @Bean
    public BinaryDeserializer<EventPayload> paymentEventPayloadDeserializer() {
        return new PaymentEventPayloadDeserializer();
    }

    @Bean
    public MachineEventParser<EventPayload> paymentEventPayloadMachineEventParser(BinaryDeserializer<EventPayload> paymentEventPayloadDeserializer) {
        return new PaymentEventPayloadMachineEventParser(paymentEventPayloadDeserializer);
    }

    @Bean
    @ConditionalOnProperty(value = "info.single-instance-mode", havingValue = "false")
    public PaymentEventsMessageListener paymentEventsKafkaListener(Parser<MachineEvent, EventPayload> paymentEventPayloadMachineEventParser,
                                                                   InvoiceBatchManager invoiceBatchManager,
                                                                   BatchService batchService,
                                                                   InvoiceBatchHandler<Invoice, Void> invoiceBatchHandler,
                                                                   InvoiceBatchHandler<Payment, Invoice> paymentInvoiceBatchHandler,
                                                                   InvoiceBatchHandler<Adjustment, Invoice> adjustmentInvoiceBatchHandler,
                                                                   InvoiceBatchHandler<Refund, Payment> refundInvoiceBatchHandler) {
        return new PaymentEventsMessageListener(paymentEventPayloadMachineEventParser, invoiceBatchManager, batchService, invoiceBatchHandler, paymentInvoiceBatchHandler, adjustmentInvoiceBatchHandler, refundInvoiceBatchHandler);
    }
}
