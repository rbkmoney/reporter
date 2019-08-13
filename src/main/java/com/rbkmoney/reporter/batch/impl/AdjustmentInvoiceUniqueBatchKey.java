package com.rbkmoney.reporter.batch.impl;

import com.rbkmoney.reporter.batch.InvoiceUniqueBatchKey;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AdjustmentInvoiceUniqueBatchKey implements InvoiceUniqueBatchKey {

    private String invoiceId;
    private String paymentId;
    private String adjustmentId;

}
