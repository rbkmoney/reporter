package com.rbkmoney.reporter.service;

import com.rbkmoney.damsel.domain.BankCardPaymentSystem;
import com.rbkmoney.damsel.merch_stat.*;
import com.rbkmoney.reporter.AbstractIntegrationTest;
import com.rbkmoney.reporter.domain.enums.ReportStatus;
import com.rbkmoney.reporter.domain.tables.pojos.Report;
import com.rbkmoney.reporter.model.Payment;
import com.rbkmoney.reporter.model.Refund;
import com.rbkmoney.reporter.service.impl.PaymentRegistryTemplateImpl;
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
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.github.benas.randombeans.api.EnhancedRandom.random;
import static org.junit.Assert.assertEquals;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;

/**
 * Created by tolkonepiu on 12/07/2017.
 */
public class PaymentRegistryTemplateServiceTest extends AbstractIntegrationTest {

    @Autowired
    private PaymentRegistryTemplateImpl templateService;

    @MockBean
    private StatisticService statisticService;

    @MockBean
    private PartyService partyService;

    @Test
    public void testProcessPaymentRegistryTemplate() throws IOException, TException {
        Path tempFile = Files.createTempFile("registry_of_act_", "_test_report.xlsx");
        System.out.println("Registry of act report generated on " + tempFile.toAbsolutePath().toString());

        List<StatPayment> paymentList = new ArrayList<>();
        for (int i = 0; i < 3; ++i) {
            StatPayment payment = new StatPayment();
            payment.setId("id" + i);
            payment.setStatus(InvoicePaymentStatus.captured(new InvoicePaymentCaptured("201" + i + "-03-22T06:12:27Z")));
            PaymentResourcePayer paymentResourcePayer = new PaymentResourcePayer(PaymentTool.bank_card(new BankCard("token", null, "424" + i, "56789" + i)), "sessionId");
            paymentResourcePayer.setEmail("abc" + i + "@mail.ru");
            payment.setPayer(Payer.payment_resource(paymentResourcePayer));
            payment.setAmount(123L + i);
            payment.setFee(2L + i);
            payment.setShopId("shopId"+i);
            paymentList.add(payment);
        }

        List<StatRefund> refundList = new ArrayList<>();
        for (int i = 0; i < 3; ++i) {
            StatRefund refund = new StatRefund();
            refund.setId("id" + i);
            refund.setPaymentId("paymentId" + i);
            refund.setInvoiceId("invoiceId" + i);
            refund.setStatus(InvoicePaymentRefundStatus.succeeded(new InvoicePaymentRefundSucceeded("201" + i + "-03-22T06:12:27Z")));
            refund.setAmount(123L + i);
            refund.setShopId("shopId"+i);
            refundList.add(refund);
        }

        given(statisticService.getPayments(any(), any(), any(), any(), any()))
                .willReturn(paymentList);

        given(statisticService.getRefunds(any(), any(), any(), any(), any()))
                .willReturn(refundList);

        StatPayment payment = new StatPayment();
        payment.setStatus(InvoicePaymentStatus.captured(new InvoicePaymentCaptured("2018-03-22T06:12:27Z")));
        PaymentResourcePayer paymentResourcePayer = new PaymentResourcePayer(PaymentTool.bank_card(new BankCard("token", null, "4249", "567890")), "sessionId");
        paymentResourcePayer.setEmail("xyz@mail.ru");
        payment.setPayer(Payer.payment_resource(paymentResourcePayer));

        given(statisticService.getPayment(any(), any(), any()))
                .willReturn(payment);

        Map<String, String> shops  = new HashMap<>();
        shops.put("shopId0", "http://0ch.ru/b");
        shops.put("shopId1", "http://1ch.ru/b");
        shops.put("shopId2", "http://2ch.ru/b");

        given(partyService.getShopUrls(any(), any(), any()))
                .willReturn(shops);

        Report report = new Report(random(Long.class), random(LocalDateTime.class), random(LocalDateTime.class), random(LocalDateTime.class), random(String.class), random(String.class), random(ReportStatus.class), "Europe/Moscow", random(String.class), true);


        try {
            templateService.processReportTemplate(report, Files.newOutputStream(tempFile));
            Workbook wb = new XSSFWorkbook(Files.newInputStream(tempFile));
            Sheet sheet = wb.getSheetAt(0);


            String from = TimeUtil.toLocalizedDate(report.getFromTime().toInstant(ZoneOffset.UTC), ZoneId.of(report.getTimezone()));
            String to = TimeUtil.toLocalizedDate(report.getToTime().toInstant(ZoneOffset.UTC), ZoneId.of(report.getTimezone()));

            Cell paymentsHeaderCell = sheet.getRow(0).getCell(0);
            assertEquals(String.format("Платежи за период с %s по %s", from, to), paymentsHeaderCell.getStringCellValue());

            Cell paymentsTotalSum = sheet.getRow(5).getCell(3);
            assertEquals(paymentList.stream().mapToLong(StatPayment::getAmount).sum(), (long) paymentsTotalSum.getNumericCellValue());

            Cell refundsHeaderCell = sheet.getRow(8).getCell(0);
            assertEquals(String.format("Возвраты за период с %s по %s", from, to), refundsHeaderCell.getStringCellValue());

            Cell refundsTotalSum = sheet.getRow(13).getCell(3);
            assertEquals(refundList.stream().mapToLong(StatRefund::getAmount).sum(), (long) refundsTotalSum.getNumericCellValue());

        } finally {
            Files.deleteIfExists(tempFile);
        }
    }

}
