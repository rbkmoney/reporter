package com.rbkmoney.reporter.service.impl;

import com.rbkmoney.damsel.domain.*;
import com.rbkmoney.reporter.dao.ContractMetaDao;
import com.rbkmoney.reporter.domain.enums.ReportType;
import com.rbkmoney.reporter.domain.tables.pojos.ContractMeta;
import com.rbkmoney.reporter.domain.tables.pojos.Report;
import com.rbkmoney.reporter.exception.DaoException;
import com.rbkmoney.reporter.exception.NotFoundException;
import com.rbkmoney.reporter.exception.StorageException;
import com.rbkmoney.reporter.service.PartyService;
import com.rbkmoney.reporter.service.ReportingService;
import com.rbkmoney.reporter.service.TemplateService;
import com.rbkmoney.reporter.util.FormatUtil;
import com.rbkmoney.reporter.util.TimeUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jxls.common.Context;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.OutputStream;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Map;

@Component
@Qualifier("provisionOfServiceTemplate")
@Slf4j
@RequiredArgsConstructor
public class ProvisionOfServiceTemplateImpl implements TemplateService {

    private static final String DEFAULT_REPORT_CURRENCY_CODE = "RUB";

    private final PartyService partyService;
    private final ReportingService reportingService;

    private final ContractMetaDao contractMetaDao;

    @Value("${report.type.pos.path|classpath:templates/provision_of_service_act.xlsx}")
    private ClassPathResource resource;

    @Override
    public boolean accept(ReportType reportType) {
        return reportType == ReportType.provision_of_service;
    }

    @Override
    public void processReportTemplate(Report report, OutputStream outputStream) throws IOException {
        try {
            String fundsAcquired = "funds_acquired";
            String feeCharged = "fee_charged";
            String fundsPaidOut = "funds_paid_out";
            String fundsRefunded = "funds_refunded";
            String fundsAdjusted = "funds_adjusted";

            Party party = partyService.getParty(report.getPartyId(), report.getCreatedAt().toInstant(ZoneOffset.UTC));
            Shop shop = party.getShops().get(report.getPartyShopId());
            if (shop == null) {
                throw new NotFoundException(String.format("Failed to find shop for provision of service report, partyId='%s', shopId='%s'",
                        report.getPartyId(), report.getPartyShopId()));
            }

            String contractId = shop.getContractId();
            ContractMeta contractMeta = contractMetaDao.getExclusive(report.getPartyId(), contractId);
            if (contractMeta == null) {
                throw new NotFoundException(String.format("Failed to find meta data for provision of service report, partyId='%s', contractId='%s'",
                        report.getPartyId(), contractId));
            }
            contractMetaDao.updateLastReportCreatedAt(report.getPartyId(), contractId, report.getCreatedAt());

            Context context = new Context();
            ZoneId reportZoneId = ZoneId.of(report.getTimezone());
            context.putVar("party_id", report.getPartyId());
            context.putVar("shop_id", report.getPartyShopId());
            context.putVar("contract_id", contractMeta.getContractId());
            context.putVar("created_at", TimeUtil.toLocalizedDate(report.getCreatedAt().toInstant(ZoneOffset.UTC), reportZoneId));
            context.putVar("from_time", TimeUtil.toLocalizedDate(report.getFromTime().toInstant(ZoneOffset.UTC), reportZoneId));
            context.putVar("to_time", TimeUtil.toLocalizedDate(report.getToTime().minusNanos(1).toInstant(ZoneOffset.UTC), reportZoneId));

            Contract contract = party.getContracts().get(contractId);
            if (contract == null) {
                throw new NotFoundException(String.format("Failed to find contract for provision of service report, partyId='%s', contractId='%s'",
                        report.getPartyId(), contractId));
            }

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
            }

            context.putVar("representative_full_name", contractMeta.getRepresentativeFullName());
            context.putVar("representative_position", contractMeta.getRepresentativePosition());

            Map<String, Long> accountingData = reportingService.getShopAccountingReportData(
                    report.getPartyId(),
                    report.getPartyShopId(),
                    DEFAULT_REPORT_CURRENCY_CODE,
                    report.getFromTime(),
                    report.getToTime()
            );

            context.putVar(fundsAcquired, FormatUtil.formatCurrency(accountingData.get(fundsAcquired)));
            context.putVar(feeCharged, FormatUtil.formatCurrency(accountingData.get(feeCharged)));
            context.putVar(fundsPaidOut, FormatUtil.formatCurrency(accountingData.get(fundsPaidOut)));
            context.putVar(fundsRefunded, FormatUtil.formatCurrency(accountingData.get(fundsRefunded)));

            long openingBalance;
            if (contractMeta.getLastClosingBalance() == null) {
                Map<String, Long> prevAccountingData = reportingService.getShopAccountingReportData(
                        report.getPartyId(),
                        report.getPartyShopId(),
                        DEFAULT_REPORT_CURRENCY_CODE,
                        report.getFromTime()
                );
                openingBalance = getAvailableFunds(fundsAcquired, feeCharged, fundsPaidOut, fundsRefunded, fundsAdjusted, prevAccountingData);
            } else {
                openingBalance = contractMeta.getLastClosingBalance();
            }

            long closingBalance = openingBalance + getAvailableFunds(fundsAcquired, feeCharged, fundsPaidOut, fundsRefunded, fundsAdjusted, accountingData);
//            contractMetaDao.saveLastClosingBalance(report.getPartyId(), contractId, closingBalance);
            context.putVar("opening_balance", FormatUtil.formatCurrency(openingBalance));
            context.putVar("closing_balance", FormatUtil.formatCurrency(closingBalance));

            processTemplate(context, resource.getInputStream(), outputStream);
        } catch (DaoException ex) {
            throw new StorageException(ex);
        }
    }

    private long getAvailableFunds(String fundsAcquired, String feeCharged, String fundsPaidOut, String fundsRefunded, String fundsAdjusted, Map<String, Long> accountingData) {
        return accountingData.get(fundsAcquired) + accountingData.get(fundsAdjusted)
                - accountingData.get(feeCharged) - accountingData.get(fundsPaidOut) - accountingData.get(fundsRefunded);
    }
}
