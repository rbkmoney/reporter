package com.rbkmoney.reporter.batch.key;

import com.rbkmoney.reporter.batch.UniqueBatchKey;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PaymentInvoiceUniqueBatchKey implements UniqueBatchKey {

    private String invoiceId;
    private String paymentId;

}
