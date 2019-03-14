package com.rbkmoney.reporter.dao.impl;

import com.rbkmoney.reporter.dao.AbstractGenericDao;
import com.rbkmoney.reporter.dao.AdjustmentDao;
import com.rbkmoney.reporter.dao.mapper.RecordRowMapper;
import com.rbkmoney.reporter.domain.tables.pojos.Adjustment;
import com.rbkmoney.reporter.domain.tables.records.AdjustmentRecord;
import com.rbkmoney.reporter.exception.DaoException;
import org.jooq.Query;
import org.jooq.impl.DSL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.Optional;

import static com.rbkmoney.reporter.domain.tables.Adjustment.ADJUSTMENT;

@Component
public class AdjustmentDaoImpl extends AbstractGenericDao implements AdjustmentDao {

    private final RowMapper<Adjustment> adjustmentRowMapper;

    @Autowired
    public AdjustmentDaoImpl(DataSource dataSource) {
        super(dataSource);
        adjustmentRowMapper = new RecordRowMapper<>(ADJUSTMENT, Adjustment.class);
    }

    @Override
    public Optional<Long> getLastEventId() throws DaoException {
        Query query = getDslContext().select(DSL.max(ADJUSTMENT.EVENT_ID)).from(ADJUSTMENT);
        return Optional.ofNullable(fetchOne(query, Long.class));
    }

    @Override
    public Long save(Adjustment adjustment) throws DaoException {
        AdjustmentRecord record = getDslContext().newRecord(ADJUSTMENT, adjustment);
        Query query = getDslContext().insertInto(ADJUSTMENT).set(record).returning(ADJUSTMENT.ID);
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        execute(query, keyHolder);
        return keyHolder.getKey().longValue();
    }

    @Override
    public Adjustment get(String invoiceId, String paymentId, String adjustmentId) throws DaoException {
        Query query = getDslContext().selectFrom(ADJUSTMENT)
                .where(
                        ADJUSTMENT.INVOICE_ID.eq(invoiceId)
                                .and(ADJUSTMENT.PAYMENT_ID.eq(paymentId))
                                .and(ADJUSTMENT.ADJUSTMENT_ID.eq(adjustmentId))
                                .and(ADJUSTMENT.CURRENT)
                );
        return fetchOne(query, adjustmentRowMapper);
    }

    @Override
    public void updateNotCurrent(String invoiceId, String paymentId, String adjustmentId) throws DaoException {
        Query query = getDslContext().update(ADJUSTMENT).set(ADJUSTMENT.CURRENT, false)
                .where(
                        ADJUSTMENT.INVOICE_ID.eq(invoiceId)
                                .and(ADJUSTMENT.PAYMENT_ID.eq(paymentId))
                                .and(ADJUSTMENT.ADJUSTMENT_ID.eq(adjustmentId))
                                .and(ADJUSTMENT.CURRENT)
                );
        executeOne(query);
    }
}
