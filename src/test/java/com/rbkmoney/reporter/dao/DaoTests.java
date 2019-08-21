package com.rbkmoney.reporter.dao;

import com.rbkmoney.reporter.domain.enums.*;
import com.rbkmoney.reporter.domain.tables.pojos.*;
import com.rbkmoney.reporter.exception.DaoException;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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
        adjustment.setEventType(InvoiceEventType.INVOICE_PAYMENT_ADJUSTMENT_CREATED);
        Long id = adjustmentDao.save(adjustment);
        adjustment.setId(id);
        assertEquals(adjustment, adjustmentDao.get(adjustment.getInvoiceId(), adjustment.getPaymentId(), adjustment.getAdjustmentId()));

        adjustment.setId(null);
        adjustment.setEventType(InvoiceEventType.INVOICE_PAYMENT_ADJUSTMENT_STATUS_CHANGED);
        adjustment.setAdjustmentStatus(AdjustmentStatus.captured);
        adjustment.setChangeId(adjustment.getChangeId() + 1);
        id = adjustmentDao.save(adjustment);
        adjustment.setId(id);
        assertEquals(adjustment, adjustmentDao.get(adjustment.getInvoiceId(), adjustment.getPaymentId(), adjustment.getAdjustmentId()));
    }

    @Test
    @Sql("classpath:data/sql/truncate.sql")
    public void duplicationTest() throws DaoException {
        Adjustment adjustment = random(Adjustment.class, "adjustmentCashFlow", "adjustmentCashFlowInverseOld");
        adjustment.setId(null);
        adjustmentDao.save(adjustment);
        assertNull(adjustmentDao.save(adjustment));
    }

    @Test
    @Sql("classpath:data/sql/truncate.sql")
    public void invoiceDaoTest() throws DaoException {
        Invoice invoice = random(Invoice.class);
        invoice.setId(null);
        invoice.setEventType(InvoiceEventType.INVOICE_CREATED);
        Long id = invoiceDao.save(invoice);
        invoice.setId(id);
        assertEquals(invoice, invoiceDao.get(invoice.getInvoiceId()));

        invoice.setId(null);
        invoice.setEventType(InvoiceEventType.INVOICE_STATUS_CHANGED);
        invoice.setInvoiceStatus(InvoiceStatus.paid);
        invoice.setChangeId(invoice.getChangeId() + 1);
        id = invoiceDao.save(invoice);
        invoice.setId(id);
        assertEquals(invoice, invoiceDao.get(invoice.getInvoiceId()));
        assertEquals(invoice.getPartyId(), invoiceDao.getPartyData(invoice.getInvoiceId()).getPartyId());
    }

    @Test
    @Sql("classpath:data/sql/truncate.sql")
    public void paymentDaoTest() throws DaoException {
        Payment payment = random(Payment.class, "paymentCashFlow");
        payment.setId(null);
        payment.setPaymentFingerprint("b334ba917e0e863283832f5d74a8cd1c'\"\u0000\u0001\u0002\u0003\u0004\u0005\u0006\u0007\b\t\n" +
                "\u000B\f\n" +
                "\u000E\u000F\u0010\u0011\u0012\u0013\u0014\u0015\u0016\u0017\u0018\u0019\u001A\u001B\u001C\u001D\u001E\u001F !\"#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKL" +
                "MNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|}~\u007F\u0080\u0081\u0082\u0083\u0084\u0085\u0086\u0087\u0088\u0089\u008A\u008B\u008C\u008D\u008E\u008F\u0090\u0091\u0092\u0093" +
                "\u0094\u0095\u0096\u0097\u0098\u0099\u009A\u009B\u009C\u009D\u009E\u009F ¡¢£¤¥¦§¨©ª«¬\u00AD®¯°±²³´µ¶·¸¹º»¼½¾¿ÀÁÂÃÄÅÆÇÈÉÊËÌÍÎÏÐÑÒÓÔÕÖ×ØÙÚÛÜÝÞßàáâãäåæçèéêëìíîïðñòóôõö÷øùúûüýþÿ\"'");
        Long id = paymentDao.save(payment);

        payment.setId(id);
        payment.setPaymentFingerprint(payment.getPaymentFingerprint().replace("\u0000", "\\u0000"));
        assertEquals(payment, paymentDao.get(payment.getInvoiceId(), payment.getPaymentId()));
        assertEquals(payment.getPartyId(), paymentDao.getPaymentPartyData(payment.getInvoiceId(), payment.getPaymentId()).getPartyId());
        assertEquals(payment.getPaymentAmount(), paymentDao.getPaymentPartyData(payment.getInvoiceId(), payment.getPaymentId()).getPaymentAmount());
    }

    @Test
    @Sql("classpath:data/sql/truncate.sql")
    public void refundDaoTest() throws DaoException {
        Refund refund = random(Refund.class, "refundCashFlow");
        refund.setId(null);
        refund.setEventType(InvoiceEventType.INVOICE_PAYMENT_REFUND_CREATED);
        Long id = refundDao.save(refund);
        refund.setId(id);
        assertEquals(refund, refundDao.get(refund.getInvoiceId(), refund.getPaymentId(), refund.getRefundId()));

        refund.setId(null);
        refund.setEventType(InvoiceEventType.INVOICE_PAYMENT_REFUND_STATUS_CHANGED);
        refund.setRefundStatus(RefundStatus.succeeded);
        refund.setChangeId(refund.getChangeId() + 1);
        id = refundDao.save(refund);
        refund.setId(id);
        assertEquals(refund, refundDao.get(refund.getInvoiceId(), refund.getPaymentId(), refund.getRefundId()));
    }

    @Test
    @Sql("classpath:data/sql/truncate.sql")
    public void payoutDaoTest() throws DaoException {
        Payout payout = random(Payout.class, "payoutCashFlow", "payoutSummary");
        payout.setId(null);
        payout.setEventCategory(PayoutEventCategory.PAYOUT);
        Long id = payoutDao.save(payout);
        payout.setId(id);
        assertEquals(payout, payoutDao.get(payout.getPayoutId()));
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

        assertEquals(1, reportDao.getReportsByRange(partyId, shopId, new ArrayList<>(), fromTime, toTime).size());

        assertEquals(1, reportDao.getReportsByRange(partyId, shopId, Arrays.asList(reportType), fromTime, toTime).size());
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
    public void attachFileTest() throws DaoException {
        FileMeta file = random(FileMeta.class);
        Long reportId = generateLong();

        String fileId = reportDao.attachFile(reportId, file);
        FileMeta currentFile = reportDao.getFile(fileId);

        assertEquals(file.getFileId(), currentFile.getFileId());
        assertEquals(reportId, currentFile.getReportId());
        assertEquals(file.getBucketId(), currentFile.getBucketId());
        assertEquals(file.getFilename(), currentFile.getFilename());
        assertEquals(file.getMd5(), currentFile.getMd5());
        assertEquals(file.getSha256(), currentFile.getSha256());

        List<FileMeta> files = reportDao.getReportFiles(reportId);
        assertEquals(1, files.size());

        currentFile = files.get(0);

        assertEquals(file.getFileId(), currentFile.getFileId());
        assertEquals(reportId, currentFile.getReportId());
        assertEquals(file.getBucketId(), currentFile.getBucketId());
        assertEquals(file.getFilename(), currentFile.getFilename());
        assertEquals(file.getMd5(), currentFile.getMd5());
        assertEquals(file.getSha256(), currentFile.getSha256());
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
