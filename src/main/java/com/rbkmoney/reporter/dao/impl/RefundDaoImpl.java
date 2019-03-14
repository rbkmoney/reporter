package com.rbkmoney.reporter.dao.impl;

import com.rbkmoney.reporter.dao.AbstractGenericDao;
import com.rbkmoney.reporter.dao.RefundDao;
import com.rbkmoney.reporter.dao.mapper.RecordRowMapper;
import com.rbkmoney.reporter.domain.tables.pojos.Refund;
import com.rbkmoney.reporter.domain.tables.records.RefundRecord;
import com.rbkmoney.reporter.exception.DaoException;
import org.jooq.Query;
import org.jooq.impl.DSL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.Optional;

import static com.rbkmoney.reporter.domain.tables.Refund.REFUND;

@Component
public class RefundDaoImpl extends AbstractGenericDao implements RefundDao {

    private final RowMapper<Refund> refundRowMapper;

    @Autowired
    public RefundDaoImpl(DataSource dataSource) {
        super(dataSource);
        refundRowMapper = new RecordRowMapper<>(REFUND, Refund.class);
    }

    @Override
    public Optional<Long> getLastEventId() throws DaoException {
        Query query = getDslContext().select(DSL.max(REFUND.EVENT_ID)).from(REFUND);
        return Optional.ofNullable(fetchOne(query, Long.class));
    }

    @Override
    public Long save(Refund refund) throws DaoException {
        RefundRecord record = getDslContext().newRecord(REFUND, refund);
        Query query = getDslContext().insertInto(REFUND).set(record).returning(REFUND.ID);
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        execute(query, keyHolder);
        return keyHolder.getKey().longValue();
    }

    @Override
    public Refund get(String invoiceId, String paymentId, String refundId) throws DaoException {
        Query query = getDslContext().selectFrom(REFUND)
                .where(
                        REFUND.INVOICE_ID.eq(invoiceId)
                                .and(REFUND.PAYMENT_ID.eq(paymentId))
                                .and(REFUND.REFUND_ID.eq(refundId))
                                .and(REFUND.CURRENT)
                );
        return fetchOne(query, refundRowMapper);
    }

    @Override
    public void updateNotCurrent(String invoiceId, String paymentId, String refundId) throws DaoException {
        Query query = getDslContext().update(REFUND).set(REFUND.CURRENT, false)
                .where(
                        REFUND.INVOICE_ID.eq(invoiceId)
                                .and(REFUND.PAYMENT_ID.eq(paymentId))
                                .and(REFUND.REFUND_ID.eq(refundId))
                                .and(REFUND.CURRENT)
                );
        executeOne(query);
    }
}
