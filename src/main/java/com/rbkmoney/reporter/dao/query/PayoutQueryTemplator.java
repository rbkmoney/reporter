package com.rbkmoney.reporter.dao.query;

import com.rbkmoney.reporter.domain.tables.pojos.Payout;
import com.rbkmoney.reporter.domain.tables.pojos.PayoutState;
import org.jooq.Query;

public interface PayoutQueryTemplator {

    Query getSavePayoutQuery(Payout payout);

    Query getSavePayoutStateQuery(PayoutState payoutState);

}
