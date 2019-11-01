package com.rbkmoney.reporter.dao.query.impl;

import com.rbkmoney.dao.impl.AbstractGenericDao;
import com.rbkmoney.reporter.dao.query.PaymentQueryTemplator;
import com.rbkmoney.reporter.domain.tables.pojos.*;
import com.rbkmoney.reporter.domain.tables.records.*;
import com.zaxxer.hikari.HikariDataSource;
import org.jooq.Query;
import org.springframework.stereotype.Component;

import static com.rbkmoney.reporter.domain.Tables.*;

@Component
public class PaymentQueryTemplatorImpl extends AbstractGenericDao implements PaymentQueryTemplator {

    public PaymentQueryTemplatorImpl(HikariDataSource dataSource) {
        super(dataSource);
    }

    @Override
    public Query getSavePaymentQuery(Payment payment) {
        PaymentRecord paymentRecord = getDslContext().newRecord(PAYMENT, payment);
        return getDslContext().insertInto(PAYMENT)
                .set(paymentRecord)
                .onConflict(PAYMENT.INVOICE_ID, PAYMENT.PAYMENT_ID)
                .doNothing();
    }

    @Override
    public Query getSavePaymentStateQuery(PaymentState paymentState) {
        PaymentStateRecord paymentStateRecord = getDslContext().newRecord(PAYMENT_STATE, paymentState);
        return getDslContext().insertInto(PAYMENT_STATE)
                .set(paymentStateRecord)
                .onConflict(PAYMENT_STATE.INVOICE_ID, PAYMENT_STATE.SEQUENCE_ID, PAYMENT_STATE.CHANGE_ID)
                .doNothing();
    }

    @Override
    public Query getSavePaymentCostQuery(PaymentCost paymentCost) {
        PaymentCostRecord paymentCostRecord = getDslContext().newRecord(PAYMENT_COST, paymentCost);
        return getDslContext().insertInto(PAYMENT_COST)
                .set(paymentCostRecord)
                .onConflict(PAYMENT_COST.INVOICE_ID, PAYMENT_COST.SEQUENCE_ID, PAYMENT_COST.CHANGE_ID)
                .doNothing();
    }

    @Override
    public Query getSavePaymentRoutingQuery(PaymentRouting paymentRouting) {
        PaymentRoutingRecord paymentRoutingRecord = getDslContext().newRecord(PAYMENT_ROUTING, paymentRouting);
        return getDslContext().insertInto(PAYMENT_ROUTING)
                .set(paymentRoutingRecord)
                .onConflict(PAYMENT_ROUTING.INVOICE_ID, PAYMENT_ROUTING.SEQUENCE_ID, PAYMENT_ROUTING.CHANGE_ID)
                .doNothing();
    }

    @Override
    public Query getSavePaymentTerminalQuery(PaymentTerminalReceipt paymentTerminalReceipt) {
        PaymentTerminalReceiptRecord paymentShortIdRecord = getDslContext().newRecord(PAYMENT_TERMINAL_RECEIPT, paymentTerminalReceipt);
        return getDslContext().insertInto(PAYMENT_TERMINAL_RECEIPT)
                .set(paymentShortIdRecord)
                .onConflict(PAYMENT_TERMINAL_RECEIPT.INVOICE_ID, PAYMENT_TERMINAL_RECEIPT.SEQUENCE_ID, PAYMENT_TERMINAL_RECEIPT.CHANGE_ID)
                .doNothing();
    }

    @Override
    public Query getSavePaymentFeeQuery(PaymentFee paymentFee) {
        PaymentFeeRecord paymentFeeRecord = getDslContext().newRecord(PAYMENT_FEE, paymentFee);
        return getDslContext().insertInto(PAYMENT_FEE)
                .set(paymentFeeRecord)
                .onConflict(PAYMENT_FEE.INVOICE_ID, PAYMENT_FEE.SEQUENCE_ID, PAYMENT_FEE.CHANGE_ID)
                .doNothing();
    }
}
