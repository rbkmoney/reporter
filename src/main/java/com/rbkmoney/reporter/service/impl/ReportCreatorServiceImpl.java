package com.rbkmoney.reporter.service.impl;

import com.rbkmoney.damsel.merch_stat.*;
import com.rbkmoney.reporter.exception.NotFoundException;
import com.rbkmoney.reporter.model.ReportCreatorDto;
import com.rbkmoney.reporter.service.ReportCreatorService;
import com.rbkmoney.reporter.util.FormatUtil;
import com.rbkmoney.reporter.util.TimeUtil;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.SpreadsheetVersion;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellUtil;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.ZoneId;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class ReportCreatorServiceImpl implements ReportCreatorService {

    private int limit = SpreadsheetVersion.EXCEL2007.getLastRowIndex();

    public void setLimit(int limit) {
        this.limit = limit;
    }

    @Override
    public void createReport(ReportCreatorDto reportCreatorDto) throws IOException {
        try (SXSSFWorkbook wb = new SXSSFWorkbook(100)) {
            Sheet sh = createSheet(wb);
            AtomicInteger rownum = new AtomicInteger(0);

            createPaymentsHeadRow(reportCreatorDto, wb, sh, rownum);
            createPaymentsColumnsDesciptionRow(wb, sh, rownum);

            AtomicLong totalAmnt = new AtomicLong();
            AtomicLong totalPayoutAmnt = new AtomicLong();
            AtomicLong totalRefundAmnt = new AtomicLong();

            while (reportCreatorDto.getPaymentsIterator().hasNext()) {
                createPaymentRow(reportCreatorDto, sh, totalAmnt, totalPayoutAmnt, rownum, reportCreatorDto.getPaymentsIterator().next());
                sh = checkAndReset(wb, sh, rownum);
            }
            sh = checkAndReset(wb, sh, rownum);
            createTotalAmountRow(wb, sh, totalAmnt, totalPayoutAmnt, rownum);
            sh = checkAndReset(wb, sh, rownum);
            sh.createRow(rownum.getAndIncrement());
            sh = checkAndReset(wb, sh, rownum);
            sh.createRow(rownum.getAndIncrement());
            sh = checkAndReset(wb, sh, rownum);
            createRefundsHeadRow(reportCreatorDto, wb, sh, rownum);
            sh = checkAndReset(wb, sh, rownum);
            createRefundsColumnsDescriptionRow(wb, sh, rownum);
            sh = checkAndReset(wb, sh, rownum);

            while (reportCreatorDto.getRefundsIterator().hasNext()) {
                createRefundRow(reportCreatorDto, sh, totalRefundAmnt, rownum, reportCreatorDto.getRefundsIterator().next());
                sh = checkAndReset(wb, sh, rownum);
            }
            sh = checkAndReset(wb, sh, rownum);
            createTotalRefundAmountRow(wb, sh, totalRefundAmnt, rownum);
            wb.write(reportCreatorDto.getOutputStream());
            reportCreatorDto.getOutputStream().close();
            wb.dispose();
        }
    }

    private Sheet checkAndReset(SXSSFWorkbook wb, Sheet sh, AtomicInteger rownum) {
        if (rownum.get() >= limit) {
            sh = createSheet(wb);
            rownum.set(0);
        }
        return sh;
    }

    private Sheet createSheet(SXSSFWorkbook wb) {
        Sheet sh = wb.createSheet();
        sh.setDefaultColumnWidth(20);
        return sh;
    }

    private void createTotalRefundAmountRow(Workbook wb, Sheet sh, AtomicLong totalRefundAmnt, AtomicInteger rownum) {
        Row rowTotalRefundAmount = sh.createRow(rownum.getAndIncrement());
        for (int i = 0; i < 4; ++i) {
            Cell cell = rowTotalRefundAmount.createCell(i);
            cell.setCellStyle(createGreyCellStyle(wb));
            CellUtil.setFont(cell, createBoldFont(wb));
        }
        sh.addMergedRegion(new CellRangeAddress(rownum.get() - 1, rownum.get() - 1, 0, 2));
        Cell cellTotalRefundAmount = rowTotalRefundAmount.getCell(0);
        cellTotalRefundAmount.setCellValue("Сумма");
        CellUtil.setAlignment(cellTotalRefundAmount, HorizontalAlignment.CENTER);
        rowTotalRefundAmount.getCell(3).setCellValue(FormatUtil.formatCurrency(totalRefundAmnt.longValue()));
    }

    private void createRefundRow(ReportCreatorDto reportCreatorDto, Sheet sh,
                                 AtomicLong totalRefundAmnt, AtomicInteger rownum, StatRefund r) {
        ZoneId reportZoneId = ZoneId.of(reportCreatorDto.getReport().getTimezone());

        Row row = sh.createRow(rownum.getAndIncrement());
        StatPayment statPayment = reportCreatorDto.getStatisticService().getCapturedPayment(reportCreatorDto.getReport().getPartyId(), reportCreatorDto.getReport().getPartyShopId(), r.getInvoiceId(), r.getPaymentId());
        row.createCell(0).setCellValue(TimeUtil.toLocalizedDateTime(r.getStatus().getSucceeded().getAt(), reportZoneId));
        row.createCell(1).setCellValue(TimeUtil.toLocalizedDateTime(statPayment.getStatus().getCaptured().getAt(), reportZoneId));
        row.createCell(2).setCellValue(r.getInvoiceId() + "." + r.getPaymentId());
        row.createCell(3).setCellValue(FormatUtil.formatCurrency(r.getAmount()));
        String paymentTool = null;
        if (statPayment.getPayer().isSetPaymentResource()) {
            paymentTool = statPayment.getPayer().getPaymentResource().getPaymentTool().getSetField().getFieldName();
        }
        row.createCell(4).setCellValue(paymentTool);
        totalRefundAmnt.addAndGet(r.getAmount());
        String payerEmail = null;
        if (statPayment.getPayer().isSetPaymentResource()) {
            payerEmail = statPayment.getPayer().getPaymentResource().getEmail();
        }
        row.createCell(5).setCellValue(payerEmail);
        row.createCell(6).setCellValue(reportCreatorDto.getShopUrls().get(r.getShopId()));
        String purpose = reportCreatorDto.getPurposes().get(r.getInvoiceId());
        if (purpose == null) {
            StatInvoice invoice = reportCreatorDto.getStatisticService().getInvoice(r.getInvoiceId());
            purpose = invoice.getProduct();
        }
        row.createCell(7).setCellValue(purpose);
        row.createCell(8).setCellValue(r.getId());
        row.createCell(9).setCellValue(r.getReason());
        row.createCell(10).setCellValue(r.getCurrencySymbolicCode());
    }

    private void createRefundsColumnsDescriptionRow(Workbook wb, Sheet sh, AtomicInteger rownum) {
        Row rowSecondRefunds = sh.createRow(rownum.getAndIncrement());
        for (int i = 0; i < 11; ++i) {
            Cell cell = rowSecondRefunds.createCell(i);
            CellUtil.setAlignment(cell, HorizontalAlignment.CENTER);
            cell.setCellStyle(createGreyCellStyle(wb));
            CellUtil.setFont(cell, createBoldFont(wb));
        }
        rowSecondRefunds.getCell(0).setCellValue("Дата возврата");
        rowSecondRefunds.getCell(1).setCellValue("Дата платежа");
        rowSecondRefunds.getCell(2).setCellValue("Id платежа");
        rowSecondRefunds.getCell(3).setCellValue("Сумма возврата");
        rowSecondRefunds.getCell(4).setCellValue("Метод оплаты");
        rowSecondRefunds.getCell(5).setCellValue("Email плательщика");
        rowSecondRefunds.getCell(6).setCellValue("URL магазина");
        rowSecondRefunds.getCell(7).setCellValue("Назначение платежа");
        rowSecondRefunds.getCell(8).setCellValue("Id возврата");
        rowSecondRefunds.getCell(9).setCellValue("Причина возврата");
        rowSecondRefunds.getCell(10).setCellValue("Валюта");
    }

    private void createRefundsHeadRow(ReportCreatorDto reportCreatorDto, Workbook wb, Sheet sh, AtomicInteger rownum) {
        Row rowFirstRefunds = sh.createRow(rownum.getAndIncrement());
        for (int i = 0; i < 11; ++i) {
            rowFirstRefunds.createCell(i);
        }
        sh.addMergedRegion(new CellRangeAddress(rownum.get() - 1, rownum.get() - 1, 0, 7));
        Cell cellFirstRefunds = rowFirstRefunds.getCell(0);
        cellFirstRefunds.setCellValue(String.format("Возвраты за период с %s по %s", reportCreatorDto.getFromTime(), reportCreatorDto.getToTime()));
        CellUtil.setAlignment(cellFirstRefunds, HorizontalAlignment.CENTER);
        CellUtil.setFont(cellFirstRefunds, createBoldFont(wb));
    }

    private void createTotalAmountRow(Workbook wb, Sheet sh, AtomicLong totalAmnt, AtomicLong totalPayoutAmnt, AtomicInteger rownum) {
        Row rowTotalPaymentAmount = sh.createRow(rownum.getAndIncrement());
        for (int i = 0; i < 5; ++i) {
            Cell cell = rowTotalPaymentAmount.createCell(i);
            cell.setCellStyle(createGreyCellStyle(wb));
            CellUtil.setFont(cell, createBoldFont(wb));
        }
        sh.addMergedRegion(new CellRangeAddress(rownum.get() - 1, rownum.get() - 1, 0, 2));
        Cell cellTotalPaymentAmount = rowTotalPaymentAmount.getCell(0);
        cellTotalPaymentAmount.setCellValue("Сумма");
        CellUtil.setAlignment(cellTotalPaymentAmount, HorizontalAlignment.CENTER);
        rowTotalPaymentAmount.getCell(3).setCellValue(FormatUtil.formatCurrency(totalAmnt.longValue()));
        rowTotalPaymentAmount.getCell(4).setCellValue(FormatUtil.formatCurrency(totalPayoutAmnt.longValue()));
    }

    private CellStyle createBorderStyle(SXSSFWorkbook wb) {
        CellStyle borderStyle = wb.createCellStyle();
        borderStyle.setBorderBottom(BorderStyle.MEDIUM);
        borderStyle.setBorderTop(BorderStyle.MEDIUM);
        borderStyle.setBorderRight(BorderStyle.MEDIUM);
        borderStyle.setBorderLeft(BorderStyle.MEDIUM);
        return borderStyle;
    }

    private void createPaymentRow(ReportCreatorDto reportCreatorDto,
                                  Sheet sh,
                                  AtomicLong totalAmnt,
                                  AtomicLong totalPayoutAmnt,
                                  AtomicInteger rownum,
                                  StatPayment payment) {
        ZoneId reportZoneId = ZoneId.of(reportCreatorDto.getReport().getTimezone());
        Row row = sh.createRow(rownum.getAndIncrement());
        row.createCell(0).setCellValue(payment.getInvoiceId() + "." + payment.getId());
        row.createCell(1).setCellValue(
                TimeUtil.toLocalizedDateTime(payment.getStatus().getCaptured().getAt(), reportZoneId));
        PaymentTool paymentTool = getPaymentTool(payment.getPayer());
        row.createCell(2).setCellValue(paymentTool.getSetField().getFieldName());
        row.createCell(3).setCellValue(FormatUtil.formatCurrency(payment.getAmount()));
        row.createCell(4).setCellValue(FormatUtil.formatCurrency(payment.getAmount() - payment.getFee()));
        totalAmnt.addAndGet(payment.getAmount());
        totalPayoutAmnt.addAndGet(payment.getAmount() - payment.getFee());
        String payerEmail = getEmail(payment.getPayer());
        row.createCell(5).setCellValue(payerEmail);
        row.createCell(6).setCellValue(reportCreatorDto.getShopUrls().get(payment.getShopId()));
        String purpose = reportCreatorDto.getPurposes().get(payment.getInvoiceId());
        if (purpose == null) {
            StatInvoice invoice = reportCreatorDto.getStatisticService().getInvoice(payment.getInvoiceId());
            purpose = invoice.getProduct();
        }
        row.createCell(7).setCellValue(purpose);
        row.createCell(8).setCellValue(FormatUtil.formatCurrency(payment.getFee()));
        row.createCell(9).setCellValue(payment.getCurrencySymbolicCode());
    }

    private void createPaymentsColumnsDesciptionRow(Workbook wb, Sheet sh, AtomicInteger rownum) {
        Row rowSecondPayments = sh.createRow(rownum.getAndIncrement());
        for (int i = 0; i < 10; ++i) {
            Cell cell = rowSecondPayments.createCell(i);
            CellUtil.setAlignment(cell, HorizontalAlignment.CENTER);
            cell.setCellStyle(createGreyCellStyle(wb));
            CellUtil.setFont(cell, createBoldFont(wb));
        }
        rowSecondPayments.getCell(0).setCellValue("Id платежа");
        rowSecondPayments.getCell(1).setCellValue("Дата");
        rowSecondPayments.getCell(2).setCellValue("Метод оплаты");
        rowSecondPayments.getCell(3).setCellValue("Сумма платежа");
        rowSecondPayments.getCell(4).setCellValue("Сумма к выводу");
        rowSecondPayments.getCell(5).setCellValue("Email плательщика");
        rowSecondPayments.getCell(6).setCellValue("URL магазина");
        rowSecondPayments.getCell(7).setCellValue("Назначение платежа");
        rowSecondPayments.getCell(8).setCellValue("Комиссия");
        rowSecondPayments.getCell(9).setCellValue("Валюта");
    }

    private void createPaymentsHeadRow(ReportCreatorDto reportCreatorDto, Workbook wb, Sheet sh, AtomicInteger rownum) {
        Row rowFirstPayments = sh.createRow(rownum.getAndIncrement());

        for (int i = 0; i < 10; ++i) {
            rowFirstPayments.createCell(i);
        }
        sh.addMergedRegion(new CellRangeAddress(rownum.get() - 1, rownum.get() - 1, 0, 7));
        Cell cellFirstPayments = rowFirstPayments.getCell(0);
        cellFirstPayments.setCellValue(String.format("Платежи за период с %s по %s", reportCreatorDto.getFromTime(), reportCreatorDto.getToTime()));
        CellUtil.setAlignment(cellFirstPayments, HorizontalAlignment.CENTER);
        CellUtil.setFont(cellFirstPayments, createBoldFont(wb));
    }

    private CellStyle createGreyCellStyle(Workbook wb) {
        CellStyle greyStyle = wb.createCellStyle();
        greyStyle.setFillBackgroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        greyStyle.setFillPattern(FillPatternType.LESS_DOTS);
        return greyStyle;
    }

    private Font createBoldFont(Workbook wb) {
        Font font = wb.createFont();
        font.setBold(true);
        return font;
    }


    private PaymentTool getPaymentTool(Payer payer) {
        switch (payer.getSetField()) {
            case PAYMENT_RESOURCE:
                return payer.getPaymentResource().getPaymentTool();
            case CUSTOMER:
                return payer.getCustomer().getPaymentTool();
            case RECURRENT:
                return payer.getRecurrent().getPaymentTool();
            default:
                throw new NotFoundException(String.format("Payer type '%s' not found", payer.getSetField()));
        }
    }

    private static String getEmail(Payer payer) {
        switch (payer.getSetField()) {
            case PAYMENT_RESOURCE:
                return payer.getPaymentResource().getEmail();
            case CUSTOMER:
                return payer.getCustomer().getEmail();
            case RECURRENT:
                return payer.getRecurrent().getEmail();
            default:
                throw new NotFoundException(String.format("Payer type '%s' not found", payer.getSetField()));
        }
    }
}
