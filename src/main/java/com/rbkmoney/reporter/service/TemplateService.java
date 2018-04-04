package com.rbkmoney.reporter.service;

import com.rbkmoney.damsel.merch_stat.*;
import com.rbkmoney.reporter.ReportType;
import com.rbkmoney.reporter.domain.tables.pojos.Report;
import com.rbkmoney.reporter.exception.ReportNotFoundException;
import com.rbkmoney.reporter.model.PartyModel;
import com.rbkmoney.reporter.model.Payment;
import com.rbkmoney.reporter.model.Refund;
import com.rbkmoney.reporter.model.ShopAccountingModel;
import com.rbkmoney.reporter.util.TimeUtil;
import org.jxls.common.Context;
import org.jxls.util.JxlsHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.rbkmoney.reporter.util.TimeUtil.toZoneSameLocal;

@Service
public class TemplateService {

    private final PartyService partyService;

    private final StatisticService statisticService;

    @Autowired
    public TemplateService(PartyService partyService, StatisticService statisticService) {
        this.partyService = partyService;
        this.statisticService = statisticService;
    }

    public void processPaymentRegistryTemplate(List<StatPayment> payments, Instant fromTime, Instant toTime, OutputStream outputStream) throws IOException {
        Context context = new Context();
        context.putVar("payments", payments);
        context.putVar("fromTime", Date.from(fromTime));
        context.putVar("toTime", Date.from(toTime));

        processTemplate(context, ReportType.payment_registry, outputStream);
    }

    public void processRegistryOfActTemplate(List<Payment> payments, List<Refund> refunds, Instant fromTime, Instant toTime, ZoneId zoneId, OutputStream outputStream) throws IOException {
        Context context = new Context();
        context.putVar("payments", payments);
        context.putVar("refunds", refunds);
        context.putVar("fromTime", TimeUtil.toLocalizedDate(fromTime, zoneId));
        context.putVar("toTime", TimeUtil.toLocalizedDate(toTime.minusMillis(1), zoneId));
        context.putVar("totalAmnt", payments.stream().mapToLong(Payment::getAmount).sum());
        context.putVar("totalPayoutAmnt", payments.stream().mapToLong(Payment::getPayoutAmount).sum());
        context.putVar("totalRefundAmnt", refunds.stream().mapToLong(Refund::getAmount).sum());

        processTemplate(context, ReportType.registry_of_act, outputStream);
    }

    public void processProvisionOfServiceTemplate(PartyModel partyModel, ShopAccountingModel shopAccountingModel, Instant fromTime, Instant toTime, ZoneId zoneId, OutputStream outputStream) throws IOException {
        Context context = new Context();
        context.putVar("shopAccounting", shopAccountingModel);
        context.putVar("partyRepresentation", partyModel);
        context.putVar("fromTime", Date.from(toZoneSameLocal(fromTime, zoneId)));
        context.putVar("toTime", Date.from(toZoneSameLocal(toTime, zoneId).minusMillis(1)));

        processTemplate(context, ReportType.provision_of_service, outputStream);
    }

    public void processReportTemplate(Report report, OutputStream outputStream) throws IOException {
        Instant fromTime = report.getFromTime().toInstant(ZoneOffset.UTC);
        Instant toTime = report.getToTime().toInstant(ZoneOffset.UTC);
        Instant createdAt = report.getCreatedAt().toInstant(ZoneOffset.UTC);
        ZoneId zoneId = ZoneId.of(report.getTimezone());

        ReportType reportType = ReportType.valueOf(report.getType());

        PartyModel partyModel = partyService.getPartyRepresentation(
                report.getPartyId(),
                report.getPartyShopId(),
                createdAt
        );

        //TODO
        String contractId = "sss";
        Map<String, String> shopUrls = partyService.getShopUrls(report.getPartyId(), contractId, createdAt);

        switch (reportType) {
            case provision_of_service:
                ShopAccountingModel shopAccountingModel = statisticService.getShopAccounting(
                        report.getPartyId(),
                        report.getPartyShopId(),
                        fromTime,
                        toTime
                );
                processProvisionOfServiceTemplate(partyModel, shopAccountingModel, fromTime, toTime, zoneId, outputStream);
                break;
            case payment_registry:
                List<StatPayment> payments = statisticService.getPayments(
                        report.getPartyId(),
                        report.getPartyShopId(),
                        fromTime,
                        toTime,
                        InvoicePaymentStatus.captured(new InvoicePaymentCaptured())
                );
                processPaymentRegistryTemplate(payments, fromTime, toTime, outputStream);
                break;
            case registry_of_act:
                List<Payment> paymentList = statisticService.getPayments(
                        report.getPartyId(),
                        contractId,
                        fromTime,
                        toTime,
                        InvoicePaymentStatus.captured(new InvoicePaymentCaptured())
                ).stream().sorted(Comparator.comparing(p -> p.getStatus().getCaptured().getAt())).map(p -> {
                    Payment payment = new Payment();
                    payment.setId(p.getInvoiceId() + "." + p.getId());
                    payment.setCapturedAt(TimeUtil.toLocalizedDateTime(p.getStatus().getCaptured().getAt(), zoneId));
                    if (p.getPayer().isSetPaymentResource() && p.getPayer().getPaymentResource().getPaymentTool().isSetBankCard()) {
                        BankCard bankCard = p.getPayer().getPaymentResource().getPaymentTool().getBankCard();
                        payment.setCardNum(bankCard.getBin() + "****" + bankCard.getMaskedPan());
                    }
                    payment.setAmount(p.getAmount());
                    //TODO
                    payment.setPayoutAmount(p.getAmount() - p.getFee());
                    if (p.getPayer().isSetPaymentResource()) {
                        payment.setPayerEmail(p.getPayer().getPaymentResource().getEmail());
                    }
                    payment.setShopUrl(shopUrls.get(p.getShopId()));
                    payment.setPurpose("TODO");
                    return payment;
                }).collect(Collectors.toList());

                List<Refund> refundList = statisticService.getRefunds(
                        report.getPartyId(),
                        contractId,
                        fromTime,
                        toTime,
                        InvoicePaymentRefundStatus.succeeded(new InvoicePaymentRefundSucceeded())
                ).stream().sorted(Comparator.comparing(r -> r.getStatus().getSucceeded().getAt())).map(r -> {
                    Refund refund = new Refund();
                    StatPayment statPayment = statisticService.getPayment(r.getInvoiceId(), r.getPaymentId(), InvoicePaymentStatus.captured(new InvoicePaymentCaptured()));
                    refund.setId(r.getId());
                    refund.setPaymentId(r.getInvoiceId() + "." + r.getPaymentId());
                    refund.setPaymentCapturedAt(statPayment.getStatus().getCaptured().getAt());
                    refund.setSucceededAt(TimeUtil.toLocalizedDateTime(r.getStatus().getSucceeded().getAt(), zoneId));
                    if (statPayment.getPayer().isSetPaymentResource() && statPayment.getPayer().getPaymentResource().getPaymentTool().isSetBankCard()) {
                        BankCard bankCard = statPayment.getPayer().getPaymentResource().getPaymentTool().getBankCard();
                        refund.setCardNum(bankCard.getBin() + "****" + bankCard.getMaskedPan());
                    }
                    refund.setAmount(r.getAmount());
                    if (statPayment.getPayer().isSetPaymentResource()) {
                        refund.setPayerEmail(statPayment.getPayer().getPaymentResource().getEmail());
                    }
                    refund.setShopUrl(shopUrls.get(r.getShopId()));
                    refund.setPaymentPurpose("TODO");
                    return refund;
                }).collect(Collectors.toList());

                processRegistryOfActTemplate(paymentList, refundList, fromTime, toTime, zoneId, outputStream);
            default:
                throw new ReportNotFoundException(String.format("Unknown report type, reportType='%s'", reportType));
        }
    }

    public void processTemplate(Context context, ReportType reportType, OutputStream outputStream) throws IOException {
        processTemplate(context, reportType.getTemplateResource().getInputStream(), outputStream);
    }

    public void processTemplate(Context context, InputStream inputStream, OutputStream outputStream) throws IOException {
        JxlsHelper.getInstance()
                .processTemplate(
                        inputStream,
                        outputStream,
                        context
                );
    }

}
