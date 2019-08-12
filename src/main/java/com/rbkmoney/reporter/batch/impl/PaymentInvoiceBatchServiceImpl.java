package com.rbkmoney.reporter.batch.impl;

import com.rbkmoney.damsel.payment_processing.InvoiceChange;
import com.rbkmoney.machinegun.eventsink.MachineEvent;
import com.rbkmoney.reporter.batch.InvoiceBatchService;
import com.rbkmoney.reporter.batch.InvoiceBatchType;
import com.rbkmoney.reporter.batch.InvoiceUniqueBatchKey;
import org.springframework.stereotype.Service;

@Service
public class PaymentInvoiceBatchServiceImpl implements InvoiceBatchService {

    @Override
    public boolean isChangeType(InvoiceChange invoiceChange) {
        return invoiceChange.isSetInvoicePaymentChange()
                && (invoiceChange.getInvoicePaymentChange().getPayload().isSetInvoicePaymentCashFlowChanged()
                || invoiceChange.getInvoicePaymentChange().getPayload().isSetInvoicePaymentRouteChanged()
                || invoiceChange.getInvoicePaymentChange().getPayload().isSetInvoicePaymentStarted()
                || invoiceChange.getInvoicePaymentChange().getPayload().isSetInvoicePaymentStatusChanged()
                || invoiceChange.getInvoicePaymentChange().getPayload().isSetInvoicePaymentSessionChange());
    }

    @Override
    public boolean isCreatedChange(InvoiceChange invoiceChange) {
        return invoiceChange.isSetInvoicePaymentChange()
                && invoiceChange.getInvoicePaymentChange().getPayload().isSetInvoicePaymentStarted();
    }

    @Override
    public InvoiceBatchType getInvoiceBatchType() {
        return InvoiceBatchType.PAYMENT;
    }

    @Override
    public InvoiceUniqueBatchKey getInvoiceUniqueBatchKey(InvoiceChange invoiceChange, MachineEvent machineEvent) {
        return new InvoiceUniqueBatchKey(
                machineEvent.getSourceId(),
                invoiceChange.getInvoicePaymentChange().getId(),
                null,
                null
        );
    }
}
