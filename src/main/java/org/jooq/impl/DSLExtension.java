package org.jooq.impl;

import org.jooq.AggregateFunction;
import org.jooq.Support;

import static com.rbkmoney.reporter.domain.tables.Adjustment.ADJUSTMENT;
import static com.rbkmoney.reporter.domain.tables.Payment.PAYMENT;
import static com.rbkmoney.reporter.domain.tables.Payout.PAYOUT;
import static com.rbkmoney.reporter.domain.tables.Refund.REFUND;

public class DSLExtension {

    @Support
    public static AggregateFunction<Long> getPaymentAmount() {
        return new org.jooq.impl.Function<>("rpt.get_payment_amount", SQLDataType.BIGINT, PAYMENT.asterisk());
    }

    @Support
    public static AggregateFunction<Long> getPaymentFee() {
        return new org.jooq.impl.Function<>("rpt.get_payment_fee", SQLDataType.BIGINT, PAYMENT.asterisk());
    }

    @Support
    public static AggregateFunction<Long> getAdjustmentFee() {
        return new org.jooq.impl.Function<>("rpt.get_adjustment_fee", SQLDataType.BIGINT, ADJUSTMENT.asterisk());
    }

    @Support
    public static AggregateFunction<Long> getRefundAmount() {
        return new org.jooq.impl.Function<>("rpt.get_refund_amount", SQLDataType.BIGINT, REFUND.asterisk());
    }

    @Support
    public static AggregateFunction<Long> getRefundFee() {
        return new org.jooq.impl.Function<>("rpt.get_refund_fee", SQLDataType.BIGINT, REFUND.asterisk());
    }

    @Support
    public static AggregateFunction<Long> getPayoutAmount() {
        return new org.jooq.impl.Function<>("rpt.get_payout_amount", SQLDataType.BIGINT, PAYOUT.asterisk());
    }

    @Support
    public static AggregateFunction<Long> getPayoutFee() {
        return new org.jooq.impl.Function<>("rpt.get_payout_fee", SQLDataType.BIGINT, PAYOUT.asterisk());
    }
}
