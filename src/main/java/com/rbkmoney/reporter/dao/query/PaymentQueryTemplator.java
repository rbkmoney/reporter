package com.rbkmoney.reporter.dao.query;

import com.rbkmoney.reporter.domain.tables.pojos.*;
import org.jooq.Query;

public interface PaymentQueryTemplator {

    Query getSavePaymentQuery(Payment payment);

    Query getSavePaymentStateQuery(PaymentState paymentState);

    Query getSavePaymentCostQuery(PaymentCost paymentCost);

    Query getSavePaymentRoutingQuery(PaymentRouting paymentRouting);

    Query getSavePaymentTerminalQuery(PaymentTerminalReceipt paymentTerminalReceipt);

    Query getSavePaymentFeeQuery(PaymentFee paymentFee);

}
