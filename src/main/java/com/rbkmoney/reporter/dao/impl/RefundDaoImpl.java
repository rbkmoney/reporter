package com.rbkmoney.reporter.dao.impl;

import com.rbkmoney.dao.impl.AbstractGenericDao;
import com.rbkmoney.reporter.dao.RefundDao;
import com.rbkmoney.reporter.domain.tables.pojos.Refund;
import com.rbkmoney.reporter.domain.tables.pojos.RefundAdditionalInfo;
import com.zaxxer.hikari.HikariDataSource;
import org.jooq.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static com.rbkmoney.reporter.domain.tables.Refund.REFUND;
import static com.rbkmoney.reporter.domain.tables.RefundAdditionalInfo.REFUND_ADDITIONAL_INFO;

@Component
public class RefundDaoImpl extends AbstractGenericDao implements RefundDao {

    @Autowired
    public RefundDaoImpl(HikariDataSource dataSource) {
        super(dataSource);
    }

    @Override
    public Long saveRefund(Refund refund) {
        Query query = getDslContext()
                .insertInto(REFUND)
                .set(getDslContext().newRecord(REFUND, refund))
                .onDuplicateKeyUpdate()
                .set(getDslContext().newRecord(REFUND, refund))
                .returning(REFUND.ID);
        return fetchOne(query, Long.class);
    }

    @Override
    public void saveAdditionalRefundInfo(RefundAdditionalInfo refundAdditionalInfo) {
        Query query = getDslContext()
                .insertInto(REFUND_ADDITIONAL_INFO)
                .set(getDslContext().newRecord(REFUND_ADDITIONAL_INFO, refundAdditionalInfo))
                .onDuplicateKeyUpdate()
                .set(getDslContext().newRecord(REFUND_ADDITIONAL_INFO, refundAdditionalInfo));
        executeOne(query);
    }
}
