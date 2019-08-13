package com.rbkmoney.reporter.batch.impl;

import com.rbkmoney.reporter.batch.InvoiceUniqueBatchKey;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PaymentInvoiceUniqueBatchKey implements InvoiceUniqueBatchKey {

    private String invoiceId;
    private String paymentId;

}
