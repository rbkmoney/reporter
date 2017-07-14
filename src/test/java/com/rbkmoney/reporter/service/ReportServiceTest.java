package com.rbkmoney.reporter.service;

import com.rbkmoney.reporter.model.ShopAccounting;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;

import static io.github.benas.randombeans.api.EnhancedRandom.random;
import static org.junit.Assert.assertEquals;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

/**
 * Created by tolkonepiu on 12/07/2017.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = RANDOM_PORT)
public class ReportServiceTest {

    @Autowired
    private ReportService reportService;

    @Test
    public void generateProvisionOfServiceReportTest() throws IOException {
        Path tempFile = Files.createTempFile("provision_of_service_", "_test_report.xlsx");
        System.out.println("Provision of service report generated on " + tempFile.toAbsolutePath().toString());

        ShopAccounting shopAccounting = random(ShopAccounting.class);
        reportService.generateProvisionOfServiceReport(shopAccounting, Files.newOutputStream(tempFile));

        Workbook wb = new XSSFWorkbook(Files.newInputStream(tempFile));
        Sheet sheet = wb.getSheetAt(0);

        Row headerRow = sheet.getRow(1);
        Cell merchantContractIdCell = headerRow.getCell(0);
        assertEquals(
                String.format("к Договору № %s от", shopAccounting.getMerchantContractId()),
                merchantContractIdCell.getStringCellValue()
        );
        Cell merchantContractCreatedAtCell = headerRow.getCell(3);
        assertEquals("dd\\.mm\\.yyyy", merchantContractCreatedAtCell.getCellStyle().getDataFormatString());
        assertEquals(
                shopAccounting.getMerchantContractCreatedAt(),
                merchantContractCreatedAtCell.getDateCellValue()
        );

        Cell merchantNameCell = sheet.getRow(5).getCell(4);
        assertEquals(shopAccounting.getMerchantName(), merchantNameCell.getStringCellValue());

        Cell merchantIdCell = sheet.getRow(7).getCell(4);
        assertEquals(shopAccounting.getMerchantId(), merchantIdCell.getStringCellValue());

        Row dateRow = sheet.getRow(14);
        Cell fromTimeCell = dateRow.getCell(1);
        assertEquals(
                "[$-FC19]dd\\ mmmm\\ yyyy\\ \\г\\.;@",
                fromTimeCell.getCellStyle().getDataFormatString()
        );
        assertEquals(shopAccounting.getFromTime(), fromTimeCell.getDateCellValue());
        Cell toTimeCell = dateRow.getCell(3);
        assertEquals(
                "[$-FC19]dd\\ mmmm\\ yyyy\\ \\г\\.;@",
                toTimeCell.getCellStyle().getDataFormatString()
        );
        assertEquals(shopAccounting.getToTime(), toTimeCell.getDateCellValue());

        Cell openingBalanceCell = sheet.getRow(23).getCell(3);
        assertEquals("#,##0.00", openingBalanceCell.getCellStyle().getDataFormatString());
        assertEquals(
                BigDecimal.valueOf(shopAccounting.getOpeningBalance()),
                BigDecimal.valueOf(openingBalanceCell.getNumericCellValue())
        );

        Cell closingBalanceCell = sheet.getRow(29).getCell(3);
        assertEquals("#,##0.00", closingBalanceCell.getCellStyle().getDataFormatString());
        assertEquals(
                BigDecimal.valueOf(shopAccounting.getClosingBalance()),
                BigDecimal.valueOf(closingBalanceCell.getNumericCellValue())
        );

        Cell fundsAcquiredCell = sheet.getRow(17).getCell(3);
        assertEquals("#,##0.00", fundsAcquiredCell.getCellStyle().getDataFormatString());
        assertEquals(
                BigDecimal.valueOf(shopAccounting.getFundsAcquired()),
                BigDecimal.valueOf(fundsAcquiredCell.getNumericCellValue())
        );
        assertEquals(
                BigDecimal.valueOf(fundsAcquiredCell.getNumericCellValue()),
                BigDecimal.valueOf(sheet.getRow(24).getCell(3).getNumericCellValue())
        );

        Cell feeChargedCell = sheet.getRow(19).getCell(3);
        assertEquals("#,##0.00", feeChargedCell.getCellStyle().getDataFormatString());
        assertEquals(
                BigDecimal.valueOf(shopAccounting.getFeeCharged()),
                BigDecimal.valueOf(feeChargedCell.getNumericCellValue())
        );
        assertEquals(
                BigDecimal.valueOf(feeChargedCell.getNumericCellValue()),
                BigDecimal.valueOf(sheet.getRow(25).getCell(3).getNumericCellValue())
        );

        Files.delete(tempFile);
    }

}
