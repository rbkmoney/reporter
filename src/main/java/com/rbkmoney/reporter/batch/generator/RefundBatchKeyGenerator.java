package com.rbkmoney.reporter.batch.generator;

import com.rbkmoney.damsel.payment_processing.InvoiceChange;
import com.rbkmoney.machinegun.eventsink.MachineEvent;
import com.rbkmoney.reporter.batch.BatchKeyGenerator;
import com.rbkmoney.reporter.batch.InvoiceBatchType;
import com.rbkmoney.reporter.batch.UniqueBatchKey;
import com.rbkmoney.reporter.batch.key.RefundInvoiceUniqueBatchKey;
import org.springframework.stereotype.Service;

@Service
public class RefundBatchKeyGenerator implements BatchKeyGenerator {

    @Override
    public UniqueBatchKey getInvoiceUniqueBatchKey(InvoiceChange invoiceChange, MachineEvent machineEvent) {
        return new RefundInvoiceUniqueBatchKey(
                machineEvent.getSourceId(),
                invoiceChange.getInvoicePaymentChange().getId(),
                invoiceChange.getInvoicePaymentChange().getPayload().getInvoicePaymentRefundChange().getId()
        );
    }

    @Override
    public boolean isChangeType(InvoiceChange invoiceChange) {
        return invoiceChange.isSetInvoicePaymentChange()
                && invoiceChange.getInvoicePaymentChange().getPayload().isSetInvoicePaymentRefundChange()
                && (invoiceChange.getInvoicePaymentChange().getPayload().getInvoicePaymentRefundChange().getPayload().isSetInvoicePaymentRefundCreated()
                || invoiceChange.getInvoicePaymentChange().getPayload().getInvoicePaymentRefundChange().getPayload().isSetInvoicePaymentRefundStatusChanged());
    }

    @Override
    public InvoiceBatchType getInvoiceBatchType() {
        return InvoiceBatchType.REFUND;
    }
}
