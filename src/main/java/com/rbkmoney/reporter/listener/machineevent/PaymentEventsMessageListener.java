package com.rbkmoney.reporter.listener.machineevent;

import com.rbkmoney.damsel.payment_processing.EventPayload;
import com.rbkmoney.machinegun.eventsink.MachineEvent;
import com.rbkmoney.machinegun.eventsink.SinkEvent;
import com.rbkmoney.reporter.batch.InvoiceBatchManager;
import com.rbkmoney.reporter.batch.InvoiceBatchService;
import com.rbkmoney.reporter.batch.InvoiceBatchType;
import com.rbkmoney.reporter.batch.InvoiceUniqueBatchKey;
import com.rbkmoney.reporter.dao.mapper.dto.PartyData;
import com.rbkmoney.reporter.dao.mapper.dto.PaymentPartyData;
import com.rbkmoney.reporter.handle.InvoiceBatchHandler;
import com.rbkmoney.reporter.mapper.MapperPayload;
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
import static java.util.function.Predicate.not;

@Slf4j
@RequiredArgsConstructor
public class PaymentEventsMessageListener {

    private final Parser<MachineEvent, EventPayload> paymentEventPayloadMachineEventParser;
    private final InvoiceBatchManager invoiceBatchManager;
    private final BatchService batchService;

    private final InvoiceBatchHandler<PartyData, Void> invoiceBatchHandler;
    private final InvoiceBatchHandler<PaymentPartyData, PartyData> paymentInvoiceBatchHandler;
    private final InvoiceBatchHandler<Void, PaymentPartyData> adjustmentInvoiceBatchHandler;
    private final InvoiceBatchHandler<Void, PaymentPartyData> refundInvoiceBatchHandler;

    @KafkaListener(topics = "${kafka.topics.invoice.id}", containerFactory = "kafkaListenerContainerFactory")
    public void listen(List<ConsumerRecord<String, SinkEvent>> messages, Acknowledgment ack) {
        String recordInfo = toSummaryStringWithSinkEventValues(messages);
        int size = messages.size();

        log.info("Start handling batch with size:, size={}, {}", size, recordInfo);

        Map<InvoiceBatchService, Map<InvoiceUniqueBatchKey, List<MapperPayload>>> mapperPayloadsByUniqueKeyByType = getMapperPayloadsByUniqueKeyByType(messages);

        List<Query> saveEventQueries = new ArrayList<>();

        Map<InvoiceUniqueBatchKey, PartyData> partyDataCache = new HashMap<>();
        Map<InvoiceUniqueBatchKey, PaymentPartyData> paymentPartyDataCache = new HashMap<>();

        List<Query> invoiceSaveEventQueries = invoiceBatchHandler.handle(mapperPayloadsByUniqueKeyByType, partyDataCache, null);
        List<Query> paymentEventQueries = paymentInvoiceBatchHandler.handle(mapperPayloadsByUniqueKeyByType, paymentPartyDataCache, partyDataCache);
        List<Query> adjustmentEventQueries = adjustmentInvoiceBatchHandler.handle(mapperPayloadsByUniqueKeyByType, null, paymentPartyDataCache);
        List<Query> refundEventQueries = refundInvoiceBatchHandler.handle(mapperPayloadsByUniqueKeyByType, null, paymentPartyDataCache);

        saveEventQueries.addAll(invoiceSaveEventQueries);
        saveEventQueries.addAll(paymentEventQueries);
        saveEventQueries.addAll(adjustmentEventQueries);
        saveEventQueries.addAll(refundEventQueries);

        if (!saveEventQueries.isEmpty()) {
            batchService.save(saveEventQueries);
            log.info("Batch has been committed, size={}, {}", size, recordInfo);
        } else {
            log.info("Batch is empty, size={}, {}", size, recordInfo);
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
                .collect(
                        Collectors.groupingBy(
                                mapperPayload -> invoiceBatchManager.getInvoiceBatchService(mapperPayload.getInvoiceChange())
                        )
                )
                .entrySet().stream()
                .filter(not(typeEntry -> typeEntry.getKey().getInvoiceBatchType().equals(InvoiceBatchType.OTHER)))
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
