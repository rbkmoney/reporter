package com.rbkmoney.reporter.mapper;

import com.rbkmoney.reporter.batch.InvoiceUniqueBatchKey;

import java.util.List;
import java.util.Map;

public interface InvoiceBatchMapper<R, C> {

    R map(InvoiceChangeMapper mapper, MapperPayload payload, List<R> invoicingEntities) throws Exception;

    R map(InvoiceChangeMapper mapper, MapperPayload payload, List<R> invoicingEntities, Map<InvoiceUniqueBatchKey, C> consumerCache);

}
