package com.rbkmoney.reporter.handle.impl;

import com.rbkmoney.damsel.claim_management.*;
import com.rbkmoney.damsel.domain.ReportPreferences;
import com.rbkmoney.damsel.domain.ServiceAcceptanceActPreferences;
import com.rbkmoney.reporter.dao.ContractMetaDao;
import com.rbkmoney.reporter.domain.tables.pojos.ContractMeta;
import com.rbkmoney.reporter.handle.CommitHandler;
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

    @Override
    public void accept(String partyId, PartyModification partyModification) throws PartyNotFound, InvalidChangeset, TException {

        if (partyModification.isSetContractModification()) {
            ContractModificationUnit contractModificationUnit = partyModification.getContractModification();
            contractModificationAccept(partyId, contractModificationUnit);
        } else if (partyModification.isSetContractorModification()) {
            log.info("Accepting for contractor modification not implemented yet!");
        } else if (partyModification.isSetShopModification()) {
            log.info("Accepting for shop modification not implemented yet!");
        } else {
            log.info("Unknown party modification!");
        }
    }

    @Override
    public void commit(String partyId, PartyModification partyModification) throws TException {
        if (partyModification.isSetContractModification()) {
            ContractModificationUnit contractModificationUnit = partyModification.getContractModification();
            contractModificationCommit(partyId, contractModificationUnit);
        } else if (partyModification.isSetContractorModification()) {
            log.info("Commit for contractor modification not implemented yet!");
        } else if (partyModification.isSetShopModification()) {
            log.info("Commit for shop modification not implemented yet!");
        } else {
            log.info("Unknown party modification!");
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
                comparePreferences(serviceAcceptanceActPreferences, contractMeta);
            }
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
                contractMetaDao.enableContract(partyId, contractId, serviceAcceptanceActPreferences.getSchedule().getId());
            }
        }
    }

}
