package com.rbkmoney.reporter.batch.generator;

import com.rbkmoney.damsel.payment_processing.InvoiceChange;
import com.rbkmoney.machinegun.eventsink.MachineEvent;
import com.rbkmoney.reporter.batch.BatchKeyGenerator;
import com.rbkmoney.reporter.batch.InvoiceBatchType;
import com.rbkmoney.reporter.batch.UniqueBatchKey;
import com.rbkmoney.reporter.batch.key.AdjustmentInvoiceUniqueBatchKey;
import org.springframework.stereotype.Service;

@Service
public class AdjustmentBatchKeyGenerator implements BatchKeyGenerator {

    @Override
    public UniqueBatchKey getInvoiceUniqueBatchKey(InvoiceChange invoiceChange, MachineEvent machineEvent) {
        return new AdjustmentInvoiceUniqueBatchKey(
                machineEvent.getSourceId(),
                invoiceChange.getInvoicePaymentChange().getId(),
                invoiceChange.getInvoicePaymentChange().getPayload().getInvoicePaymentAdjustmentChange().getId()
        );
    }

    @Override
    public boolean isChangeType(InvoiceChange invoiceChange) {
        return invoiceChange.isSetInvoicePaymentChange()
                && invoiceChange.getInvoicePaymentChange().getPayload().isSetInvoicePaymentAdjustmentChange()
                && (invoiceChange.getInvoicePaymentChange().getPayload().getInvoicePaymentAdjustmentChange().getPayload().isSetInvoicePaymentAdjustmentCreated()
                || invoiceChange.getInvoicePaymentChange().getPayload().getInvoicePaymentAdjustmentChange().getPayload().isSetInvoicePaymentAdjustmentStatusChanged());
    }

    @Override
    public InvoiceBatchType getInvoiceBatchType() {
        return InvoiceBatchType.ADJUSTMENT;
    }
}
