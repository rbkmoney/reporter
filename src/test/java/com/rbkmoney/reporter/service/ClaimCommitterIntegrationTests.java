package com.rbkmoney.reporter.service;

import com.rbkmoney.damsel.claim_management.*;
import com.rbkmoney.damsel.domain.ReportPreferences;
import com.rbkmoney.damsel.domain.ServiceAcceptanceActPreferences;
import com.rbkmoney.reporter.dao.ContractMetaDao;
import org.apache.thrift.TException;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.ArrayList;
import java.util.List;

import static com.rbkmoney.reporter.service.data.ClaimCommitterTestData.*;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public final class ClaimCommitterIntegrationTests extends AbstractAppServiceTests {

    @Autowired
    private ClaimCommitterSrv.Iface claimCommitterService;

    @MockBean
    private ContractMetaDao contractMetaDao;

    @Test
    public void claimCommitterTest() throws TException {
        when(contractMetaDao.get(any(String.class), any(String.class))).thenReturn(getTestContractMeta());

        boolean isError = false;
        try {
            claimCommitterService.accept("PartyID-1", getTestActPreferencesClaim(getActPreferencesFullCorrect()));
        } catch (PartyNotFound | InvalidChangeset ex) {
            isError = true;
        }
        assertFalse("Error occurred during accepting", isError);

        try {
            claimCommitterService.accept("PartyID-1", getTestActPreferencesClaim(getActPreferencesSchedulerError()));
        } catch (PartyNotFound | InvalidChangeset ex) {
            isError = true;
        }
        assertTrue("Exception didn't threw during accepting", isError);
    }

    private static Claim getTestActPreferencesClaim(ServiceAcceptanceActPreferences actPreferences) {
        Claim claim = new Claim();
        claim.setId(1L);
        List<ModificationUnit> modificationUnitList = new ArrayList<>();
        ModificationUnit modificationUnit = new ModificationUnit();
        modificationUnit.setModificationId(1L);
        Modification modification = new Modification();

        PartyModification partyModification = new PartyModification();
        ContractModificationUnit contractModificationUnit = new ContractModificationUnit();
        contractModificationUnit.setId("ContractID-1");
        ContractModification contractModification = new ContractModification();
        ReportPreferences reportPreferences = new ReportPreferences();

        reportPreferences.setServiceAcceptanceActPreferences(actPreferences);

        contractModification.setReportPreferencesModification(reportPreferences);
        contractModificationUnit.setModification(contractModification);
        partyModification.setContractModification(contractModificationUnit);
        modification.setPartyModification(partyModification);
        modificationUnit.setModification(modification);
        modificationUnitList.add(modificationUnit);
        claim.setChangeset(modificationUnitList);
        return claim;
    }

}
