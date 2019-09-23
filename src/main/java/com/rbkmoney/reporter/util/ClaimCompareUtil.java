package com.rbkmoney.reporter.util;

import com.rbkmoney.damsel.claim_management.InvalidChangeset;
import com.rbkmoney.damsel.domain.*;
import com.rbkmoney.reporter.domain.tables.pojos.ContractMeta;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ClaimCompareUtil {

    public static void comparePreferences(ServiceAcceptanceActPreferences actPreferences, ContractMeta contractMeta)
            throws InvalidChangeset {
        if (compareShedulersInfo(actPreferences.getSchedule(), contractMeta)) {
            throw new InvalidChangeset();
        }
        if (compareSignerInfo(actPreferences.getSigner(), contractMeta)) {
            throw new InvalidChangeset();
        }
    }

    private static boolean compareShedulersInfo(BusinessScheduleRef schedule, ContractMeta contractMeta) {
        return contractMeta.getScheduleId() != null && !contractMeta.getScheduleId().equals(schedule.getId()) ?
                true : false;
    }

    private static boolean compareSignerInfo(Representative signer, ContractMeta contractMeta) {
        return !signer.getFullName().equals(contractMeta.getRepresentativeFullName())
                || !signer.getPosition().equals(contractMeta.getRepresentativePosition())
                || hasRepresentativeDocumentDiff(signer.getDocument(), contractMeta) ?
                true : false;
    }

    private static boolean hasRepresentativeDocumentDiff(RepresentativeDocument document, ContractMeta contractMeta) {
        if (!document.getSetField().getFieldName().equals(contractMeta.getRepresentativeDocument())) {
            return true;
        }
        if (document.isSetArticlesOfAssociation()) {
            log.info("Document 'Articles Of Association' not implemented yet!");
            return true;
        } else if (document.isSetPowerOfAttorney()) {
            LegalAgreement powerOfAttorney = document.getPowerOfAttorney();
            if (!powerOfAttorney.getLegalAgreementId().equals(contractMeta.getLegalAgreementId())) {
                return true;
            }
            if (!powerOfAttorney.getSignedAt().equals(contractMeta.getLegalAgreementSignedAt().toString())) {
                return true;
            }
            if (hasValidUntilDiff(powerOfAttorney.getValidUntil(), contractMeta)) {
                return true;
            }
        } else {
            log.info("Document type not found");
            return true;
        }
        return false;
    }

    private static boolean hasValidUntilDiff(String validUntil, ContractMeta contractMeta) {
        if (validUntil == null) {
            if (contractMeta.getLegalAgreementValidUntil() != null) {
                return true;
            }
        } else {
            if (!validUntil.equals(contractMeta.getLegalAgreementValidUntil().toString())) {
                return true;
            }
        }
        return false;
    }

}
