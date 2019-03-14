package com.rbkmoney.reporter.dao;

import com.rbkmoney.reporter.domain.tables.pojos.Adjustment;
import com.rbkmoney.reporter.exception.DaoException;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import static io.github.benas.randombeans.api.EnhancedRandom.random;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class DaoTests extends AbstractAppDaoTests {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private AdjustmentDao adjustmentDao;

    @Test
    public void adjustmentDaoTest() throws DaoException {
        Adjustment a = random(Adjustment.class);
        a.setCurrent(true);
        a.setAdjustmentExternalCashFlow("{}");
        a.setAdjustmentProviderCashFlow("{}");
        PostgresJSONGsonBinding;
        Long id = adjustmentDao.save(a);
        a.setId(id);
        assertEquals(a, adjustmentDao.get(a.getInvoiceId(), a.getPaymentId(), a.getAdjustmentId()));
        adjustmentDao.updateNotCurrent(a.getInvoiceId(), a.getPaymentId(), a.getAdjustmentId());
        assertNull(adjustmentDao.get(a.getInvoiceId(), a.getPaymentId(), a.getAdjustmentId()));
    }
}
