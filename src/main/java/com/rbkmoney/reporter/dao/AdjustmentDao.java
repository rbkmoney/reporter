package com.rbkmoney.reporter.dao;

import com.rbkmoney.reporter.domain.tables.pojos.Adjustment;

public interface AdjustmentDao {

    Long saveAdjustment(Adjustment adjustment);

}
