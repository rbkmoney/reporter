package com.rbkmoney.reporter.dao;

import com.rbkmoney.reporter.domain.tables.pojos.Refund;
import com.rbkmoney.reporter.domain.tables.pojos.RefundAdditionalInfo;

public interface RefundDao {

    Long saveRefund(Refund refund);

    void saveAdditionalRefundInfo(RefundAdditionalInfo refundAdditionalInfo);

}
