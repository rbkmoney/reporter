package com.rbkmoney.reporter.service.impl;

import com.rbkmoney.damsel.merch_stat.*;
import com.rbkmoney.reporter.domain.enums.ReportType;
import com.rbkmoney.reporter.domain.tables.pojos.ContractMeta;
import com.rbkmoney.reporter.domain.tables.pojos.Report;
import com.rbkmoney.reporter.model.Payment;
import com.rbkmoney.reporter.model.Refund;
import com.rbkmoney.reporter.service.PartyService;
import com.rbkmoney.reporter.service.StatisticService;
import com.rbkmoney.reporter.service.TemplateService;
import com.rbkmoney.reporter.util.FormatUtil;
import com.rbkmoney.reporter.util.TimeUtil;
import org.jxls.common.Context;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.OutputStream;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Component
public class PaymentRegistryTemplateImpl implements TemplateService {

    private final StatisticService statisticService;

    private final PartyService partyService;

    private final Resource resource;

    @Autowired
    public PaymentRegistryTemplateImpl(
            StatisticService statisticService,
            PartyService partyService,
            @Value("${report.type.pr.path|classpath:/templates/payment_registry.xlsx}") ClassPathResource resource
    ) {
        this.statisticService = statisticService;
        this.partyService = partyService;
        this.resource = resource;
    }

    @Override
    public boolean accept(ReportType reportType, ContractMeta contractMeta) {
        return reportType == ReportType.payment_registry
                || (contractMeta.getReportType() == ReportType.provision_of_service && contractMeta.getNeedReference());
    }

    @Override
    public void processReportTemplate(Report report, ContractMeta contractMeta, OutputStream outputStream) throws
            IOException {
        ZoneId reportZoneId = ZoneId.of(report.getTimezone());
        Map<String, String> shopUrls = partyService.getShopUrls(report.getPartyId(), report.getPartyContractId(), report.getCreatedAt().toInstant(ZoneOffset.UTC));

        Map<String, String> purposes = statisticService.getInvoices(
                report.getPartyId(),
                report.getPartyContractId(),
                report.getFromTime().toInstant(ZoneOffset.UTC),
                report.getToTime().toInstant(ZoneOffset.UTC)
        ).stream().collect(Collectors.toMap(StatInvoice::getId, StatInvoice::getProduct));

        AtomicLong totalAmnt = new AtomicLong();
        AtomicLong totalPayoutAmnt = new AtomicLong();
        List<Payment> paymentList = statisticService.getPayments(
                report.getPartyId(),
                report.getPartyContractId(),
                report.getFromTime().toInstant(ZoneOffset.UTC),
                report.getToTime().toInstant(ZoneOffset.UTC),
                InvoicePaymentStatus.captured(new InvoicePaymentCaptured())
        ).stream().sorted(Comparator.comparing(p -> p.getStatus().getCaptured().getAt())).map(p -> {
            Payment payment = new Payment();
            payment.setId(p.getInvoiceId() + "." + p.getId());
            payment.setCapturedAt(TimeUtil.toLocalizedDateTime(p.getStatus().getCaptured().getAt(), reportZoneId));
            if (p.getPayer().isSetPaymentResource()) {
                payment.setPaymentTool(p.getPayer().getPaymentResource().getPaymentTool().getSetField().getFieldName());
            }
            payment.setAmount(FormatUtil.formatCurrency(p.getAmount()));
            payment.setPayoutAmount(FormatUtil.formatCurrency(p.getAmount() - p.getFee()));
            totalAmnt.addAndGet(p.getAmount());
            totalPayoutAmnt.addAndGet(p.getAmount() - p.getFee());
            if (p.getPayer().isSetPaymentResource()) {
                payment.setPayerEmail(p.getPayer().getPaymentResource().getEmail());
            }
            payment.setShopUrl(shopUrls.get(p.getShopId()));
            String purpose = purposes.get(p.getInvoiceId());
            if (purpose == null) {
                StatInvoice invoice = statisticService.getInvoice(p.getInvoiceId());
                purpose = invoice.getProduct();
            }
            payment.setPurpose(purpose);
            return payment;
        }).collect(Collectors.toList());

        AtomicLong totalRefundAmnt = new AtomicLong();
        List<Refund> refundList = statisticService.getRefunds(
                report.getPartyId(),
                report.getPartyContractId(),
                report.getFromTime().toInstant(ZoneOffset.UTC),
                report.getToTime().toInstant(ZoneOffset.UTC),
                InvoicePaymentRefundStatus.succeeded(new InvoicePaymentRefundSucceeded())
        ).stream().sorted(Comparator.comparing(r -> r.getStatus().getSucceeded().getAt())).map(r -> {
            Refund refund = new Refund();
            StatPayment statPayment = statisticService.getPayment(r.getInvoiceId(), r.getPaymentId());
            refund.setId(r.getId());
            refund.setPaymentId(r.getInvoiceId() + "." + r.getPaymentId());
            //TODO captured_at only
            if (statPayment.getStatus().isSetCaptured()) {
                refund.setPaymentCapturedAt(TimeUtil.toLocalizedDateTime(statPayment.getStatus().getCaptured().getAt(), reportZoneId));
            } else {
                refund.setPaymentCapturedAt(TimeUtil.toLocalizedDateTime(statPayment.getCreatedAt(), reportZoneId));
            }
            refund.setSucceededAt(TimeUtil.toLocalizedDateTime(r.getStatus().getSucceeded().getAt(), reportZoneId));
            if (statPayment.getPayer().isSetPaymentResource()) {
                refund.setPaymentTool(statPayment.getPayer().getPaymentResource().getPaymentTool().getSetField().getFieldName());
            }
            refund.setAmount(FormatUtil.formatCurrency(r.getAmount()));
            totalRefundAmnt.addAndGet(r.getAmount());
            if (statPayment.getPayer().isSetPaymentResource()) {
                refund.setPayerEmail(statPayment.getPayer().getPaymentResource().getEmail());
            }
            refund.setShopUrl(shopUrls.get(r.getShopId()));
            String purpose = purposes.get(r.getInvoiceId());
            if (purpose == null) {
                StatInvoice invoice = statisticService.getInvoice(r.getInvoiceId());
                purpose = invoice.getProduct();
            }
            refund.setPaymentPurpose(purpose);
            return refund;
        }).collect(Collectors.toList());

        Context context = new Context();
        context.putVar("payments", paymentList);
        context.putVar("refunds", refundList);
        context.putVar("fromTime", TimeUtil.toLocalizedDate(report.getFromTime().toInstant(ZoneOffset.UTC), reportZoneId));
        context.putVar("toTime", TimeUtil.toLocalizedDate(report.getToTime().minusNanos(1).toInstant(ZoneOffset.UTC), reportZoneId));
        context.putVar("totalAmnt", FormatUtil.formatCurrency(totalAmnt.longValue()));
        context.putVar("totalPayoutAmnt", FormatUtil.formatCurrency(totalPayoutAmnt.longValue()));
        context.putVar("totalRefundAmnt", FormatUtil.formatCurrency(totalRefundAmnt.longValue()));

        processTemplate(context, resource.getInputStream(), outputStream);
    }

}
