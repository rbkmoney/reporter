package com.rbkmoney.reporter.dao.impl;

import com.rbkmoney.reporter.dao.AbstractGenericDao;
import com.rbkmoney.reporter.dao.PaymentDao;
import com.rbkmoney.reporter.dao.mapper.RecordRowMapper;
import com.rbkmoney.reporter.domain.tables.pojos.Payment;
import com.rbkmoney.reporter.domain.tables.records.PaymentRecord;
import com.rbkmoney.reporter.exception.DaoException;
import org.jooq.Query;
import org.jooq.impl.DSL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.Optional;

import static com.rbkmoney.reporter.domain.tables.Payment.PAYMENT;

@Component
public class PaymentDaoImpl extends AbstractGenericDao implements PaymentDao {

    private final RowMapper<Payment> paymentRowMapper;

    @Autowired
    public PaymentDaoImpl(DataSource dataSource) {
        super(dataSource);
        paymentRowMapper = new RecordRowMapper<>(PAYMENT, Payment.class);
    }

    @Override
    public Optional<Long> getLastEventId() throws DaoException {
        Query query = getDslContext().select(DSL.max(PAYMENT.EVENT_ID)).from(PAYMENT);
        return Optional.ofNullable(fetchOne(query, Long.class));
    }

    @Override
    public Long save(Payment payment) throws DaoException {
        PaymentRecord paymentRecord = getDslContext().newRecord(PAYMENT, payment);
        Query query = getDslContext().insertInto(PAYMENT).set(paymentRecord).returning(PAYMENT.ID);
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        execute(query, keyHolder);
        return keyHolder.getKey().longValue();
    }

    @Override
    public Payment get(String invoiceId, String paymentId) throws DaoException {
        Query query = getDslContext().selectFrom(PAYMENT)
                .where(
                        PAYMENT.INVOICE_ID.eq(invoiceId)
                                .and(PAYMENT.PAYMENT_ID.eq(paymentId))
                                .and(PAYMENT.CURRENT)
                );
        return fetchOne(query, paymentRowMapper);
    }

    @Override
    public void updateNotCurrent(String invoiceId, String paymentId) throws DaoException {
        Query query = getDslContext().update(PAYMENT).set(PAYMENT.CURRENT, false)
                .where(
                        PAYMENT.INVOICE_ID.eq(invoiceId)
                                .and(PAYMENT.PAYMENT_ID.eq(paymentId))
                                .and(PAYMENT.CURRENT)
                );
        executeOne(query);
    }
}
