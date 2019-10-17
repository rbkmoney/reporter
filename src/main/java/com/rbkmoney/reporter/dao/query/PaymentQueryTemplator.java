package com.rbkmoney.reporter.dao.query;

import com.rbkmoney.reporter.domain.tables.pojos.CashFlow;
import com.rbkmoney.reporter.domain.tables.pojos.Payment;
import com.rbkmoney.reporter.domain.tables.pojos.PaymentCost;
import com.rbkmoney.reporter.domain.tables.pojos.PaymentRouting;
import com.rbkmoney.reporter.domain.tables.pojos.PaymentShortId;
import com.rbkmoney.reporter.domain.tables.pojos.PaymentState;
import org.jooq.Query;

import java.util.List;

public interface PaymentQueryTemplator {

    Query getSavePaymentQuery(Payment payment);

    Query getSavePaymentStateQuery(PaymentState paymentState);

    Query getSavePaymentCostQuery(PaymentCost paymentCost);

    Query getSavePaymentRoutingQuery(PaymentRouting paymentRouting);

    Query getSavePaymentTerminalQuery(PaymentShortId paymentShortId);

    List<Query> getSavePaymentCashFlowQuery(List<CashFlow> cashFlowList);

}
