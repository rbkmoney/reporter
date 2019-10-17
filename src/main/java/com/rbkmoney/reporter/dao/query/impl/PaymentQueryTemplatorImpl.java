package com.rbkmoney.reporter.dao.query.impl;

import com.rbkmoney.dao.impl.AbstractGenericDao;
import com.rbkmoney.reporter.dao.query.PaymentQueryTemplator;
import com.rbkmoney.reporter.domain.tables.pojos.CashFlow;
import com.rbkmoney.reporter.domain.tables.pojos.Payment;
import com.rbkmoney.reporter.domain.tables.pojos.PaymentCost;
import com.rbkmoney.reporter.domain.tables.pojos.PaymentRouting;
import com.rbkmoney.reporter.domain.tables.pojos.PaymentShortId;
import com.rbkmoney.reporter.domain.tables.pojos.PaymentState;
import com.rbkmoney.reporter.domain.tables.records.*;
import com.zaxxer.hikari.HikariDataSource;
import org.jooq.Query;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

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
                .onConflict(PAYMENT.INVOICE_ID, PAYMENT.SEQUENCE_ID, PAYMENT.CHANGE_ID)
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
    public Query getSavePaymentTerminalQuery(PaymentShortId paymentShortId) {
        PaymentShortIdRecord paymentShortIdRecord = getDslContext().newRecord(PAYMENT_SHORT_ID, paymentShortId);
        return getDslContext().insertInto(PAYMENT_SHORT_ID)
                .set(paymentShortIdRecord)
                .onConflict(PAYMENT_SHORT_ID.INVOICE_ID, PAYMENT_SHORT_ID.SEQUENCE_ID, PAYMENT_SHORT_ID.CHANGE_ID)
                .doNothing();
    }

    @Override
    public List<Query> getSavePaymentCashFlowQuery(List<CashFlow> cashFlowList) {
        List<Query> queries = new ArrayList<>();
        for (CashFlow cashFlow : cashFlowList) {
            CashFlowRecord cashFlowRecord = getDslContext().newRecord(CASH_FLOW, cashFlow);
            queries.add(getDslContext().insertInto(CASH_FLOW)
                    .set(cashFlowRecord)
                    .onConflict(CASH_FLOW.INVOICE_ID, CASH_FLOW.SEQUENCE_ID, CASH_FLOW.CHANGE_ID)
                    .doNothing());
        }
        return queries;
    }

}
