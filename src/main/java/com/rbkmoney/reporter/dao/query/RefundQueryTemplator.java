package com.rbkmoney.reporter.dao.query;

import com.rbkmoney.reporter.domain.tables.pojos.Refund;
import com.rbkmoney.reporter.domain.tables.pojos.RefundState;
import org.jooq.Query;

public interface RefundQueryTemplator {

    Query getSaveRefundQuery(Refund refund);

    Query getSaveRefundStateQuery(RefundState refundState);

}
