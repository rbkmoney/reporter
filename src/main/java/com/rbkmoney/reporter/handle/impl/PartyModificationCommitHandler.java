package com.rbkmoney.reporter.handle.impl;

import com.rbkmoney.damsel.claim_management.*;
import com.rbkmoney.damsel.domain.*;
import com.rbkmoney.reporter.dao.ContractMetaDao;
import com.rbkmoney.reporter.domain.tables.pojos.ContractMeta;
import com.rbkmoney.reporter.exception.NotFoundException;
import com.rbkmoney.reporter.handle.CommitHandler;
import com.rbkmoney.reporter.service.DomainConfigService;
import com.rbkmoney.reporter.service.PartyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.thrift.TException;
import org.springframework.stereotype.Component;

import static com.rbkmoney.reporter.util.ClaimCompareUtil.comparePreferences;

@Slf4j
@Component
@RequiredArgsConstructor
public class PartyModificationCommitHandler implements CommitHandler<PartyModification> {

    private final ContractMetaDao contractMetaDao;

    private final PartyService partyService;

    private final DomainConfigService domainConfigService;

    @Override
    public void accept(String partyId, PartyModification partyModification) throws PartyNotFound, InvalidChangeset, TException {

        if (partyModification.isSetContractModification()) {
            ContractModificationUnit contractModificationUnit = partyModification.getContractModification();
            contractModificationAccept(partyId, contractModificationUnit);
        } else {
            log.info("Received unknown party modification '{}' at the accept stage", partyModification.getSetField().getFieldName());
        }
    }

    @Override
    public void commit(String partyId, PartyModification partyModification) throws TException {
        if (partyModification.isSetContractModification()) {
            ContractModificationUnit contractModificationUnit = partyModification.getContractModification();
            contractModificationCommit(partyId, contractModificationUnit);
        } else {
            log.info("Received unknown party modification '{}' at the accept stage", partyModification.getSetField().getFieldName());
        }
    }

    private void contractModificationAccept(String partyId, ContractModificationUnit contractModificationUnit)
            throws PartyNotFound, InvalidChangeset {
        String contractId = contractModificationUnit.getId();
        ContractModification contractModification = contractModificationUnit.getModification();
        if (contractModification.isSetReportPreferencesModification()) {
            ReportPreferences reportPreferences = contractModification.getReportPreferencesModification();

            if (reportPreferences.isSetServiceAcceptanceActPreferences()) {
                ServiceAcceptanceActPreferences serviceAcceptanceActPreferences = reportPreferences.getServiceAcceptanceActPreferences();
                if (serviceAcceptanceActPreferences == null) {
                    throw new InvalidChangeset();
                }
                ContractMeta contractMeta = contractMetaDao.get(partyId, contractId);
                if (contractMeta == null) {
                    throw new PartyNotFound();
                }
                checkSchedulerExistence(serviceAcceptanceActPreferences.getSchedule());
                comparePreferences(serviceAcceptanceActPreferences, contractMeta);
            }
        } else {
            log.info("Received unknown contract modification '{}' at the accept stage", contractModification.getSetField().getFieldName());
        }
    }

    private void checkSchedulerExistence(BusinessScheduleRef schedule) throws InvalidChangeset {
        if (schedule == null) {
            throw new InvalidChangeset();
        }
        try {
            domainConfigService.getBusinessSchedule(schedule);
        } catch (NotFoundException ex) {
            throw new InvalidChangeset();
        }
    }

    private void contractModificationCommit(String partyId, ContractModificationUnit contractModificationUnit)
            throws PartyNotFound, InvalidChangeset {
        String contractId = contractModificationUnit.getId();
        ContractModification contractModification = contractModificationUnit.getModification();
        if (contractModification.isSetReportPreferencesModification()) {
            ReportPreferences reportPreferences = contractModification.getReportPreferencesModification();

            if (reportPreferences.isSetServiceAcceptanceActPreferences()) {
                ServiceAcceptanceActPreferences serviceAcceptanceActPreferences = reportPreferences.getServiceAcceptanceActPreferences();
                if (serviceAcceptanceActPreferences == null) {
                    throw new InvalidChangeset();
                }
                ContractMeta contractMeta = contractMetaDao.get(partyId, contractId);
                if (contractMeta == null) {
                    throw new PartyNotFound();
                }
                PaymentInstitutionRef paymentInstitutionRef = partyService.getPaymentInstitutionRef(partyId, contractId);
                PaymentInstitution paymentInstitution = domainConfigService.getPaymentInstitution(paymentInstitutionRef);
                if (!paymentInstitution.isSetCalendar()) {
                    throw new NotFoundException(
                            String.format("Calendar not found, partyId='%s', contractId='%s'", partyId, contractId)
                    );
                }
                BusinessScheduleRef schedule = serviceAcceptanceActPreferences.getSchedule();
                CalendarRef calendarRef = paymentInstitution.getCalendar();
                contractMetaDao.enableContract(partyId, contractId, schedule.getId(), calendarRef.getId());
            }
        } else {
            log.info("Received unknown contract modification '{}' at the commit stage", contractModification.getSetField().getFieldName());
        }
    }

}
