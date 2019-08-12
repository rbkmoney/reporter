package com.rbkmoney.reporter.batch;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class InvoiceUniqueBatchKey {

    private String invoiceId;
    private String paymentId;
    private String adjustmentId;
    private String refundId;

}
