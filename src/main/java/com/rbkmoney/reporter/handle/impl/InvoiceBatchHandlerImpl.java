package com.rbkmoney.reporter.handle.impl;

import com.rbkmoney.reporter.batch.InvoiceBatchManager;
import com.rbkmoney.reporter.batch.InvoiceBatchService;
import com.rbkmoney.reporter.batch.InvoiceBatchType;
import com.rbkmoney.reporter.batch.InvoiceUniqueBatchKey;
import com.rbkmoney.reporter.handle.InvoiceBatchHandler;
import com.rbkmoney.reporter.mapper.InvoiceBatchMapper;
import com.rbkmoney.reporter.mapper.InvoiceChangeMapper;
import com.rbkmoney.reporter.mapper.MapperPayload;
import lombok.RequiredArgsConstructor;
import org.jooq.Query;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.function.Predicate.not;

@RequiredArgsConstructor
public class InvoiceBatchHandlerImpl<P, C> implements InvoiceBatchHandler<P, C> {

    private final InvoiceBatchType invoiceBatchType;
    private final InvoiceBatchManager invoiceBatchManager;
    private final InvoiceBatchMapper<P, C> invoiceBatchMapper;

    @Override
    public List<Query> handle(Map<InvoiceBatchService, Map<InvoiceUniqueBatchKey, List<MapperPayload>>> mapperPayloadsByUniqueKeyByType, Map<InvoiceUniqueBatchKey, P> producerCache, Map<InvoiceUniqueBatchKey, C> consumerCache) {
        Map.Entry<InvoiceBatchService, Map<InvoiceUniqueBatchKey, List<MapperPayload>>> mapperPayloadsByUniqueKeyEntry = mapperPayloadsByUniqueKeyByType.entrySet().stream()
                .filter(entry -> entry.getKey().getInvoiceBatchType().equals(invoiceBatchType))
                .findFirst()
                .orElse(null);

        if (mapperPayloadsByUniqueKeyEntry != null) {
            return mapperPayloadsByUniqueKeyEntry.getValue().entrySet().stream()
                    .map(
                            entry -> {
                                List<Query> queries = new ArrayList<>();

                                List<MapperPayload> uniqueMapperPayloads = entry.getValue();

                                uniqueMapperPayloads.forEach(
                                        payload -> {
                                            InvoiceChangeMapper mapper = invoiceBatchManager.getInvoiceChangeMapper(payload.getInvoiceChange());

                                            queries.addAll(invoiceBatchMapper.map(mapper, payload, producerCache, consumerCache));
                                        }
                                );

                                return Map.entry(entry.getKey(), queries);
                            }
                    )
                    .filter(not(entry -> entry.getValue().isEmpty()))
                    .flatMap(entry -> entry.getValue().stream())
                    .collect(Collectors.toList());
        }

        return List.of();
    }
}
