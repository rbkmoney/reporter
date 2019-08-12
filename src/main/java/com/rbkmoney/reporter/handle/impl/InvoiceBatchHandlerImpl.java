package com.rbkmoney.reporter.handle.impl;

import com.rbkmoney.reporter.batch.InvoiceBatchManager;
import com.rbkmoney.reporter.batch.InvoiceBatchService;
import com.rbkmoney.reporter.batch.InvoiceBatchType;
import com.rbkmoney.reporter.batch.InvoiceUniqueBatchKey;
import com.rbkmoney.reporter.dao.BatchDao;
import com.rbkmoney.reporter.handle.InvoiceBatchHandler;
import com.rbkmoney.reporter.mapper.InvoiceBatchMapper;
import com.rbkmoney.reporter.mapper.InvoiceChangeMapper;
import com.rbkmoney.reporter.mapper.MapperPayload;
import com.rbkmoney.reporter.mapper.MapperResult;
import lombok.RequiredArgsConstructor;
import org.jooq.Query;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.function.Predicate.not;

@RequiredArgsConstructor
public class InvoiceBatchHandlerImpl<R, C> implements InvoiceBatchHandler<R, C> {

    private final InvoiceBatchType invoiceBatchType;
    private final InvoiceBatchManager invoiceBatchManager;
    private final InvoiceBatchMapper<R, C> invoiceBatchMapper;

    @Override
    public List<Query> handle(Map<InvoiceBatchService, Map<InvoiceUniqueBatchKey, List<MapperPayload>>> mapperPayloadsByUniqueKeyByType, Function<R, MapperResult> mapperResultBuilder, Map<InvoiceUniqueBatchKey, R> producerCache, Map<InvoiceUniqueBatchKey, C> consumerCache) {
        Map.Entry<InvoiceBatchService, Map<InvoiceUniqueBatchKey, List<MapperPayload>>> mapperPayloadsByUniqueKeyEntry = mapperPayloadsByUniqueKeyByType.entrySet().stream()
                .filter(entry -> entry.getKey().getInvoiceBatchType().equals(invoiceBatchType))
                .findFirst()
                .orElse(null);

        if (mapperPayloadsByUniqueKeyEntry != null) {
            return mapperPayloadsByUniqueKeyEntry.getValue().entrySet().stream()
                    .map(
                            entry -> {
                                List<R> invoicingEntities = new ArrayList<>();

                                List<MapperPayload> uniqueMapperPayloads = entry.getValue();
                                uniqueMapperPayloads.forEach(
                                        payload -> {
                                            InvoiceChangeMapper mapper = invoiceBatchManager.getInvoiceChangeMapper(payload.getInvoiceChange());

                                            R invoicingEntity;
                                            if (consumerCache != null) {
                                                invoicingEntity = invoiceBatchMapper.map(mapper, payload, invoicingEntities, consumerCache, entry.getKey());
                                            } else {
                                                invoicingEntity = invoiceBatchMapper.map(mapper, payload, invoicingEntities);
                                            }

                                            invoicingEntities.add(invoicingEntity);
                                        }
                                );

                                return Map.entry(entry.getKey(), invoicingEntities);
                            }
                    )
                    .filter(not(entry -> entry.getValue().isEmpty()))
                    .peek(
                            entry -> {
                                if (producerCache != null) {
                                    producerCache.put(entry.getKey(), entry.getValue().get(entry.getValue().size() - 1));
                                }
                            }
                    )
                    .flatMap(
                            entry -> entry.getValue().stream()
                                    .map(
                                            invoicingEntity -> {
                                                BatchDao dao = invoiceBatchManager.getBatchDao(invoiceBatchType);
                                                return dao.getSaveEventQuery(mapperResultBuilder.apply(invoicingEntity));
                                            }
                                    )
                    )
                    .collect(Collectors.toList());
        }

        return Collections.emptyList();
    }
}
