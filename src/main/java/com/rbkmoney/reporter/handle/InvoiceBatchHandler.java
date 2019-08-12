package com.rbkmoney.reporter.handle;

import com.rbkmoney.reporter.batch.InvoiceBatchService;
import com.rbkmoney.reporter.batch.InvoiceUniqueBatchKey;
import com.rbkmoney.reporter.mapper.MapperPayload;
import com.rbkmoney.reporter.mapper.MapperResult;
import org.jooq.Query;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

public interface InvoiceBatchHandler<R, C> {

    List<Query> handle(Map<InvoiceBatchService, Map<InvoiceUniqueBatchKey, List<MapperPayload>>> mapperPayloadsByUniqueKeyByType, Function<R, MapperResult> mapperResultBuilder, Map<InvoiceUniqueBatchKey, R> producerCache, Map<InvoiceUniqueBatchKey, C> consumerCache);

}
