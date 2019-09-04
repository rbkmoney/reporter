package com.rbkmoney.reporter.service;

import com.rbkmoney.damsel.claim_management.InvalidChangeset;
import com.rbkmoney.damsel.domain.*;
import com.rbkmoney.reporter.domain.tables.pojos.ContractMeta;
import org.junit.Test;

import java.time.LocalDateTime;

import static com.rbkmoney.reporter.util.ClaimCompareUtil.comparePreferences;
import static org.junit.Assert.assertTrue;

public final class ClaimCommitterUnitTests {

    @Test
    public void comparePreferencesTest() {
        try {
            comparePreferences(getActPreferencesFullCorrect(), getTestContractMeta());
        } catch (InvalidChangeset invalidChangeset) {
            assertTrue("Correct compare preferences test throw exception", false);
        }

        boolean isSignerError = false;
        try {
            comparePreferences(getActPreferencesErrorSigner(), getTestContractMeta());
        } catch (InvalidChangeset invalidChangeset) {
            isSignerError = true;
        }
        assertTrue("Compare preferences with signer data error didn't throw exception", isSignerError);

        boolean isSchedulerError = false;
        try {
            comparePreferences(getActPreferencesSchedulerError(), getTestContractMeta());
        } catch (InvalidChangeset invalidChangeset) {
            isSchedulerError = true;
        }
        assertTrue("Compare preferences with scheduler data error didn't throw exception", isSchedulerError);
    }

    private static ServiceAcceptanceActPreferences getActPreferencesFullCorrect() {
        ServiceAcceptanceActPreferences actPreferences = new ServiceAcceptanceActPreferences();

        actPreferences.setSchedule(new BusinessScheduleRef().setId(123));
        RepresentativeDocument representativeDocument = new RepresentativeDocument();
        representativeDocument.setPowerOfAttorney(new LegalAgreement()
                .setLegalAgreementId("AgreementID-1")
                .setSignedAt("2019-09-04T19:01:02.407796")
                .setValidUntil("2021-09-04T19:01:02.407796"));

        actPreferences.setSigner(new Representative()
                .setFullName("Some full name")
                .setPosition("Some position")
                .setDocument(representativeDocument));
        return actPreferences;
    }

    private static ServiceAcceptanceActPreferences getActPreferencesSchedulerError() {
        ServiceAcceptanceActPreferences actPreferences = new ServiceAcceptanceActPreferences();

        actPreferences.setSchedule(new BusinessScheduleRef().setId(1234));
        RepresentativeDocument representativeDocument = new RepresentativeDocument();
        representativeDocument.setPowerOfAttorney(new LegalAgreement()
                .setLegalAgreementId("AgreementID-1")
                .setSignedAt("2019-09-04T19:01:02.407796")
                .setValidUntil("2021-09-04T19:01:02.407796"));

        actPreferences.setSigner(new Representative()
                .setFullName("Some full name")
                .setPosition("Some position")
                .setDocument(representativeDocument));
        return actPreferences;
    }

    private static ServiceAcceptanceActPreferences getActPreferencesErrorSigner() {
        ServiceAcceptanceActPreferences actPreferences = new ServiceAcceptanceActPreferences();

        actPreferences.setSchedule(new BusinessScheduleRef().setId(123));
        RepresentativeDocument representativeDocument = new RepresentativeDocument();
        representativeDocument.setPowerOfAttorney(new LegalAgreement()
                .setLegalAgreementId("SomeID-123")
                .setSignedAt("2019-09-04T19:01:02.407796")
                .setValidUntil("2021-09-04T19:01:02.407796"));

        actPreferences.setSigner(new Representative()
                .setFullName("Some full name 1")
                .setPosition("Some position 1")
                .setDocument(representativeDocument));
        return actPreferences;
    }

    private static ContractMeta getTestContractMeta() {
        ContractMeta contractMeta = new ContractMeta();
        contractMeta.setPartyId("PartyID-1");
        contractMeta.setContractId("ContractID-1");
        contractMeta.setCalendarId(1);
        contractMeta.setLastClosingBalance(1L);
        contractMeta.setLastEventId(1L);
        contractMeta.setLastReportCreatedAt(LocalDateTime.parse("2019-09-04T19:01:02.407796"));
        contractMeta.setLegalAgreementId("AgreementID-1");
        contractMeta.setLegalAgreementSignedAt(LocalDateTime.parse("2019-09-04T19:01:02.407796"));
        contractMeta.setLegalAgreementValidUntil(LocalDateTime.parse("2021-09-04T19:01:02.407796"));
        contractMeta.setScheduleId(123);
        contractMeta.setWtime(LocalDateTime.now());
        contractMeta.setRepresentativeDocument("power_of_attorney");
        contractMeta.setRepresentativeFullName("Some full name");
        contractMeta.setRepresentativePosition("Some position");
        return contractMeta;
    }

}
