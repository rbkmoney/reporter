package com.rbkmoney.reporter.batch.key;

import com.rbkmoney.reporter.batch.UniqueBatchKey;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class InvoiceUniqueBatchKeyImpl implements UniqueBatchKey {

    private String invoiceId;

}
