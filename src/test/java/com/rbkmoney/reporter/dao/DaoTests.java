package com.rbkmoney.reporter.dao;

import com.rbkmoney.reporter.domain.enums.PayoutEventCategory;
import com.rbkmoney.reporter.domain.enums.ReportStatus;
import com.rbkmoney.reporter.domain.enums.ReportType;
import com.rbkmoney.reporter.domain.tables.pojos.*;
import com.rbkmoney.reporter.exception.DaoException;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.TimeZone;

import static io.github.benas.randombeans.api.EnhancedRandom.random;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class DaoTests extends AbstractAppDaoTests {

    @Autowired
    private AdjustmentDao adjustmentDao;

    @Autowired
    private InvoiceDao invoiceDao;

    @Autowired
    private PaymentDao paymentDao;

    @Autowired
    private RefundDao refundDao;

    @Autowired
    private PayoutDao payoutDao;

    @Autowired
    private ReportDao reportDao;

    @Autowired
    private ContractMetaDao contractMetaDao;

    @Test
    @Sql("classpath:data/sql/truncate.sql")
    public void adjustmentDaoTest() throws DaoException {
        Adjustment adjustment = random(Adjustment.class, "adjustmentCashFlow", "adjustmentCashFlowInverseOld");
        adjustment.setId(null);
        adjustment.setCurrent(true);
        Long id = adjustmentDao.save(adjustment);
        adjustment.setId(id);
        assertEquals(adjustment, adjustmentDao.get(adjustment.getInvoiceId(), adjustment.getPaymentId(), adjustment.getAdjustmentId()));
        adjustmentDao.updateNotCurrent(adjustment.getInvoiceId(), adjustment.getPaymentId(), adjustment.getAdjustmentId());
        assertNull(adjustmentDao.get(adjustment.getInvoiceId(), adjustment.getPaymentId(), adjustment.getAdjustmentId()));
    }

    @Test
    @Sql("classpath:data/sql/truncate.sql")
    public void duplicationTest() throws DaoException {
        Adjustment adjustment = random(Adjustment.class, "adjustmentCashFlow", "adjustmentCashFlowInverseOld");
        adjustment.setId(null);
        adjustment.setCurrent(true);
        adjustmentDao.save(adjustment);
        Long id = adjustmentDao.save(adjustment);
        adjustment.setId(id);
        assertEquals(adjustment, adjustmentDao.get(adjustment.getInvoiceId(), adjustment.getPaymentId(), adjustment.getAdjustmentId()));
        adjustmentDao.updateNotCurrent(adjustment.getInvoiceId(), adjustment.getPaymentId(), adjustment.getAdjustmentId());
        assertNull(adjustmentDao.get(adjustment.getInvoiceId(), adjustment.getPaymentId(), adjustment.getAdjustmentId()));
    }

    @Test
    @Sql("classpath:data/sql/truncate.sql")
    public void invoiceDaoTest() throws DaoException {
        Invoice invoice = random(Invoice.class);
        invoice.setId(null);
        invoice.setCurrent(true);
        Long id = invoiceDao.save(invoice);
        invoice.setId(id);
        assertEquals(invoice, invoiceDao.get(invoice.getInvoiceId()));
        invoiceDao.updateNotCurrent(invoice.getInvoiceId());
        assertNull(invoiceDao.get(invoice.getInvoiceId()));
    }

    @Test
    @Sql("classpath:data/sql/truncate.sql")
    public void paymentDaoTest() throws DaoException {
        Payment payment = random(Payment.class, "paymentCashFlow");
        payment.setId(null);
        payment.setCurrent(true);
        Long id = paymentDao.save(payment);
        payment.setId(id);
        assertEquals(payment, paymentDao.get(payment.getInvoiceId(), payment.getPaymentId()));
        paymentDao.updateNotCurrent(payment.getInvoiceId(), payment.getPaymentId());
        assertNull(paymentDao.get(payment.getInvoiceId(), payment.getPaymentId()));
    }

    @Test
    @Sql("classpath:data/sql/truncate.sql")
    public void refundDaoTest() throws DaoException {
        Refund refund = random(Refund.class, "refundCashFlow");
        refund.setId(null);
        refund.setCurrent(true);
        Long id = refundDao.save(refund);
        refund.setId(id);
        assertEquals(refund, refundDao.get(refund.getInvoiceId(), refund.getPaymentId(), refund.getRefundId()));
        refundDao.updateNotCurrent(refund.getInvoiceId(), refund.getPaymentId(), refund.getRefundId());
        assertNull(refundDao.get(refund.getInvoiceId(), refund.getPaymentId(), refund.getRefundId()));
    }

    @Test
    @Sql("classpath:data/sql/truncate.sql")
    public void payoutDaoTest() throws DaoException {
        Payout payout = random(Payout.class, "payoutCashFlow", "payoutSummary");
        payout.setId(null);
        payout.setCurrent(true);
        payout.setEventCategory(PayoutEventCategory.PAYOUT);
        Long id = payoutDao.save(payout);
        payout.setId(id);
        assertEquals(payout, payoutDao.get(payout.getPayoutId()));
        payoutDao.updateNotCurrent(payout.getPayoutId());
        assertNull(payoutDao.get(payout.getPayoutId()));
    }

    @Test
    @Sql("classpath:data/sql/truncate.sql")
    public void insertAndGetReportTest() throws DaoException {
        String partyId = generateString();
        String shopId = generateString();
        LocalDateTime fromTime = generateLocalDateTime();
        LocalDateTime toTime = generateLocalDateTime();
        ReportType reportType = random(ReportType.class);
        String timezone = random(TimeZone.class).getID();
        LocalDateTime createdAt = generateLocalDateTime();

        long reportId = reportDao.createReport(partyId, shopId, fromTime, toTime, reportType, timezone, createdAt);
        assertNull(reportDao.getReport("is", "null", reportId));

        Report report = reportDao.getReport(partyId, shopId, reportId);
        assertEquals(reportId, report.getId().longValue());
        assertEquals(partyId, report.getPartyId());
        assertEquals(shopId, report.getPartyShopId());
        assertEquals(fromTime, report.getFromTime());
        assertEquals(toTime, report.getToTime());
        assertEquals(reportType, report.getType());
        assertEquals(timezone, report.getTimezone());
        assertEquals(createdAt, report.getCreatedAt());

        assertEquals(1, reportDao.getReportsByRange(partyId, shopId, fromTime, toTime, new ArrayList<>()).size());

        assertEquals(1, reportDao.getReportsByRange(partyId, shopId, fromTime, toTime, Arrays.asList(reportType)).size());
    }

    @Test
    @Sql("classpath:data/sql/truncate.sql")
    public void checkCreatedStatus() throws DaoException {
        String partyId = generateString();
        String shopId = generateString();
        LocalDateTime fromTime = generateLocalDateTime();
        LocalDateTime toTime = generateLocalDateTime();
        ReportType reportType = random(ReportType.class);
        String timezone = random(TimeZone.class).getID();
        LocalDateTime createdAt = generateLocalDateTime();

        long reportId = reportDao.createReport(partyId, shopId, fromTime, toTime, reportType, timezone, createdAt);
        reportDao.changeReportStatus(reportId, ReportStatus.created);

        reportDao.getReport(partyId, shopId, reportId);
    }

    @Test
    @Sql("classpath:data/sql/truncate.sql")
    public void testSaveAndGet() throws DaoException {
        String partyId = "test";
        String contractId = "test";

        ContractMeta contractMeta = random(ContractMeta.class, "partyId", "contractId", "reportType");
        contractMeta.setPartyId(partyId);
        contractMeta.setContractId(contractId);

        contractMetaDao.save(contractMeta);
        ContractMeta contractMeta2 = contractMetaDao.get(contractMeta.getPartyId(), contractMeta.getContractId());
        assertEquals(contractMeta.getPartyId(), contractMeta2.getPartyId());
        assertEquals(contractMeta.getContractId(), contractMeta2.getContractId());
        assertEquals(contractMeta.getScheduleId(), contractMeta2.getScheduleId());
        assertEquals(contractMeta.getLastEventId(), contractMeta2.getLastEventId());
        assertEquals(contractMeta.getCalendarId(), contractMeta2.getCalendarId());

        assertEquals(contractMeta.getLastEventId(), contractMetaDao.getLastEventId().get());

        assertEquals(contractMeta2, contractMetaDao.getAllActiveContracts().get(0));

        contractMeta = random(ContractMeta.class, "partyId", "contractId", "reportType");
        contractMeta.setPartyId(partyId);
        contractMeta.setContractId(contractId);

        contractMetaDao.save(contractMeta);
        contractMeta2 = contractMetaDao.get(contractMeta.getPartyId(), contractMeta.getContractId());
        assertEquals(contractMeta.getPartyId(), contractMeta2.getPartyId());
        assertEquals(contractMeta.getContractId(), contractMeta2.getContractId());
        assertEquals(contractMeta.getScheduleId(), contractMeta2.getScheduleId());
        assertEquals(contractMeta.getLastEventId(), contractMeta2.getLastEventId());
        assertEquals(contractMeta.getCalendarId(), contractMeta2.getCalendarId());
    }
}
