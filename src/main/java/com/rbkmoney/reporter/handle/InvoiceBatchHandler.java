package com.rbkmoney.reporter.handle;

import com.rbkmoney.reporter.batch.InvoiceBatchService;
import com.rbkmoney.reporter.batch.InvoiceUniqueBatchKey;
import com.rbkmoney.reporter.mapper.MapperPayload;
import org.jooq.Query;

import java.util.List;
import java.util.Map;

public interface InvoiceBatchHandler<P, C> {

    List<Query> handle(Map<InvoiceBatchService, Map<InvoiceUniqueBatchKey, List<MapperPayload>>> mapperPayloadsByUniqueKeyByType, Map<InvoiceUniqueBatchKey, P> producerCache, Map<InvoiceUniqueBatchKey, C> consumerCache);

}
