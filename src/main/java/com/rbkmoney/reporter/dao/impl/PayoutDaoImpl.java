package com.rbkmoney.reporter.dao.impl;

import com.rbkmoney.reporter.dao.AbstractGenericDao;
import com.rbkmoney.reporter.dao.PayoutDao;
import com.rbkmoney.reporter.dao.mapper.RecordRowMapper;
import com.rbkmoney.reporter.domain.tables.pojos.Payout;
import com.rbkmoney.reporter.domain.tables.records.PayoutRecord;
import com.rbkmoney.reporter.exception.DaoException;
import org.jooq.Query;
import org.jooq.impl.DSL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.Optional;

import static com.rbkmoney.reporter.domain.tables.Payout.PAYOUT;

@Component
public class PayoutDaoImpl extends AbstractGenericDao implements PayoutDao {

    private final RowMapper<Payout> rowMapper;

    @Autowired
    public PayoutDaoImpl(DataSource dataSource) {
        super(dataSource);
        rowMapper = new RecordRowMapper<>(PAYOUT, Payout.class);
    }

    @Override
    public Optional<Long> getLastEventId() throws DaoException {
        Query query = getDslContext().select(DSL.max(PAYOUT.EVENT_ID)).from(PAYOUT);
        return Optional.ofNullable(fetchOne(query, Long.class));
    }

    @Override
    public Long save(Payout payout) throws DaoException {
        PayoutRecord payoutRecord = getDslContext().newRecord(PAYOUT, payout);
        Query query = getDslContext().insertInto(PAYOUT).set(payoutRecord).returning(PAYOUT.ID);
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        execute(query, keyHolder);
        return keyHolder.getKey().longValue();
    }

    @Override
    public Payout get(String payoutId) throws DaoException {
        Query query = getDslContext().selectFrom(PAYOUT)
                .where(PAYOUT.PAYOUT_ID.eq(payoutId).and(PAYOUT.CURRENT));
        return fetchOne(query, rowMapper);
    }

    @Override
    public void updateNotCurrent(String payoutId) throws DaoException {
        Query query = getDslContext().update(PAYOUT).set(PAYOUT.CURRENT, false)
                .where(PAYOUT.PAYOUT_ID.eq(payoutId).and(PAYOUT.CURRENT));
        executeOne(query);
    }
}
