package com.rbkmoney.reporter.service;

import com.rbkmoney.damsel.claim_management.InvalidChangeset;
import org.junit.Test;

import static com.rbkmoney.reporter.service.data.ClaimCommitterTestData.*;
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

}
