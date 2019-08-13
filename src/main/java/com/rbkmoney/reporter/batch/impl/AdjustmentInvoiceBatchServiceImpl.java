package com.rbkmoney.reporter.batch.impl;

import com.rbkmoney.damsel.payment_processing.InvoiceChange;
import com.rbkmoney.machinegun.eventsink.MachineEvent;
import com.rbkmoney.reporter.batch.InvoiceBatchService;
import com.rbkmoney.reporter.batch.InvoiceBatchType;
import com.rbkmoney.reporter.batch.InvoiceUniqueBatchKey;
import org.springframework.stereotype.Service;

@Service
public class AdjustmentInvoiceBatchServiceImpl implements InvoiceBatchService {

    @Override
    public boolean isChangeType(InvoiceChange invoiceChange) {
        return invoiceChange.isSetInvoicePaymentChange()
                && invoiceChange.getInvoicePaymentChange().getPayload().isSetInvoicePaymentAdjustmentChange();
    }

    @Override
    public boolean isCreatedChange(InvoiceChange invoiceChange) {
        return invoiceChange.isSetInvoicePaymentChange()
                && invoiceChange.getInvoicePaymentChange().getPayload().isSetInvoicePaymentAdjustmentChange()
                && invoiceChange.getInvoicePaymentChange().getPayload().getInvoicePaymentAdjustmentChange().getPayload().isSetInvoicePaymentAdjustmentCreated();
    }

    @Override
    public InvoiceBatchType getInvoiceBatchType() {
        return InvoiceBatchType.ADJUSTMENT;
    }

    @Override
    public InvoiceUniqueBatchKey getInvoiceUniqueBatchKey(InvoiceChange invoiceChange, MachineEvent machineEvent) {
        return new AdjustmentInvoiceUniqueBatchKey(
                machineEvent.getSourceId(),
                invoiceChange.getInvoicePaymentChange().getId(),
                invoiceChange.getInvoicePaymentChange().getPayload().getInvoicePaymentAdjustmentChange().getId()
        );
    }
}
