package com.rbkmoney.reporter.batch.key;

import com.rbkmoney.reporter.batch.UniqueBatchKey;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AdjustmentInvoiceUniqueBatchKey implements UniqueBatchKey {

    private String invoiceId;
    private String paymentId;
    private String adjustmentId;

}
