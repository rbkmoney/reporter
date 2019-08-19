package com.rbkmoney.reporter.report;

import com.google.common.collect.ImmutableMap;
import com.rbkmoney.damsel.base.*;
import com.rbkmoney.damsel.domain.*;
import com.rbkmoney.damsel.domain_config.RepositoryClientSrv;
import com.rbkmoney.damsel.domain_config.VersionedObject;
import com.rbkmoney.damsel.msgpack.Value;
import com.rbkmoney.damsel.payment_processing.PartyManagementSrv;
import com.rbkmoney.geck.common.util.TypeUtil;
import com.rbkmoney.reporter.dao.ContractMetaDao;
import com.rbkmoney.reporter.dao.mapper.dto.PaymentRegistryReportData;
import com.rbkmoney.reporter.dao.mapper.dto.RefundPaymentRegistryReportData;
import com.rbkmoney.reporter.domain.enums.ReportStatus;
import com.rbkmoney.reporter.domain.enums.ReportType;
import com.rbkmoney.reporter.domain.tables.pojos.ContractMeta;
import com.rbkmoney.reporter.domain.tables.pojos.Report;
import com.rbkmoney.reporter.exception.DaoException;
import com.rbkmoney.reporter.service.ReportingService;
import com.rbkmoney.reporter.service.TemplateService;
import com.rbkmoney.reporter.util.FormatUtil;
import com.rbkmoney.reporter.util.TimeUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.thrift.TException;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.jdbc.Sql;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static io.github.benas.randombeans.api.EnhancedRandom.random;
import static io.github.benas.randombeans.api.EnhancedRandom.randomListOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

@Slf4j
public class ReportsBuildingTests extends AbstractAppReportBuildingTests {

    private static final String FUNDS_ACQUIRED = "funds_acquired";
    private static final String FUNDS_ADJUSTED = "funds_adjusted";
    private static final String FEE_CHARGED = "fee_charged";
    private static final String FUNDS_PAID_OUT = "funds_paid_out";
    private static final String FUNDS_REFUNDED = "funds_refunded";

    private final String partyId = generateString();
    private final String shopId = generateString();
    private final String contractId = generateString();

    private final Shop shop = getShop(shopId, contractId);
    private final RussianLegalEntity russianLegalEntity = getRussianLegalEntity();
    private final Contract contract = getContract(contractId, russianLegalEntity);
    private final Party party = getParty(partyId, shopId, contractId, shop, contract);
    private final List<PaymentRegistryReportData> payments = randomListOf(4, PaymentRegistryReportData.class);
    private final List<RefundPaymentRegistryReportData> refunds = randomListOf(4, RefundPaymentRegistryReportData.class);
    private final Map<String, Long> previousAccountingData = getAccountingData(15L);
    private final Map<String, Long> currentShopAccountingData = getAccountingData(20L);

    @MockBean
    private RepositoryClientSrv.Iface dominantClient;

    @MockBean
    private PartyManagementSrv.Iface partyManagementClient;

    @MockBean
    private ReportingService reportingService;

    @Autowired
    @Qualifier("paymentRegistryTemplate")
    private TemplateService paymentRegistryTemplate;

    @Autowired
    @Qualifier("provisionOfServiceTemplate")
    private TemplateService provisionOfServiceTemplate;

    @Autowired
    private ContractMetaDao contractMetaDao;

    @Before
    public void setUp() throws Exception {
        mockPartyManagementClient(party);
        mockReportingService(payments, refunds, previousAccountingData, currentShopAccountingData);
        mockDominantClient();
    }

    @Test
    @Sql("classpath:data/sql/truncate.sql")
    public void generateProvisionOfServiceReportTest() throws DaoException, IOException, TException {
        Path tempFile = Files.createTempFile("provision_of_service_", "_test_report.xlsx");

        System.out.println("Provision of service report generated on " + tempFile.toAbsolutePath().toString());

        Report report = getReport(partyId, shopId);

        ContractMeta contractMeta = random(ContractMeta.class, "lastClosingBalance");
        contractMeta.setPartyId(report.getPartyId());
        contractMeta.setContractId(contractId);
        contractMetaDao.save(contractMeta);

        try {
            provisionOfServiceTemplate.processReportTemplate(report, Files.newOutputStream(tempFile));

            Workbook wb = new XSSFWorkbook(Files.newInputStream(tempFile));
            Sheet sheet = wb.getSheetAt(0);

            Row headerRow = sheet.getRow(1);
            Cell merchantContractIdCell = headerRow.getCell(0);
            assertEquals(
                    String.format("к Договору № %s от", contract.getLegalAgreement().getLegalAgreementId()),
                    merchantContractIdCell.getStringCellValue()
            );
            Cell merchantContractSignedAtCell = headerRow.getCell(3);
            assertEquals(
                    TimeUtil.toLocalizedDate(contract.getLegalAgreement().getSignedAt(), ZoneId.of(report.getTimezone())),
                    merchantContractSignedAtCell.getStringCellValue()

            );

            Cell merchantNameCell = sheet.getRow(5).getCell(4);
            assertEquals(russianLegalEntity.getRegisteredName(), merchantNameCell.getStringCellValue());

            Cell merchantIdCell = sheet.getRow(7).getCell(4);
            assertEquals(party.getId(), merchantIdCell.getStringCellValue());

            Cell shopIdCell = sheet.getRow(9).getCell(4);
            assertEquals(report.getPartyShopId(), shopIdCell.getStringCellValue());

            Row dateRow = sheet.getRow(14);
            Cell fromTimeCell = dateRow.getCell(1);
            assertEquals(
                    TimeUtil.toLocalizedDate(report.getFromTime().toInstant(ZoneOffset.UTC), ZoneId.of(report.getTimezone())),
                    fromTimeCell.getStringCellValue()
            );
            Cell toTimeCell = dateRow.getCell(3);
            assertEquals(
                    TimeUtil.toLocalizedDate(report.getToTime().minusNanos(1).toInstant(ZoneOffset.UTC), ZoneId.of(report.getTimezone())),
                    toTimeCell.getStringCellValue()
            );

            Cell openingBalanceCell = sheet.getRow(23).getCell(3);
            assertEquals("#,##0.00", openingBalanceCell.getCellStyle().getDataFormatString());
            assertEquals(
                    FormatUtil.formatCurrency(getAvailableFunds(previousAccountingData)),
                    openingBalanceCell.getStringCellValue()
            );

            Cell fundsPaidOutCell = sheet.getRow(26).getCell(3);
            assertEquals("#,##0.00", fundsPaidOutCell.getCellStyle().getDataFormatString());
            assertEquals(
                    FormatUtil.formatCurrency(currentShopAccountingData.get(FUNDS_PAID_OUT)),
                    fundsPaidOutCell.getStringCellValue()
            );

            Cell fundsRefundedCell = sheet.getRow(28).getCell(3);
            assertEquals("#,##0.00", fundsRefundedCell.getCellStyle().getDataFormatString());
            assertEquals(
                    FormatUtil.formatCurrency(currentShopAccountingData.get(FUNDS_REFUNDED)),
                    fundsRefundedCell.getStringCellValue()
            );

            Cell closingBalanceCell = sheet.getRow(29).getCell(3);
            assertEquals("#,##0.00", closingBalanceCell.getCellStyle().getDataFormatString());
            assertEquals(
                    FormatUtil.formatCurrency(getAvailableFunds(previousAccountingData) + getAvailableFunds(currentShopAccountingData)),
                    closingBalanceCell.getStringCellValue()
            );

            Cell fundsAcquiredCell = sheet.getRow(17).getCell(3);
            assertEquals("#,##0.00", fundsAcquiredCell.getCellStyle().getDataFormatString());
            assertEquals(
                    FormatUtil.formatCurrency(currentShopAccountingData.get(FUNDS_ACQUIRED)),
                    fundsAcquiredCell.getStringCellValue()
            );
            assertEquals(
                    fundsAcquiredCell.getStringCellValue(),
                    sheet.getRow(24).getCell(3).getStringCellValue()
            );

            Cell feeChargedCell = sheet.getRow(19).getCell(3);
            assertEquals("#,##0.00", feeChargedCell.getCellStyle().getDataFormatString());
            assertEquals(
                    FormatUtil.formatCurrency(currentShopAccountingData.get(FEE_CHARGED)),
                    feeChargedCell.getStringCellValue()
            );
            assertEquals(
                    feeChargedCell.getStringCellValue(),
                    sheet.getRow(25).getCell(3).getStringCellValue()
            );

            assertEquals(
                    contractMeta.getRepresentativePosition(),
                    sheet.getRow(40).getCell(4).getStringCellValue()
            );
            assertEquals(
                    contractMeta.getRepresentativeFullName(),
                    sheet.getRow(41).getCell(4).getStringCellValue()
            );

        } finally {
            Files.deleteIfExists(tempFile);
        }
    }

    @Test
    @Sql("classpath:data/sql/truncate.sql")
    public void testProcessPaymentRegistryTemplate() throws IOException {
        Path tempFile = Files.createTempFile("registry_of_act_", "_test_report.xlsx");

        log.info("Registry of act report generated on " + tempFile.toAbsolutePath().toString());

        Report report = getReport(partyId, shopId);

        try {
            paymentRegistryTemplate.processReportTemplate(report, Files.newOutputStream(tempFile));
            Workbook wb = new XSSFWorkbook(Files.newInputStream(tempFile));
            Sheet sheet = wb.getSheetAt(0);

            String from = TimeUtil.toLocalizedDate(report.getFromTime().toInstant(ZoneOffset.UTC), ZoneId.of(report.getTimezone()));
            String to = TimeUtil.toLocalizedDate(report.getToTime().toInstant(ZoneOffset.UTC), ZoneId.of(report.getTimezone()));

            Cell paymentsHeaderCell = sheet.getRow(0).getCell(0);
            assertEquals(String.format("Платежи за период с %s по %s", from, to), paymentsHeaderCell.getStringCellValue());

            Cell paymentsTotalSum = sheet.getRow(payments.size() + 2).getCell(3);
            long expectedSum = payments.stream().mapToLong(PaymentRegistryReportData::getPaymentAmount).sum();
            assertEquals(FormatUtil.formatCurrency(expectedSum), paymentsTotalSum.getStringCellValue());

            Cell refundsHeaderCell = sheet.getRow(payments.size() + 2 + 3).getCell(0);
            assertEquals(String.format("Возвраты за период с %s по %s", from, to), refundsHeaderCell.getStringCellValue());

            Cell refundsTotalSum = sheet.getRow(payments.size() + 2 + 3 + refunds.size() + 2).getCell(3);
            long expectedRefundSum = refunds.stream().mapToLong(RefundPaymentRegistryReportData::getRefundAmount).sum();
            assertEquals(FormatUtil.formatCurrency(expectedRefundSum), refundsTotalSum.getStringCellValue());
        } finally {
            Files.deleteIfExists(tempFile);
        }
    }

    private void mockPartyManagementClient(Party party) throws TException {
        given(partyManagementClient.checkout(any(), any(), any())).willReturn(party);
        given(partyManagementClient.getMetaData(any(), any(), any())).willReturn(Value.b(true));
    }

    private void mockReportingService(List<PaymentRegistryReportData> payments, List<RefundPaymentRegistryReportData> refunds, Map<String, Long> previousAccountingData, Map<String, Long> currentShopAccountingData) {
        given(reportingService.getPaymentRegistryReportData(any(), any(), any(), any())).willReturn(payments);
        given(reportingService.getRefundPaymentRegistryReportData(any(), any(), any(), any())).willReturn(refunds);
        given(reportingService.getShopAccountingReportData(any(), any(), any(), any()))
                .willReturn(previousAccountingData);
        given(reportingService.getShopAccountingReportData(any(), any(), any(), any(), any()))
                .willReturn(currentShopAccountingData);
    }

    private void mockDominantClient() throws TException {
        given(dominantClient.checkoutObject(any(), eq(Reference.payment_institution(new PaymentInstitutionRef(1)))))
                .willReturn(buildPaymentInstitutionObject(new PaymentInstitutionRef(1)));
        given(dominantClient.checkoutObject(any(), eq(Reference.calendar(new CalendarRef(1)))))
                .willReturn(buildPaymentCalendarObject(new CalendarRef(1)));
        given(dominantClient.checkoutObject(any(), eq(Reference.business_schedule(new BusinessScheduleRef(1)))))
                .willReturn(buildPayoutScheduleObject(new BusinessScheduleRef(1)));
    }

    private VersionedObject buildPaymentCalendarObject(CalendarRef calendarRef) {
        Calendar calendar = new Calendar("calendar", "Europe/Moscow", Collections.emptyMap());

        return new VersionedObject(1, DomainObject.calendar(new CalendarObject(calendarRef, calendar)));
    }

    private VersionedObject buildPaymentInstitutionObject(PaymentInstitutionRef institutionRef) {
        PaymentInstitution institution = new PaymentInstitution();
        institution.setCalendar(new CalendarRef(1));

        return new VersionedObject(
                1,
                DomainObject.payment_institution(new PaymentInstitutionObject(institutionRef, institution))
        );
    }

    private VersionedObject buildPayoutScheduleObject(BusinessScheduleRef payoutScheduleRef) {
        ScheduleEvery nth5 = new ScheduleEvery();
        nth5.setNth((byte) 5);

        BusinessSchedule payoutSchedule = new BusinessSchedule();
        payoutSchedule.setName("schedule");
        payoutSchedule.setSchedule(
                new Schedule(
                        ScheduleYear.every(new ScheduleEvery()),
                        ScheduleMonth.every(new ScheduleEvery()),
                        ScheduleFragment.every(new ScheduleEvery()),
                        ScheduleDayOfWeek.every(new ScheduleEvery()),
                        ScheduleFragment.every(new ScheduleEvery()),
                        ScheduleFragment.every(new ScheduleEvery()),
                        ScheduleFragment.every(new ScheduleEvery(nth5))
                )
        );
        payoutSchedule.setPolicy(new PayoutCompilationPolicy(new TimeSpan()));

        return new VersionedObject(
                1,
                DomainObject.business_schedule(new BusinessScheduleObject(payoutScheduleRef, payoutSchedule))
        );
    }

    private ImmutableMap<String, Long> getAccountingData(Long fundsAdjusted) {
        return ImmutableMap.<String, Long>builder()
                .put(FUNDS_ACQUIRED, 10L)
                .put(FEE_CHARGED, 3L)
                .put(FUNDS_PAID_OUT, 2L)
                .put(FUNDS_REFUNDED, 1L)
                .put(FUNDS_ADJUSTED, fundsAdjusted)
                .build();
    }

    private Report getReport(String partyId, String shopId) {
        return new Report(
                generateLong(),
                LocalDateTime.now().minusMonths(1),
                LocalDateTime.now().plusDays(1),
                generateLocalDateTime(),
                partyId,
                shopId,
                random(ReportStatus.class),
                "Europe/Moscow",
                random(ReportType.class)
        );
    }

    private long getAvailableFunds(Map<String, Long> accountingData) {
        return accountingData.get(FUNDS_ACQUIRED) + accountingData.get(FUNDS_ADJUSTED)
                - accountingData.get(FEE_CHARGED) - accountingData.get(FUNDS_PAID_OUT) - accountingData.get(FUNDS_REFUNDED);
    }

    private Party getParty(String partyId, String shopId, String contractId, Shop shop, Contract contract) {
        Party party = new Party();
        party.setId(partyId);
        party.setShops(Collections.singletonMap(shopId, shop));
        party.setContracts(Collections.singletonMap(contractId, contract));
        return party;
    }

    private Contract getContract(String contractId, RussianLegalEntity russianLegalEntity) {
        Contract contract = new Contract();
        contract.setId(contractId);
        contract.setPaymentInstitution(new PaymentInstitutionRef(1));
        contract.setLegalAgreement(new LegalAgreement(TypeUtil.temporalToString(Instant.now()), random(String.class)));
        contract.setContractor(Contractor.legal_entity(LegalEntity.russian_legal_entity(russianLegalEntity)));
        return contract;
    }

    private RussianLegalEntity getRussianLegalEntity() {
        RussianLegalEntity russianLegalEntity = new RussianLegalEntity();
        russianLegalEntity.setRegisteredName(generateString());
        russianLegalEntity.setRepresentativePosition(generateString());
        russianLegalEntity.setRepresentativeFullName(generateString());
        return russianLegalEntity;
    }

    private Shop getShop(String shopId, String contractId) {
        Shop shop = new Shop();
        shop.setId(shopId);
        shop.setContractId(contractId);
        shop.setLocation(ShopLocation.url("http://2ch.hk/"));
        return shop;
    }
}
