package com.rbkmoney.reporter.mapper;

import com.rbkmoney.reporter.domain.tables.pojos.*;
import lombok.Data;

@Data
public class MapperResult {

    private Adjustment adjustment;
    private AdjustmentState adjustmentState;

    private Invoice invoice;
    private InvoiceState invoiceState;

    private Payment payment;
    private PaymentCost paymentCost;
    private PaymentState paymentState;
    private PaymentRouting paymentRouting;
    private PaymentTerminalReceipt paymentTerminalReceipt;
    private PaymentFee paymentFee;

    private Refund refund;
    private RefundState refundState;

    public MapperResult(Invoice invoice, InvoiceState invoiceState) {
        this.invoice = invoice;
        this.invoiceState = invoiceState;
    }

    public MapperResult(Payment payment,
                        PaymentState paymentState,
                        PaymentCost paymentCost,
                        PaymentRouting paymentRouting,
                        PaymentFee paymentFee) {
        this.payment = payment;
        this.paymentState = paymentState;
        this.paymentCost = paymentCost;
        this.paymentRouting = paymentRouting;
        this.paymentFee = paymentFee;
    }

    public MapperResult(Adjustment adjustment, AdjustmentState adjustmentState) {
        this.adjustment = adjustment;
        this.adjustmentState = adjustmentState;
    }

    public MapperResult(Refund refund, RefundState refundState) {
        this.refund = refund;
        this.refundState = refundState;
    }

    public MapperResult(InvoiceState invoiceState) {
        this.invoiceState = invoiceState;
    }

    public MapperResult(PaymentState paymentState, PaymentCost paymentCost) {
        this.paymentState = paymentState;
        this.paymentCost = paymentCost;
    }

    public MapperResult(PaymentCost paymentCost) {
        this.paymentCost = paymentCost;
    }

    public MapperResult(PaymentCost paymentCost, PaymentFee paymentFee) {
        this.paymentCost = paymentCost;
        this.paymentFee = paymentFee;
    }

    public MapperResult(PaymentRouting paymentRouting) {
        this.paymentRouting = paymentRouting;
    }

    public MapperResult(PaymentTerminalReceipt paymentTerminalReceipt) {
        this.paymentTerminalReceipt = paymentTerminalReceipt;
    }

    public MapperResult(AdjustmentState adjustmentState) {
        this.adjustmentState = adjustmentState;
    }

    public MapperResult(RefundState refundState) {
        this.refundState = refundState;
    }
}
