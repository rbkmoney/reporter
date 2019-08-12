package com.rbkmoney.reporter.dao;

import com.rbkmoney.reporter.batch.InvoiceBatchType;
import com.rbkmoney.reporter.mapper.MapperResult;
import org.jooq.Query;

public interface BatchDao {

    boolean isInvoiceChangeType(InvoiceBatchType invoiceChangeTypeEnum);

    Query getSaveEventQuery(MapperResult entity);

}
