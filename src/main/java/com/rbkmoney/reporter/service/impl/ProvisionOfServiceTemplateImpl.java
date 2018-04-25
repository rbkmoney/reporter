package com.rbkmoney.reporter.service.impl;

import com.rbkmoney.damsel.domain.Contract;
import com.rbkmoney.damsel.domain.LegalAgreement;
import com.rbkmoney.damsel.domain.RussianLegalEntity;
import com.rbkmoney.reporter.dao.ContractMetaDao;
import com.rbkmoney.reporter.domain.enums.ReportType;
import com.rbkmoney.reporter.domain.tables.pojos.ContractMeta;
import com.rbkmoney.reporter.domain.tables.pojos.Report;
import com.rbkmoney.reporter.exception.DaoException;
import com.rbkmoney.reporter.exception.StorageException;
import com.rbkmoney.reporter.model.ShopAccountingModel;
import com.rbkmoney.reporter.service.PartyService;
import com.rbkmoney.reporter.service.StatisticService;
import com.rbkmoney.reporter.service.TemplateService;
import com.rbkmoney.reporter.util.FormatUtil;
import com.rbkmoney.reporter.util.TimeUtil;
import org.jxls.common.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.OutputStream;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;

@Component
public class ProvisionOfServiceTemplateImpl implements TemplateService {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    public static final String DEFAULT_REPORT_CURRENCY_CODE = "RUB";

    private final StatisticService statisticService;

    private final PartyService partyService;

    private final ContractMetaDao contractMetaDao;

    private final Resource resource;

    @Autowired
    public ProvisionOfServiceTemplateImpl(
            StatisticService statisticService,
            PartyService partyService,
            ContractMetaDao contractMetaDao,
            @Value("${report.type.pos.path|classpath:templates/provision_of_service_act.xlsx}") ClassPathResource resource
    ) {
        this.statisticService = statisticService;
        this.partyService = partyService;
        this.contractMetaDao = contractMetaDao;
        this.resource = resource;
    }

    @Override
    public boolean accept(ReportType reportType, ContractMeta contractMeta) {
        return reportType == ReportType.provision_of_service;
    }

    @Override
    public void processReportTemplate(Report report, ContractMeta contractMeta, OutputStream outputStream) throws IOException {

        Instant createdAt = report.getCreatedAt().toInstant(ZoneOffset.UTC);

        Context context = new Context();
        ZoneId reportZoneId = ZoneId.of(report.getTimezone());
        context.putVar("party_id", report.getPartyId());
        context.putVar("contract_id", report.getPartyContractId());
        context.putVar("created_at", TimeUtil.toLocalizedDate(report.getCreatedAt().toInstant(ZoneOffset.UTC), reportZoneId));
        context.putVar("from_time", TimeUtil.toLocalizedDate(report.getFromTime().toInstant(ZoneOffset.UTC), reportZoneId));
        context.putVar("to_time", TimeUtil.toLocalizedDate(report.getToTime().minusNanos(1).toInstant(ZoneOffset.UTC), reportZoneId));

        Contract contract = partyService.getContract(report.getPartyId(), report.getPartyContractId(), createdAt);
        if (contract.isSetLegalAgreement()) {
            LegalAgreement legalAgreement = contract.getLegalAgreement();
            context.putVar("legal_agreement_id", legalAgreement.getLegalAgreementId());
            context.putVar("legal_agreement_signed_at", TimeUtil.toLocalizedDate(legalAgreement.getSignedAt(), reportZoneId));
        }

        if (contract.isSetContractor()
                && contract.getContractor().isSetLegalEntity()
                && contract.getContractor().getLegalEntity().isSetRussianLegalEntity()) {
            RussianLegalEntity entity = contract.getContractor()
                    .getLegalEntity()
                    .getRussianLegalEntity();
            context.putVar("registered_name", entity.getRegisteredName());
            context.putVar("representative_full_name", entity.getRepresentativeFullName());
            context.putVar("representative_position", entity.getRepresentativePosition());
        }

        ShopAccountingModel shopAccountingModel = statisticService.getShopAccounting(
                report.getPartyId(),
                report.getPartyContractId(),
                DEFAULT_REPORT_CURRENCY_CODE,
                report.getFromTime().toInstant(ZoneOffset.UTC),
                report.getToTime().toInstant(ZoneOffset.UTC)
        );
        context.putVar("funds_acquired", FormatUtil.formatCurrency(shopAccountingModel.getFundsAcquired()));
        context.putVar("fee_charged", FormatUtil.formatCurrency(shopAccountingModel.getFeeCharged()));
        context.putVar("funds_paid_out", FormatUtil.formatCurrency(shopAccountingModel.getFundsPaidOut()));
        context.putVar("funds_refunded", FormatUtil.formatCurrency(shopAccountingModel.getFundsRefunded()));

        long openingBalance;
        long closingBalance;
        if (contractMeta.getLastClosingBalance() == null) {
            ShopAccountingModel previousPeriod = statisticService.getShopAccounting(
                    report.getPartyId(),
                    report.getPartyContractId(),
                    DEFAULT_REPORT_CURRENCY_CODE,
                    report.getFromTime().toInstant(ZoneOffset.UTC)
            );
            openingBalance = previousPeriod.getAvailableFunds();
            closingBalance = openingBalance + shopAccountingModel.getAvailableFunds();
        } else {
            openingBalance = contractMeta.getLastClosingBalance();
            closingBalance = openingBalance + shopAccountingModel.getAvailableFunds();
            try {
                contractMetaDao.saveLastClosingBalance(report.getPartyId(), report.getPartyContractId(), report.getType(), closingBalance);
            } catch (DaoException ex) {
                throw new StorageException(String.format("Failed to save contract meta, partyId='%s', contractId='%s'", report.getPartyId(), report.getPartyContractId()), ex);
            }
        }
        context.putVar("opening_balance", FormatUtil.formatCurrency(openingBalance));
        context.putVar("closing_balance", FormatUtil.formatCurrency(closingBalance));

        processTemplate(context, resource.getInputStream(), outputStream);
    }

}
