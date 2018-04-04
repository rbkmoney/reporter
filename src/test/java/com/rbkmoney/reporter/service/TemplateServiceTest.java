package com.rbkmoney.reporter.service;

import com.rbkmoney.damsel.merch_stat.*;
import com.rbkmoney.reporter.AbstractIntegrationTest;
import com.rbkmoney.reporter.model.PartyModel;
import com.rbkmoney.reporter.model.Payment;
import com.rbkmoney.reporter.model.Refund;
import com.rbkmoney.reporter.model.ShopAccountingModel;
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
import java.util.*;

import static com.rbkmoney.reporter.util.TimeUtil.toZoneSameLocal;
import static io.github.benas.randombeans.api.EnhancedRandom.random;
import static org.junit.Assert.assertEquals;

/**
 * Created by tolkonepiu on 12/07/2017.
 */
public class TemplateServiceTest extends AbstractIntegrationTest {

    @Autowired
    private TemplateService templateService;

    @Test
    public void generateProvisionOfServiceReportTest() throws IOException {
        Path tempFile = Files.createTempFile("provision_of_service_", "_test_report.xlsx");
        System.out.println("Provision of service report generated on " + tempFile.toAbsolutePath().toString());

        Instant fromTime = random(Instant.class);
        Instant toTime = random(Instant.class);
        PartyModel partyModel = random(PartyModel.class);
        ShopAccountingModel shopAccountingModel = random(ShopAccountingModel.class);
        ZoneId zoneId = ZoneId.of("Europe/Moscow");

        try {
            templateService.processProvisionOfServiceTemplate(
                    partyModel,
                    shopAccountingModel,
                    fromTime,
                    toTime,
                    zoneId,
                    Files.newOutputStream(tempFile));

            Workbook wb = new XSSFWorkbook(Files.newInputStream(tempFile));
            Sheet sheet = wb.getSheetAt(0);

            Row headerRow = sheet.getRow(1);
            Cell merchantContractIdCell = headerRow.getCell(0);
            assertEquals(
                    String.format("к Договору № %s от", partyModel.getMerchantContractId()),
                    merchantContractIdCell.getStringCellValue()
            );
            Cell merchantContractSignedAtCell = headerRow.getCell(3);
            assertEquals("dd\\.mm\\.yyyy", merchantContractSignedAtCell.getCellStyle().getDataFormatString());
            assertEquals(
                    partyModel.getMerchantContractSignedAt(),
                    merchantContractSignedAtCell.getDateCellValue()
            );

            Cell merchantNameCell = sheet.getRow(5).getCell(4);
            assertEquals(partyModel.getMerchantName(), merchantNameCell.getStringCellValue());

            Cell merchantIdCell = sheet.getRow(7).getCell(4);
            assertEquals(partyModel.getMerchantId(), merchantIdCell.getStringCellValue());

            Row dateRow = sheet.getRow(14);
            Cell fromTimeCell = dateRow.getCell(1);
            assertEquals(
                    "dd\\.mm\\.yyyy",
                    fromTimeCell.getCellStyle().getDataFormatString()
            );
            assertEquals(Date.from(toZoneSameLocal(fromTime, zoneId)), fromTimeCell.getDateCellValue());
            Cell toTimeCell = dateRow.getCell(3);
            assertEquals(
                    "dd\\.mm\\.yyyy",
                    toTimeCell.getCellStyle().getDataFormatString()
            );
            assertEquals(Date.from(toZoneSameLocal(toTime, zoneId).minusMillis(1)), toTimeCell.getDateCellValue());

            Cell openingBalanceCell = sheet.getRow(23).getCell(3);
            assertEquals("#,##0.00", openingBalanceCell.getCellStyle().getDataFormatString());
            assertEquals(
                    BigDecimal.valueOf(shopAccountingModel.getOpeningBalance()),
                    BigDecimal.valueOf(openingBalanceCell.getNumericCellValue())
            );

            Cell fundsPaidOutCell = sheet.getRow(26).getCell(3);
            assertEquals("#,##0.00", fundsPaidOutCell.getCellStyle().getDataFormatString());
            assertEquals(
                    BigDecimal.valueOf(shopAccountingModel.getFundsPaidOut()),
                    BigDecimal.valueOf(fundsPaidOutCell.getNumericCellValue())
            );

            Cell fundsRefundedCell = sheet.getRow(28).getCell(3);
            assertEquals("#,##0.00", fundsRefundedCell.getCellStyle().getDataFormatString());
            assertEquals(
                    BigDecimal.valueOf(shopAccountingModel.getFundsRefunded()),
                    BigDecimal.valueOf(fundsRefundedCell.getNumericCellValue())
            );

            Cell closingBalanceCell = sheet.getRow(29).getCell(3);
            assertEquals("#,##0.00", closingBalanceCell.getCellStyle().getDataFormatString());
            assertEquals(
                    BigDecimal.valueOf(shopAccountingModel.getClosingBalance()),
                    BigDecimal.valueOf(closingBalanceCell.getNumericCellValue())
            );

            Cell fundsAcquiredCell = sheet.getRow(17).getCell(3);
            assertEquals("#,##0.00", fundsAcquiredCell.getCellStyle().getDataFormatString());
            assertEquals(
                    BigDecimal.valueOf(shopAccountingModel.getFundsAcquired()),
                    BigDecimal.valueOf(fundsAcquiredCell.getNumericCellValue())
            );
            assertEquals(
                    BigDecimal.valueOf(fundsAcquiredCell.getNumericCellValue()),
                    BigDecimal.valueOf(sheet.getRow(24).getCell(3).getNumericCellValue())
            );

            Cell feeChargedCell = sheet.getRow(19).getCell(3);
            assertEquals("#,##0.00", feeChargedCell.getCellStyle().getDataFormatString());
            assertEquals(
                    BigDecimal.valueOf(shopAccountingModel.getFeeCharged()),
                    BigDecimal.valueOf(feeChargedCell.getNumericCellValue())
            );
            assertEquals(
                    BigDecimal.valueOf(feeChargedCell.getNumericCellValue()),
                    BigDecimal.valueOf(sheet.getRow(25).getCell(3).getNumericCellValue())
            );

            assertEquals(
                    partyModel.getMerchantRepresentativePosition(),
                    sheet.getRow(40).getCell(4).getStringCellValue()
            );
            assertEquals(
                    partyModel.getMerchantRepresentativeFullName(),
                    sheet.getRow(41).getCell(4).getStringCellValue()
            );

        } finally {
          //  Files.deleteIfExists(tempFile);
        }
    }

    @Test
    public void testProcessPaymentRegistryTemplate() throws IOException, TException {
        Path tempFile = Files.createTempFile("registry_of_act_", "_test_report.xlsx");
        System.out.println("Registry of act report generated on " + tempFile.toAbsolutePath().toString());

        Instant fromTime = random(Instant.class);
        Instant toTime = random(Instant.class);
        ZoneId zoneId = ZoneId.of("Europe/Moscow");

        List<Payment> paymentList = new ArrayList<>();
        for (int i = 0; i < 3; ++i) {
            Payment payment = new Payment();
            payment.setId("id" + i);
            payment.setCapturedAt(TimeUtil.toLocalizedDateTime("201" + i + "-03-22T06:12:27Z", zoneId));
            payment.setCardNum("4242****56789" +  i);
            payment.setAmount(123L + i);
            payment.setPayoutAmount(120L + i);
            payment.setPayerEmail("abc" + i + "@mail.ru");
            payment.setShopUrl("2ch" + i + ".ru");
            payment.setPurpose("purpose" + i);
            paymentList.add(payment);
        }

        List<Refund> refundList = new ArrayList<>();
        for (int i = 0; i < 3; ++i) {
            Refund refund = new Refund();
            refund.setId("id" + i);
            refund.setPaymentId("paymentId" + i);
            refund.setPaymentCapturedAt(TimeUtil.toLocalizedDateTime("201" + i + "-03-22T06:12:27Z", zoneId));
            refund.setSucceededAt(TimeUtil.toLocalizedDateTime("201" + i + "-03-22T06:12:27Z", zoneId));
            refund.setCardNum("4242****56789" + i);
            refund.setAmount(123L + i);
            refund.setPayerEmail("abc" + i + "@mail.ru");
            refund.setShopUrl("2ch" + i + ".ru");
            refund.setPaymentPurpose("purpose" + i);
            refundList.add(refund);
        }

        try {
            templateService.processRegistryOfActTemplate(
                    paymentList,
                    refundList,
                    fromTime,
                    toTime,
                    zoneId,
                    Files.newOutputStream(tempFile));
        } finally {
            Files.deleteIfExists(tempFile);
        }
    }

}
