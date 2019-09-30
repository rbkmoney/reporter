package com.rbkmoney.reporter.service;

import com.rbkmoney.damsel.domain.*;
import com.rbkmoney.damsel.payment_processing.PartyManagementSrv;
import com.rbkmoney.geck.common.util.TypeUtil;
import com.rbkmoney.reporter.AbstractIntegrationTest;
import com.rbkmoney.reporter.dao.ContractMetaDao;
import com.rbkmoney.reporter.domain.tables.pojos.ContractMeta;
import com.rbkmoney.reporter.domain.tables.pojos.Report;
import com.rbkmoney.reporter.exception.DaoException;
import com.rbkmoney.reporter.model.ShopAccountingModel;
import com.rbkmoney.reporter.service.impl.ProvisionOfServiceTemplateImpl;
import com.rbkmoney.reporter.util.FormatUtil;
import com.rbkmoney.reporter.util.TimeUtil;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.thrift.TException;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static io.github.benas.randombeans.api.EnhancedRandom.random;
import static org.junit.Assert.assertEquals;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;

/**
 * Created by tolkonepiu on 12/07/2017.
 */
public class ProvisionOfServiceTemplateServiceTest extends AbstractIntegrationTest {

    @Autowired
    private ProvisionOfServiceTemplateImpl templateService;

    @Autowired
    private ContractMetaDao contractMetaDao;

    @MockBean
    private PartyManagementSrv.Iface partyManagementClient;

    @MockBean
    private StatisticService statisticService;

    @Test
    public void generateProvisionOfServiceReportTest() throws DaoException, IOException, TException {
        Path tempFile = Files.createTempFile("provision_of_service_", "_test_report.xlsx");
        System.out.println("Provision of service report generated on " + tempFile.toAbsolutePath().toString());

        String contractId = random(String.class);
        Report report = random(Report.class);
        report.setTimezone("Europe/Moscow");

        Party party = new Party();
        party.setId(report.getPartyId());
        Contract contract = new Contract();
        Shop shop = new Shop();
        shop.setId(report.getPartyId());
        shop.setContractId(contractId);
        party.setShops(Collections.singletonMap(report.getPartyShopId(), shop));
        contract.setId(contractId);
        RussianLegalEntity russianLegalEntity = new RussianLegalEntity();
        russianLegalEntity.setRegisteredName(random(String.class));
        russianLegalEntity.setRepresentativePosition(random(String.class));
        russianLegalEntity.setRepresentativeFullName(random(String.class));
        contract.setContractor(Contractor.legal_entity(LegalEntity.russian_legal_entity(russianLegalEntity)));
        contract.setLegalAgreement(new LegalAgreement(TypeUtil.temporalToString(Instant.now()), random(String.class)));
        party.setContracts(Collections.singletonMap(contractId, contract));
        given(partyManagementClient.checkout(any(), any(), any()))
                .willReturn(party);
        given(partyManagementClient.getRevision(any(), any()))
                .willReturn(1L);

        ShopAccountingModel previousAccounting = random(ShopAccountingModel.class);
        given(statisticService.getShopAccounting(report.getPartyId(), report.getPartyShopId(), "RUB", report.getFromTime().toInstant(ZoneOffset.UTC)))
                .willReturn(previousAccounting);

        ShopAccountingModel currentAccounting = random(ShopAccountingModel.class);
        given(statisticService.getShopAccounting(report.getPartyId(), report.getPartyShopId(), "RUB", report.getFromTime().toInstant(ZoneOffset.UTC), report.getToTime().toInstant(ZoneOffset.UTC)))
                .willReturn(currentAccounting);

        ContractMeta contractMeta = random(ContractMeta.class, "lastClosingBalance");
        contractMeta.setPartyId(report.getPartyId());
        contractMeta.setContractId(contractId);
        contractMetaDao.save(contractMeta);

        try {
            templateService.processReportTemplate(report, Files.newOutputStream(tempFile));

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
                    FormatUtil.formatCurrency(previousAccounting.getAvailableFunds()),
                    openingBalanceCell.getStringCellValue()
            );

            Cell fundsPaidOutCell = sheet.getRow(26).getCell(3);
            assertEquals("#,##0.00", fundsPaidOutCell.getCellStyle().getDataFormatString());
            assertEquals(
                    FormatUtil.formatCurrency(currentAccounting.getFundsPaidOut()),
                    fundsPaidOutCell.getStringCellValue()
            );

            Cell fundsRefundedCell = sheet.getRow(28).getCell(3);
            assertEquals("#,##0.00", fundsRefundedCell.getCellStyle().getDataFormatString());
            assertEquals(
                    FormatUtil.formatCurrency(currentAccounting.getFundsRefunded()),
                    fundsRefundedCell.getStringCellValue()
            );

            Cell closingBalanceCell = sheet.getRow(29).getCell(3);
            assertEquals("#,##0.00", closingBalanceCell.getCellStyle().getDataFormatString());
            assertEquals(
                    FormatUtil.formatCurrency(previousAccounting.getAvailableFunds() + currentAccounting.getAvailableFunds()),
                    closingBalanceCell.getStringCellValue()
            );

            Cell fundsAcquiredCell = sheet.getRow(17).getCell(3);
            assertEquals("#,##0.00", fundsAcquiredCell.getCellStyle().getDataFormatString());
            assertEquals(
                    FormatUtil.formatCurrency(currentAccounting.getFundsAcquired()),
                    fundsAcquiredCell.getStringCellValue()
            );
            assertEquals(
                    fundsAcquiredCell.getStringCellValue(),
                    sheet.getRow(24).getCell(3).getStringCellValue()
            );

            Cell feeChargedCell = sheet.getRow(19).getCell(3);
            assertEquals("#,##0.00", feeChargedCell.getCellStyle().getDataFormatString());
            assertEquals(
                    FormatUtil.formatCurrency(currentAccounting.getFeeCharged()),
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

}
