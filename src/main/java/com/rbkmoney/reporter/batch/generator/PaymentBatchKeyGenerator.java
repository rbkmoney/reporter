package com.rbkmoney.reporter.batch.generator;

import com.rbkmoney.damsel.payment_processing.InvoiceChange;
import com.rbkmoney.machinegun.eventsink.MachineEvent;
import com.rbkmoney.reporter.batch.BatchKeyGenerator;
import com.rbkmoney.reporter.batch.InvoiceBatchType;
import com.rbkmoney.reporter.batch.UniqueBatchKey;
import com.rbkmoney.reporter.batch.key.PaymentInvoiceUniqueBatchKey;
import org.springframework.stereotype.Service;

@Service
public class PaymentBatchKeyGenerator implements BatchKeyGenerator {

    @Override
    public UniqueBatchKey getInvoiceUniqueBatchKey(InvoiceChange invoiceChange, MachineEvent machineEvent) {
        return new PaymentInvoiceUniqueBatchKey(
                machineEvent.getSourceId(),
                invoiceChange.getInvoicePaymentChange().getId()
        );
    }

    @Override
    public boolean isChangeType(InvoiceChange invoiceChange) {
        return invoiceChange.isSetInvoicePaymentChange()
                && (invoiceChange.getInvoicePaymentChange().getPayload().isSetInvoicePaymentCashFlowChanged()
                || invoiceChange.getInvoicePaymentChange().getPayload().isSetInvoicePaymentRouteChanged()
                || invoiceChange.getInvoicePaymentChange().getPayload().isSetInvoicePaymentStarted()
                || invoiceChange.getInvoicePaymentChange().getPayload().isSetInvoicePaymentStatusChanged()
                || (invoiceChange.getInvoicePaymentChange().getPayload().isSetInvoicePaymentSessionChange()
                && invoiceChange.getInvoicePaymentChange().getPayload().getInvoicePaymentSessionChange().getPayload().isSetSessionInteractionRequested()
                && invoiceChange.getInvoicePaymentChange().getPayload().getInvoicePaymentSessionChange().getPayload().getSessionInteractionRequested().getInteraction().isSetPaymentTerminalReciept()));
    }

    @Override
    public InvoiceBatchType getInvoiceBatchType() {
        return InvoiceBatchType.PAYMENT;
    }
}
