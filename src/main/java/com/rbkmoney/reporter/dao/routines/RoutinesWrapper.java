package com.rbkmoney.reporter.dao.routines;

import com.rbkmoney.reporter.domain.Routines;
import com.rbkmoney.reporter.domain.tables.records.AdjustmentRecord;
import com.rbkmoney.reporter.domain.tables.records.PaymentRecord;
import com.rbkmoney.reporter.domain.tables.records.PayoutRecord;
import com.rbkmoney.reporter.domain.tables.records.RefundRecord;
import org.jooq.AggregateFunction;
import org.jooq.Field;
import org.jooq.impl.DSL;

import static com.rbkmoney.reporter.dao.mapper.PaymentRegistryReportDataRowMapper.*;
import static com.rbkmoney.reporter.dao.mapper.RefundPaymentRegistryReportDataRowMapper.refundAmount;
import static com.rbkmoney.reporter.domain.tables.Payment.PAYMENT;
import static com.rbkmoney.reporter.domain.tables.Refund.REFUND;

public class RoutinesWrapper {

    public static AggregateFunction<Long> getPaymentAmount() {
        return com.rbkmoney.reporter.domain.Routines.getPaymentAmount(DSL.field("rpt.payment.*", PaymentRecord.class));
    }

    public static AggregateFunction<Long> getPaymentFee() {
        return com.rbkmoney.reporter.domain.Routines.getPaymentFee(DSL.field("rpt.payment.*", PaymentRecord.class));
    }

    public static AggregateFunction<Long> getAdjustmentFee() {
        return com.rbkmoney.reporter.domain.Routines.getAdjustmentFee(DSL.field("rpt.adjustment.*", AdjustmentRecord.class));
    }

    public static AggregateFunction<Long> getRefundAmount() {
        return com.rbkmoney.reporter.domain.Routines.getRefundAmount(DSL.field("rpt.refund.*", RefundRecord.class));
    }

    public static AggregateFunction<Long> getRefundFee() {
        return com.rbkmoney.reporter.domain.Routines.getRefundFee(DSL.field("rpt.refund.*", RefundRecord.class));
    }

    public static AggregateFunction<Long> getPayoutAmount() {
        return com.rbkmoney.reporter.domain.Routines.getPayoutAmount(DSL.field("rpt.payout.*", PayoutRecord.class));
    }

    public static AggregateFunction<Long> getPayoutFee() {
        return com.rbkmoney.reporter.domain.Routines.getPayoutFee(DSL.field("rpt.payout.*", PayoutRecord.class));
    }

    public static Field<Long> getPaymentCashFlowAmount() {
        return Routines.getCashFlowAmount(PAYMENT.PAYMENT_CASH_FLOW, PAYMENT.PAYMENT_AMOUNT).as(paymentAmount);
    }

    public static Field<Long> getPaymentCashFlowFee() {
        return Routines.getCashFlowFee(PAYMENT.PAYMENT_CASH_FLOW, defaultValue).as(paymentFee);
    }

    public static Field<Long> getPaymentCashFlowProviderFee() {
        return Routines.getCashFlowProviderFee(PAYMENT.PAYMENT_CASH_FLOW, defaultValue).as(paymentProviderFee);
    }

    public static Field<Long> getPaymentCashFlowExternalFee() {
        return Routines.getCashFlowExternalFee(PAYMENT.PAYMENT_CASH_FLOW, defaultValue).as(paymentExternalFee);
    }

    public static Field<Long> getRefundCashFlowAmount() {
        return Routines.getCashFlowAmount(REFUND.REFUND_CASH_FLOW, REFUND.REFUND_AMOUNT).as(refundAmount);
    }
}
