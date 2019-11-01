package com.rbkmoney.reporter.dao;

import com.rbkmoney.reporter.batch.impl.InvoiceUniqueBatchKeyImpl;
import com.rbkmoney.reporter.batch.impl.PaymentInvoiceUniqueBatchKey;
import com.rbkmoney.reporter.dao.mapper.dto.PartyData;
import com.rbkmoney.reporter.dao.mapper.dto.PaymentPartyData;
import com.rbkmoney.reporter.dao.mapper.dto.PaymentRegistryReportData;
import com.rbkmoney.reporter.dao.mapper.dto.RefundPaymentRegistryReportData;
import com.rbkmoney.reporter.domain.enums.ReportStatus;
import com.rbkmoney.reporter.domain.enums.ReportType;
import com.rbkmoney.reporter.domain.tables.pojos.ContractMeta;
import com.rbkmoney.reporter.domain.tables.pojos.FileMeta;
import com.rbkmoney.reporter.domain.tables.pojos.Report;
import com.rbkmoney.reporter.exception.DaoException;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;

import java.time.LocalDateTime;
import java.util.*;

import static com.rbkmoney.geck.common.util.TypeUtil.stringToTemporal;
import static com.rbkmoney.geck.common.util.TypeUtil.toLocalDateTime;
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
    @Sql("classpath:data/sql/adjustment_dao_test.sql")
    public void adjustmentDaoTest() {
        Map<String, Long> shopAccountingReportData = adjustmentDao.getShopAccountingReportData(
                "db79ad6c-a507-43ed-9ecf-3bbd88475b32",
                "test_shop_1",
                "RUB",
                Optional.of(toLocalDateTime(stringToTemporal("2017-08-31T21:00:00Z"))),
                toLocalDateTime(stringToTemporal("2017-08-31T21:00:00Z"))
        );

        assertEquals(0L, (long) shopAccountingReportData.get("funds_adjusted"));

        shopAccountingReportData = adjustmentDao.getShopAccountingReportData(
                "db79ad6c-a507-43ed-9ecf-3bbd88475b32",
                "test_shop_1",
                "RUB",
                Optional.of(toLocalDateTime(stringToTemporal("2021-08-23T12:12:50Z"))),
                toLocalDateTime(stringToTemporal("2022-08-23T12:12:54Z"))
        );

        assertEquals(1L, (long) shopAccountingReportData.get("funds_adjusted"));
    }

    @Test
    @Sql("classpath:data/sql/invoice_dao_test.sql")
    public void invoiceDaoTest() {
        PartyData partyData = invoiceDao.getPartyData(new InvoiceUniqueBatchKeyImpl("uAykKfsktM"));

        assertEquals(UUID.fromString("db79ad6c-a507-43ed-9ecf-3bbd88475b32"), partyData.getPartyId());
    }

    @Test
    @Sql("classpath:data/sql/payment_dao_test.sql")
    public void paymentDaoTest() {
        PaymentPartyData paymentPartyData = paymentDao.getPaymentPartyData(new PaymentInvoiceUniqueBatchKey("uAykKfsktM", "1"));

        assertEquals(1234L, (long) paymentPartyData.getPaymentAmount());

        Map<String, Long> shopAccountingReportData = paymentDao.getShopAccountingReportData(
                "db79ad6c-a507-43ed-9ecf-3bbd88475b33",
                "test_shop_1",
                "RUB",
                Optional.of(toLocalDateTime(stringToTemporal("2017-08-31T21:00:00Z"))),
                toLocalDateTime(stringToTemporal("2022-08-23T12:12:54Z"))
        );

        Long fundsAcquired = shopAccountingReportData.get("funds_acquired");
        Long feeCharged = shopAccountingReportData.get("fee_charged");

        assertEquals(247L, (long) fundsAcquired);
        assertEquals(100L, (long) feeCharged);

        List<PaymentRegistryReportData> paymentRegistryReportDataList = paymentDao.getPaymentRegistryReportData(
                "db79ad6c-a507-43ed-9ecf-3bbd88475b35",
                "test_shop_1",
                toLocalDateTime(stringToTemporal("2017-08-31T21:00:00Z")),
                toLocalDateTime(stringToTemporal("2022-08-23T12:12:54Z"))
        );

        assertEquals(124L, (long) paymentRegistryReportDataList.get(0).getPaymentAmount());
        assertEquals(100L, (long) paymentRegistryReportDataList.get(0).getPaymentFee());
        assertEquals(125L, (long) paymentRegistryReportDataList.get(1).getPaymentAmount());
        assertEquals(101L, (long) paymentRegistryReportDataList.get(1).getPaymentProviderFee());
    }

    @Test
    @Sql("classpath:data/sql/payout_dao_test.sql")
    public void payoutDaoTest() {
        Map<String, Long> shopAccountingReportData = payoutDao.getShopAccountingReportData(
                "db79ad6c-a507-43ed-9ecf-3bbd88475b32",
                "test_shop_1",
                "RUB",
                Optional.of(toLocalDateTime(stringToTemporal("2017-08-31T21:00:00Z"))),
                toLocalDateTime(stringToTemporal("2022-08-23T12:12:54Z"))
        );

        assertEquals(1L, (long) shopAccountingReportData.get("funds_paid_out"));
    }

    @Test
    @Sql("classpath:data/sql/refund_dao_test.sql")
    public void refundDaoTest() {
        Map<String, Long> shopAccountingReportData = refundDao.getShopAccountingReportData(
                "db79ad6c-a507-43ed-9ecf-3bbd88475b32",
                "test_shop_1",
                "RUB",
                Optional.of(toLocalDateTime(stringToTemporal("2017-08-31T21:00:00Z"))),
                toLocalDateTime(stringToTemporal("2022-08-23T12:12:54Z"))
        );

        assertEquals(22L, (long) shopAccountingReportData.get("funds_refunded"));


        List<RefundPaymentRegistryReportData> refundPaymentRegistryReportData = refundDao.getRefundPaymentRegistryReportData(
                "db79ad6c-a507-43ed-9ecf-3bbd88475b35",
                "test_shop_1",
                toLocalDateTime(stringToTemporal("2017-08-31T21:00:00Z")),
                toLocalDateTime(stringToTemporal("2022-08-23T12:12:54Z"))
        );

        assertEquals(123L, (long) refundPaymentRegistryReportData.get(0).getRefundAmount());
        assertEquals(124L, (long) refundPaymentRegistryReportData.get(1).getRefundAmount());
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
