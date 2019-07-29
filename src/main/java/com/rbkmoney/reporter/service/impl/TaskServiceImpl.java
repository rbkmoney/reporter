package com.rbkmoney.reporter.service.impl;

import com.rbkmoney.damsel.domain.*;
import com.rbkmoney.geck.common.util.TypeUtil;
import com.rbkmoney.reporter.dao.ContractMetaDao;
import com.rbkmoney.reporter.domain.tables.pojos.ContractMeta;
import com.rbkmoney.reporter.exception.DaoException;
import com.rbkmoney.reporter.exception.NotFoundException;
import com.rbkmoney.reporter.exception.ScheduleProcessingException;
import com.rbkmoney.reporter.exception.StorageException;
import com.rbkmoney.reporter.service.DomainConfigService;
import com.rbkmoney.reporter.service.JobService;
import com.rbkmoney.reporter.service.PartyService;
import com.rbkmoney.reporter.service.TaskService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.SchedulerException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class TaskServiceImpl implements TaskService {

    private final ContractMetaDao contractMetaDao;

    private final PartyService partyService;
    private final DomainConfigService domainConfigService;
    private final JobService jobService;

    @Override
    @Transactional
    public void registerProvisionOfServiceJob(String partyId, String contractId, long lastEventId, BusinessScheduleRef scheduleRef, Representative signer) throws ScheduleProcessingException, NotFoundException, StorageException {
        log.info("Trying to register provision of service job, partyId='{}', contractId='{}', scheduleId='{}', signer='{}'",
                partyId, contractId, scheduleRef, signer);
        PaymentInstitutionRef paymentInstitutionRef = partyService.getPaymentInstitutionRef(partyId, contractId);
        PaymentInstitution paymentInstitution = domainConfigService.getPaymentInstitution(paymentInstitutionRef);

        if (!paymentInstitution.isSetCalendar()) {
            throw new NotFoundException(String.format("Calendar not found, partyId='%s', contractId='%s'", partyId, contractId));
        }
        CalendarRef calendarRef = paymentInstitution.getCalendar();

        try {
            log.info("Start ContractMeta handling, partyId='{}', contractId='{}'", partyId, contractId);

            ContractMeta contractMeta = new ContractMeta();
            contractMeta.setPartyId(partyId);
            contractMeta.setContractId(contractId);
            contractMeta.setCalendarId(calendarRef.getId());
            contractMeta.setLastEventId(lastEventId);
            contractMeta.setScheduleId(scheduleRef.getId());

            contractMeta.setRepresentativeFullName(signer.getFullName());
            contractMeta.setRepresentativePosition(signer.getPosition());
            contractMeta.setRepresentativeDocument(signer.getDocument().getSetField().getFieldName());
            if (signer.getDocument().isSetPowerOfAttorney()) {
                LegalAgreement legalAgreement = signer.getDocument().getPowerOfAttorney();
                contractMeta.setLegalAgreementId(legalAgreement.getLegalAgreementId());
                contractMeta.setLegalAgreementSignedAt(TypeUtil.stringToLocalDateTime(legalAgreement.getSignedAt()));
                contractMeta.setLegalAgreementValidUntil(TypeUtil.stringToLocalDateTime(legalAgreement.getValidUntil()));
            }

            contractMetaDao.save(contractMeta);

            log.info("ContractMeta has been saved,, partyId='{}', contractId='{}'", partyId, contractId);

            jobService.createJob(partyId, contractId, calendarRef, scheduleRef);

            log.info(
                    "Job have been successfully enabled, partyId='{}', contractId='{}', scheduleRef='{}', calendarRef='{}'",
                    partyId, contractId, scheduleRef, calendarRef
            );
        } catch (DaoException ex) {
            throw new StorageException(
                    String.format(
                            "Failed to save job on storage, partyId='%s', contractId='%s', scheduleRef='%s', calendarRef='%s'",
                            partyId, contractId, scheduleRef, calendarRef
                    ),
                    ex
            );
        } catch (NotFoundException | SchedulerException ex) {
            throw new ScheduleProcessingException(
                    String.format(
                            "Failed to create job, partyId='%s', contractId='%s', calendarRef='%s', scheduleRef='%s'",
                            partyId, contractId, calendarRef, scheduleRef
                    ),
                    ex
            );
        }
    }

    @Override
    @Transactional
    public void deregisterProvisionOfServiceJob(String partyId, String contractId) throws ScheduleProcessingException, StorageException {
        log.info("Trying to deregister provision of service job, partyId='{}', contractId='{}'", partyId, contractId);
        try {
            ContractMeta contractMeta = contractMetaDao.get(partyId, contractId);
            if (contractMeta != null) {
                contractMetaDao.disableContract(partyId, contractId);
                jobService.removeJob(contractMeta);
                log.info(
                        "Provision of service job have been successfully disabled, partyId='{}', contractId='{}', scheduleId='{}', calendarId='{}'",
                        partyId, contractId, contractMeta.getScheduleId(), contractMeta.getCalendarId()
                );
            } else {
                log.warn("Not possible to disable provision of service job, contract meta not found, partyId='{}', contractId='{}'", partyId, contractId);
            }
        } catch (DaoException ex) {
            throw new StorageException(String.format("Failed to disable provision of service job on storage, partyId='%s', contractId='%s'", partyId, contractId), ex);
        } catch (SchedulerException ex) {
            throw new ScheduleProcessingException(String.format("Failed to remove job, partyId='%s', contractId='%s'", partyId, contractId), ex);
        }
    }
}
