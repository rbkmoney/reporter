package com.rbkmoney.reporter.service;

import com.rbkmoney.damsel.base.*;
import com.rbkmoney.damsel.domain.*;
import com.rbkmoney.damsel.domain_config.RepositoryClientSrv;
import com.rbkmoney.damsel.domain_config.VersionedObject;
import com.rbkmoney.damsel.msgpack.Value;
import com.rbkmoney.damsel.payment_processing.PartyManagementSrv;
import com.rbkmoney.geck.common.util.TypeUtil;
import com.rbkmoney.reporter.domain.enums.ReportStatus;
import com.rbkmoney.reporter.domain.enums.ReportType;
import com.rbkmoney.reporter.domain.tables.pojos.FileMeta;
import com.rbkmoney.reporter.domain.tables.pojos.Report;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.thrift.TException;
import org.apache.tomcat.util.http.fileupload.util.Streams;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.jdbc.Sql;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static com.rbkmoney.geck.common.util.TypeUtil.stringToTemporal;
import static com.rbkmoney.geck.common.util.TypeUtil.toLocalDateTime;
import static io.github.benas.randombeans.api.EnhancedRandom.random;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

public class ServiceTests extends AbstractAppServiceTests {

    private static final String FUNDS_ACQUIRED = "funds_acquired";
    private static final String FUNDS_ADJUSTED = "funds_adjusted";
    private static final String FEE_CHARGED = "fee_charged";
    private static final String FUNDS_PAID_OUT = "funds_paid_out";
    private static final String FUNDS_REFUNDED = "funds_refunded";

    private final String partyId = UUID.randomUUID().toString();
    private final String shopId = generateString();
    private final String contractId = generateString();
    private final Instant fromTime = random(Instant.class);
    private final Instant toTime = random(Instant.class);
    private final Shop shop = getShop(shopId, contractId);
    private final RussianLegalEntity russianLegalEntity = getRussianLegalEntity();
    private final Contract contract = getContract(contractId, russianLegalEntity);
    private final Party party = getParty(partyId, shopId, contractId, shop, contract);

    @MockBean
    private RepositoryClientSrv.Iface dominantClient;

    @MockBean
    private PartyManagementSrv.Iface partyManagementClient;

    @Autowired
    private ReportingService reportingService;

    @Autowired
    private StorageService storageService;

    @Autowired
    private ReportService reportService;

    @Autowired
    private TaskService taskService;

    @Test
    @Sql("classpath:data/sql/shop_accounting_full_data.sql")
    public void reportingServiceTest() {
        Map<String, Long> data = reportingService.getShopAccountingReportData(
                "db79ad6c-a507-43ed-9ecf-3bbd88475b32",
                "test_shop_1",
                "RUB",
                toLocalDateTime(stringToTemporal("2017-08-31T21:00:00Z"))
        );
        assertEquals((long) 3000, (long) data.get("funds_acquired"));
        assertEquals((long) 75, (long) data.get("fee_charged"));
        assertEquals((long) 2, (long) data.get("funds_adjusted"));
        assertEquals((long) 1000, (long) data.get("funds_refunded"));
        assertEquals((long) 950, (long) data.get("funds_paid_out"));
    }

    @Test
    public void saveFileTest() throws IOException {
        Path expectedFile = Files.createTempFile("reporter_", "_expected_file");
        Path actualFile = Files.createTempFile("reporter_", "_actual_file");

        try {
            Files.write(expectedFile, "4815162342".getBytes());
            FileMeta fileMeta = storageService.saveFile(expectedFile);
            URL url = storageService.getFileUrl(fileMeta.getFileId(), fileMeta.getBucketId(), LocalDateTime.now().plusDays(1).toInstant(ZoneOffset.UTC));
            assertNotNull(url);

            try (InputStream in = url.openStream()) {
                Files.copy(in, actualFile, StandardCopyOption.REPLACE_EXISTING);
            }
            assertEquals(Files.readAllLines(expectedFile), Files.readAllLines(actualFile));
            assertEquals(fileMeta.getMd5(), DigestUtils.md5Hex(Files.newInputStream(actualFile)));
            assertEquals(fileMeta.getSha256(), DigestUtils.sha256Hex(Files.newInputStream(actualFile)));
        } finally {
            Files.deleteIfExists(expectedFile);
            Files.deleteIfExists(actualFile);
        }
    }

    @Test
    @Sql("classpath:data/sql/truncate.sql")
    public void pendingReportScheduledJobTest() throws IOException, InterruptedException, TException {
        mockPartyManagementClient(party);
        mockDominantClient();

        taskService.registerProvisionOfServiceJob(
                partyId,
                contractId,
                1L,
                new BusinessScheduleRef(1),
                new Representative("test", "test", RepresentativeDocument.articles_of_association(new ArticlesOfAssociation()))
        );

        long reportId = reportService.createReport(partyId, shopId, fromTime, toTime, ReportType.provision_of_service);

        TimeUnit.SECONDS.sleep(10);

        Report report = reportService.getReport(partyId, shopId, reportId);

        assertEquals(ReportStatus.created, report.getStatus());

        List<FileMeta> reportFiles = reportService.getReportFiles(report.getId());

        assertEquals(2, reportFiles.size());

        for (FileMeta fileMeta : reportFiles) {
            URL url = reportService.generatePresignedUrl(fileMeta.getFileId(), LocalDateTime.now().plusDays(1).toInstant(ZoneOffset.UTC));
            assertNotNull(url);
            try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
                try (InputStream inputStream = url.openStream()) {
                    Streams.copy(inputStream, outputStream, true);
                    byte[] actualBytes = outputStream.toByteArray();

                    assertEquals(fileMeta.getMd5(), DigestUtils.md5Hex(actualBytes));
                    assertEquals(fileMeta.getSha256(), DigestUtils.sha256Hex(actualBytes));
                }
            }
        }
    }

    private void mockPartyManagementClient(Party party) throws TException {
        given(partyManagementClient.checkout(any(), any(), any())).willReturn(party);
        given(partyManagementClient.getMetaData(any(), any(), any())).willReturn(Value.b(true));
    }

    private void mockDominantClient() throws TException {
        given(dominantClient.checkoutObject(any(), eq(Reference.payment_institution(new PaymentInstitutionRef(1)))))
                .willReturn(buildPaymentInstitutionObject(new PaymentInstitutionRef(1)));
        given(dominantClient.checkoutObject(any(), eq(Reference.calendar(new CalendarRef(1)))))
                .willReturn(buildPaymentCalendarObject(new CalendarRef(1)));
        given(dominantClient.checkoutObject(any(), eq(Reference.business_schedule(new BusinessScheduleRef(1)))))
                .willReturn(buildPayoutScheduleObject(new BusinessScheduleRef(1)));
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

    private VersionedObject buildPaymentInstitutionObject(PaymentInstitutionRef institutionRef) {
        PaymentInstitution institution = new PaymentInstitution();
        institution.setCalendar(new CalendarRef(1));

        return new VersionedObject(
                1,
                DomainObject.payment_institution(new PaymentInstitutionObject(institutionRef, institution))
        );
    }

    private VersionedObject buildPaymentCalendarObject(CalendarRef calendarRef) {
        Calendar calendar = new Calendar("calendar", "Europe/Moscow", Collections.emptyMap());

        return new VersionedObject(1, DomainObject.calendar(new CalendarObject(calendarRef, calendar)));
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
}
