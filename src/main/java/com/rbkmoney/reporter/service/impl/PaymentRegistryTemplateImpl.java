package com.rbkmoney.reporter.service.impl;

import com.rbkmoney.reporter.dao.mapper.dto.PaymentRegistryReportData;
import com.rbkmoney.reporter.dao.mapper.dto.RefundPaymentRegistryReportData;
import com.rbkmoney.reporter.domain.enums.ReportType;
import com.rbkmoney.reporter.domain.tables.pojos.Report;
import com.rbkmoney.reporter.service.PartyService;
import com.rbkmoney.reporter.service.ReportingService;
import com.rbkmoney.reporter.service.TemplateService;
import com.rbkmoney.reporter.util.FormatUtil;
import com.rbkmoney.reporter.util.TimeUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellUtil;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.OutputStream;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

@Component
@Qualifier("paymentRegistryTemplate")
@Slf4j
@RequiredArgsConstructor
public class PaymentRegistryTemplateImpl implements TemplateService {

    private final PartyService partyService;
    private final ReportingService reportingService;

    @Override
    public boolean accept(ReportType reportType) {
        return reportType == ReportType.payment_registry || reportType == ReportType.provision_of_service;
    }

    @Override
    public void processReportTemplate(Report report, OutputStream outputStream) throws
            IOException {
        ZoneId reportZoneId = ZoneId.of(report.getTimezone());
        String fromTime = TimeUtil.toLocalizedDate(report.getFromTime().toInstant(ZoneOffset.UTC), reportZoneId);
        String toTime = TimeUtil.toLocalizedDate(report.getToTime().minusNanos(1).toInstant(ZoneOffset.UTC), reportZoneId);

        String shopUrl = partyService.getShopUrl(report.getPartyId(), report.getPartyShopId(), report.getCreatedAt().toInstant(ZoneOffset.UTC));

        List<PaymentRegistryReportData> payments = reportingService.getPaymentRegistryReportData(
                report.getPartyId(),
                report.getPartyShopId(),
                report.getFromTime(),
                report.getToTime()
        );

        AtomicLong totalAmnt = new AtomicLong();
        AtomicLong totalPayoutAmnt = new AtomicLong();

        SXSSFWorkbook wb = null;
        try {
            wb = new SXSSFWorkbook(100);// keep 100 rows in memory, exceeding rows will be flushed to disk
            Sheet sh = wb.createSheet();
            sh.setDefaultColumnWidth(20);
            int rownum = 0;
            Row rowFirstPayments = sh.createRow(rownum++);

            for (int i = 0; i < 8; ++i) {
                rowFirstPayments.createCell(i);
            }
            sh.addMergedRegion(new CellRangeAddress(rownum - 1, rownum - 1, 0, 7));
            Cell cellFirstPayments = rowFirstPayments.getCell(0);
            cellFirstPayments.setCellValue(String.format("Платежи за период с %s по %s", fromTime, toTime));
            CellUtil.setAlignment(cellFirstPayments, HorizontalAlignment.CENTER);
            Font font = wb.createFont();
            font.setBold(true);
            CellUtil.setFont(cellFirstPayments, font);

            Row rowSecondPayments = sh.createRow(rownum++);
            CellStyle greyStyle = wb.createCellStyle();
            greyStyle.setFillBackgroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            greyStyle.setFillPattern(FillPatternType.LESS_DOTS);
            for (int i = 0; i < 8; ++i) {
                Cell cell = rowSecondPayments.createCell(i);
                CellUtil.setAlignment(cell, HorizontalAlignment.CENTER);
                cell.setCellStyle(greyStyle);
                CellUtil.setFont(cell, font);
            }
            rowSecondPayments.getCell(0).setCellValue("Дата");
            rowSecondPayments.getCell(1).setCellValue("Id платежа");
            rowSecondPayments.getCell(2).setCellValue("Метод оплаты");
            rowSecondPayments.getCell(3).setCellValue("Сумма платежа");
            rowSecondPayments.getCell(4).setCellValue("Сумма к выводу");
            rowSecondPayments.getCell(5).setCellValue("Email плательщика");
            rowSecondPayments.getCell(6).setCellValue("URL магазина");
            rowSecondPayments.getCell(7).setCellValue("Назначение платежа");

            for (PaymentRegistryReportData p : payments) {
                Row row = sh.createRow(rownum++);
                row.createCell(0).setCellValue(p.getInvoiceId() + "." + p.getPaymentId());
                row.createCell(1).setCellValue(p.getEventCreatedAt().toString());
                row.createCell(2).setCellValue(p.getPaymentTool().getLiteral());
                row.createCell(3).setCellValue(FormatUtil.formatCurrency(p.getPaymentAmount()));
                row.createCell(4).setCellValue(FormatUtil.formatCurrency(p.getPaymentAmount() - p.getPaymentFee()));
                row.createCell(5).setCellValue(p.getPaymentEmail());
                row.createCell(6).setCellValue(shopUrl);
                row.createCell(7).setCellValue(p.getInvoiceProduct());
                totalAmnt.addAndGet(p.getPaymentAmount());
                totalPayoutAmnt.addAndGet(p.getPaymentAmount() - p.getPaymentFee());
            }

            CellStyle borderStyle = wb.createCellStyle();
            borderStyle.setBorderBottom(BorderStyle.MEDIUM);
            borderStyle.setBorderTop(BorderStyle.MEDIUM);
            borderStyle.setBorderRight(BorderStyle.MEDIUM);
            borderStyle.setBorderLeft(BorderStyle.MEDIUM);

            //---- total amount ---------
            Row rowTotalPaymentAmount = sh.createRow(rownum++);
            for (int i = 0; i < 5; ++i) {
                Cell cell = rowTotalPaymentAmount.createCell(i);
                cell.setCellStyle(borderStyle);
                CellUtil.setFont(cell, font);
            }
            sh.addMergedRegion(new CellRangeAddress(rownum - 1, rownum - 1, 0, 2));
            Cell cellTotalPaymentAmount = rowTotalPaymentAmount.getCell(0);
            cellTotalPaymentAmount.setCellValue("Сумма");
            CellUtil.setAlignment(cellTotalPaymentAmount, HorizontalAlignment.CENTER);
            rowTotalPaymentAmount.getCell(3).setCellValue(FormatUtil.formatCurrency(totalAmnt.longValue()));
            rowTotalPaymentAmount.getCell(4).setCellValue(FormatUtil.formatCurrency(totalPayoutAmnt.longValue()));

            //-----skip rows -------
            sh.createRow(rownum++);
            sh.createRow(rownum++);

            Row rowFirstRefunds = sh.createRow(rownum++);
            for (int i = 0; i < 8; ++i) {
                rowFirstRefunds.createCell(i);
            }
            sh.addMergedRegion(new CellRangeAddress(rownum - 1, rownum - 1, 0, 7));
            Cell cellFirstRefunds = rowFirstRefunds.getCell(0);
            cellFirstRefunds.setCellValue(String.format("Возвраты за период с %s по %s", fromTime, toTime));
            CellUtil.setAlignment(cellFirstRefunds, HorizontalAlignment.CENTER);
            CellUtil.setFont(cellFirstRefunds, font);
            Row rowSecondRefunds = sh.createRow(rownum++);
            for (int i = 0; i < 8; ++i) {
                Cell cell = rowSecondRefunds.createCell(i);
                CellUtil.setAlignment(cell, HorizontalAlignment.CENTER);
                cell.setCellStyle(greyStyle);
                CellUtil.setFont(cell, font);
            }
            rowSecondRefunds.getCell(0).setCellValue("Дата возврата");
            rowSecondRefunds.getCell(1).setCellValue("Дата платежа");
            rowSecondRefunds.getCell(2).setCellValue("Id платежа");
            rowSecondRefunds.getCell(3).setCellValue("Сумма возврата");
            rowSecondRefunds.getCell(4).setCellValue("Метод оплаты");
            rowSecondRefunds.getCell(5).setCellValue("Email плательщика");
            rowSecondRefunds.getCell(6).setCellValue("URL магазина");
            rowSecondRefunds.getCell(7).setCellValue("Назначение платежа");

            AtomicLong totalRefundAmnt = new AtomicLong();
            List<RefundPaymentRegistryReportData> refunds = reportingService.getRefundPaymentRegistryReportData(
                    report.getPartyId(),
                    report.getPartyShopId(),
                    report.getFromTime(),
                    report.getToTime()
            );

            for (RefundPaymentRegistryReportData r : refunds) {
                Row row = sh.createRow(rownum++);
                row.createCell(0).setCellValue(r.getRefundEventCreatedAt().toString());
                row.createCell(1).setCellValue(r.getPaymentEventCreatedAt().toString());
                row.createCell(2).setCellValue(r.getInvoiceId() + "." + r.getPaymentId());
                row.createCell(3).setCellValue(FormatUtil.formatCurrency(r.getRefundAmount()));
                row.createCell(4).setCellValue(Optional.ofNullable(r.getPaymentTool()).map(paymentTool -> paymentTool.getLiteral()).orElse(null));
                row.createCell(5).setCellValue(r.getPaymentEmail());
                row.createCell(6).setCellValue(shopUrl);
                row.createCell(7).setCellValue(r.getInvoiceProduct());
                totalRefundAmnt.addAndGet(r.getRefundAmount());
            }

            //---- total refund amount ---------
            Row rowTotalRefundAmount = sh.createRow(rownum++);
            for (int i = 0; i < 4; ++i) {
                Cell cell = rowTotalRefundAmount.createCell(i);
                cell.setCellStyle(borderStyle);
                CellUtil.setFont(cell, font);
            }
            sh.addMergedRegion(new CellRangeAddress(rownum - 1, rownum - 1, 0, 2));
            Cell cellTotalRefundAmount = rowTotalRefundAmount.getCell(0);
            cellTotalRefundAmount.setCellValue("Сумма");
            CellUtil.setAlignment(cellTotalRefundAmount, HorizontalAlignment.CENTER);
            rowTotalRefundAmount.getCell(3).setCellValue(FormatUtil.formatCurrency(totalRefundAmnt.longValue()));

            wb.write(outputStream);
        } catch (Throwable e) {
            log.error("Problem with fill of report", e);
        } finally {
            outputStream.close();
            if (wb != null) {
                wb.dispose();
            }
        }
    }
}
