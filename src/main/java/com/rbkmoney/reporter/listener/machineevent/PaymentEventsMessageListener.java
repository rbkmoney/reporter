package com.rbkmoney.reporter.listener.machineevent;

import com.rbkmoney.damsel.payment_processing.EventPayload;
import com.rbkmoney.machinegun.eventsink.MachineEvent;
import com.rbkmoney.machinegun.eventsink.SinkEvent;
import com.rbkmoney.reporter.batch.InvoiceBatchManager;
import com.rbkmoney.reporter.batch.InvoiceBatchService;
import com.rbkmoney.reporter.batch.InvoiceUniqueBatchKey;
import com.rbkmoney.reporter.domain.tables.pojos.Adjustment;
import com.rbkmoney.reporter.domain.tables.pojos.Invoice;
import com.rbkmoney.reporter.domain.tables.pojos.Payment;
import com.rbkmoney.reporter.domain.tables.pojos.Refund;
import com.rbkmoney.reporter.handle.InvoiceBatchHandler;
import com.rbkmoney.reporter.mapper.MapperPayload;
import com.rbkmoney.reporter.mapper.MapperResult;
import com.rbkmoney.reporter.service.BatchService;
import com.rbkmoney.sink.common.parser.Parser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.jooq.Query;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.rbkmoney.kafka.common.util.LogUtil.toSummaryStringWithSinkEventValues;

@Slf4j
@RequiredArgsConstructor
public class PaymentEventsMessageListener {

    private final Parser<MachineEvent, EventPayload> paymentEventPayloadMachineEventParser;
    private final InvoiceBatchManager invoiceBatchManager;
    private final BatchService batchService;

    private final InvoiceBatchHandler<Invoice, Void> invoiceBatchHandler;
    private final InvoiceBatchHandler<Payment, Invoice> paymentInvoiceBatchHandler;
    private final InvoiceBatchHandler<Adjustment, Payment> adjustmentInvoiceBatchHandler;
    private final InvoiceBatchHandler<Refund, Payment> refundInvoiceBatchHandler;

    @KafkaListener(topics = "${kafka.topics.invoice.id}", containerFactory = "kafkaListenerContainerFactory")
    public void listen(List<ConsumerRecord<String, SinkEvent>> messages, Acknowledgment ack) {
        log.info("Got machineEvent batch with size: {}", messages.size());

        Map<InvoiceBatchService, Map<InvoiceUniqueBatchKey, List<MapperPayload>>> mapperPayloadsByUniqueKeyByType = getMapperPayloadsByUniqueKeyByType(messages);

        List<Query> saveEventQueries = new ArrayList<>();

        Map<InvoiceUniqueBatchKey, Invoice> invoiceCache = new HashMap<>();
        Map<InvoiceUniqueBatchKey, Payment> paymentCache = new HashMap<>();

        List<Query> invoiceSaveEventQueries = invoiceBatchHandler.handle(mapperPayloadsByUniqueKeyByType, MapperResult::new, invoiceCache, null);
        List<Query> paymentEventQueries = paymentInvoiceBatchHandler.handle(mapperPayloadsByUniqueKeyByType, MapperResult::new, paymentCache, invoiceCache);
        List<Query> adjustmentEventQueries = adjustmentInvoiceBatchHandler.handle(mapperPayloadsByUniqueKeyByType, MapperResult::new, null, paymentCache);
        List<Query> refundEventQueries = refundInvoiceBatchHandler.handle(mapperPayloadsByUniqueKeyByType, MapperResult::new, null, paymentCache);

        saveEventQueries.addAll(invoiceSaveEventQueries);
        saveEventQueries.addAll(paymentEventQueries);
        saveEventQueries.addAll(adjustmentEventQueries);
        saveEventQueries.addAll(refundEventQueries);

        if (!saveEventQueries.isEmpty()) {
            batchService.save(saveEventQueries);
            log.info("Batch has been committed, size={}, {}", messages.size(), toSummaryStringWithSinkEventValues(messages));
        } else {
            log.info("Batch is empty, size={}, {}", messages.size(), toSummaryStringWithSinkEventValues(messages));
        }

        ack.acknowledge();
    }

    private Map<InvoiceBatchService, Map<InvoiceUniqueBatchKey, List<MapperPayload>>> getMapperPayloadsByUniqueKeyByType(List<ConsumerRecord<String, SinkEvent>> messages) {
        return messages.stream()
                .map(ConsumerRecord::value)
                .map(sinkEvent -> Map.entry(sinkEvent.getEvent(), paymentEventPayloadMachineEventParser.parse(sinkEvent.getEvent())))
                .filter(entry -> entry.getValue().isSetInvoiceChanges())
                .map(
                        entry -> IntStream.range(0, entry.getValue().getInvoiceChanges().size())
                                .mapToObj(i -> new MapperPayload(entry.getKey(), entry.getValue().getInvoiceChanges().get(i), i))
                                .collect(Collectors.toList())
                )
                .flatMap(List::stream)
                .collect(Collectors.groupingBy(mapperPayload -> invoiceBatchManager.getInvoiceBatchService(mapperPayload.getInvoiceChange()))).entrySet().stream()
                .map(
                        typeEntry -> {
                            Map<InvoiceUniqueBatchKey, List<MapperPayload>> mapperPayloadsByUniqueKey = typeEntry.getValue().stream()
                                    .sorted(Comparator.comparing(mapperPayload -> mapperPayload.getMachineEvent().getEventId()))
                                    .collect(Collectors.groupingBy(mapperPayload -> typeEntry.getKey().getInvoiceUniqueBatchKey(mapperPayload.getInvoiceChange(), mapperPayload.getMachineEvent())));
                            return Map.entry(typeEntry.getKey(), mapperPayloadsByUniqueKey);
                        }
                )
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }
}
