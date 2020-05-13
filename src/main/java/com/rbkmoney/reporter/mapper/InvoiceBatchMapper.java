package com.rbkmoney.reporter.mapper;

import com.rbkmoney.reporter.batch.UniqueBatchKey;
import org.jooq.Query;

import java.util.List;
import java.util.Map;

public interface InvoiceBatchMapper<P, C> {

    List<Query> map(InvoiceChangeMapper mapper, MapperPayload payload, Map<UniqueBatchKey, P> producerCache, Map<UniqueBatchKey, C> consumerCache);

}
