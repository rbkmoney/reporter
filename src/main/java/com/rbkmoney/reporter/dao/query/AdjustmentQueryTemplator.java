package com.rbkmoney.reporter.dao.query;

import com.rbkmoney.reporter.domain.tables.pojos.Adjustment;
import com.rbkmoney.reporter.domain.tables.pojos.AdjustmentState;
import org.jooq.Query;

public interface AdjustmentQueryTemplator {

    Query getSaveAdjustmentQuery(Adjustment adjustment);

    Query getSaveAdjustmentStateQuery(AdjustmentState adjustmentState);

}
