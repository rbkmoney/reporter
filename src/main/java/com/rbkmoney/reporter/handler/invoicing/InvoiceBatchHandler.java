package com.rbkmoney.reporter.handler.invoicing;

import com.rbkmoney.reporter.batch.BatchKeyGenerator;
import com.rbkmoney.reporter.batch.UniqueBatchKey;
import com.rbkmoney.reporter.mapper.MapperPayload;
import org.jooq.Query;

import java.util.List;
import java.util.Map;

public interface InvoiceBatchHandler<P, C> {

    List<Query> handle(Map<BatchKeyGenerator, Map<UniqueBatchKey, List<MapperPayload>>> mapperPayloadsByUniqueKeyByType, Map<UniqueBatchKey, P> producerCache, Map<UniqueBatchKey, C> consumerCache);

}
