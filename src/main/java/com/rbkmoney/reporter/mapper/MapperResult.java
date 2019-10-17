package com.rbkmoney.reporter.mapper;

import com.rbkmoney.reporter.domain.tables.pojos.*;
import lombok.Data;

import java.util.List;

@Data
public class MapperResult {

    private Adjustment adjustment;
    private AdjustmentState adjustmentState;

    private Invoice invoice;
    private InvoiceState invoiceState;

    private Payment payment;
    private PaymentCost paymentCost;
    private PaymentState paymentState;
    private List<CashFlow> cashFlowList;
    private PaymentRouting paymentRouting;
    private PaymentShortId paymentShortId;

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
                        List<CashFlow> cashFlowList) {
        this.payment = payment;
        this.paymentState = paymentState;
        this.paymentCost = paymentCost;
        this.paymentRouting = paymentRouting;
        this.cashFlowList = cashFlowList;
    }

    public MapperResult(Payment payment, PaymentCost paymentCost) {
        this.payment = payment;
        this.paymentCost = paymentCost;
    }

    public MapperResult(Adjustment adjustment, AdjustmentState adjustmentState) {
        this.adjustment = adjustment;
        this.adjustmentState = adjustmentState;
    }

    public MapperResult(Refund refund, RefundState refundState, List<CashFlow> cashFlowList) {
        this.refund = refund;
        this.refundState = refundState;
        this.cashFlowList = cashFlowList;
    }

    public MapperResult(AdjustmentState adjustmentState) {
        this.adjustmentState = adjustmentState;
    }

    public MapperResult(PaymentRouting paymentRouting) {
        this.paymentRouting = paymentRouting;
    }

    public MapperResult(InvoiceState invoiceState) {
        this.invoiceState = invoiceState;
    }

    public MapperResult(PaymentState paymentState) {
        this.paymentState = paymentState;
    }

    public MapperResult(RefundState refundState) {
        this.refundState = refundState;
    }

    public MapperResult(PaymentCost paymentCost) {
        this.paymentCost = paymentCost;
    }

    public MapperResult(PaymentShortId paymentShortId) {
        this.paymentShortId = paymentShortId;
    }

    public MapperResult(PaymentState paymentState, PaymentCost paymentCost) {
        this.paymentState = paymentState;
        this.paymentCost = paymentCost;
    }

    public MapperResult(List<CashFlow> cashFlowList) {
        this.cashFlowList = cashFlowList;
    }

}
