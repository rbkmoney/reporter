package com.rbkmoney.reporter.service.impl;

import com.rbkmoney.damsel.domain.*;
import com.rbkmoney.damsel.domain_config.Reference;
import com.rbkmoney.damsel.domain_config.*;
import com.rbkmoney.reporter.exception.NotFoundException;
import com.rbkmoney.reporter.service.DomainConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.thrift.TException;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class DomainConfigServiceImpl implements DomainConfigService {

    private final RepositoryClientSrv.Iface dominantClient;

    @Override
    public BusinessSchedule getBusinessSchedule(BusinessScheduleRef scheduleRef) throws NotFoundException {
        return getBusinessSchedule(scheduleRef, Reference.head(new Head()));
    }

    @Override
    public BusinessSchedule getBusinessSchedule(BusinessScheduleRef scheduleRef, Reference revisionReference) throws NotFoundException {
        log.info("Trying to get schedule, scheduleRef='{}', revisionReference='{}'", scheduleRef, revisionReference);
        try {
            com.rbkmoney.damsel.domain.Reference reference = new com.rbkmoney.damsel.domain.Reference();
            reference.setBusinessSchedule(scheduleRef);
            VersionedObject versionedObject = dominantClient.checkoutObject(revisionReference, reference);
            BusinessSchedule schedule = versionedObject.getObject().getBusinessSchedule().getData();
            log.info("Schedule has been found, scheduleRef='{}', revisionReference='{}', schedule='{}'", scheduleRef, revisionReference, schedule);
            return schedule;
        } catch (VersionNotFound | ObjectNotFound ex) {
            throw new NotFoundException(String.format("%s, scheduleRef='%s', revisionReference='%s'", ex.getClass().getSimpleName(), scheduleRef, revisionReference), ex);
        } catch (TException ex) {
            throw new RuntimeException(String.format("Failed to get schedule, scheduleRef='%s', revisionReference='%s'", scheduleRef, revisionReference), ex);
        }
    }

    @Override
    public PaymentInstitution getPaymentInstitution(PaymentInstitutionRef paymentInstitutionRef) throws NotFoundException {
        return getPaymentInstitution(paymentInstitutionRef, Reference.head(new Head()));
    }

    @Override
    public PaymentInstitution getPaymentInstitution(PaymentInstitutionRef paymentInstitutionRef, Reference revisionReference) throws NotFoundException {
        log.info("Trying to get payment institution, paymentInstitutionRef='{}', revisionReference='{}'", paymentInstitutionRef, revisionReference);
        try {
            com.rbkmoney.damsel.domain.Reference reference = new com.rbkmoney.damsel.domain.Reference();
            reference.setPaymentInstitution(paymentInstitutionRef);
            VersionedObject versionedObject = dominantClient.checkoutObject(revisionReference, reference);
            PaymentInstitution paymentInstitution = versionedObject.getObject().getPaymentInstitution().getData();
            log.info("Payment institution has been found, PaymentInstitutionRef='{}', revisionReference='{}', paymentInstitution='{}'", paymentInstitutionRef, revisionReference, paymentInstitution);
            return paymentInstitution;
        } catch (VersionNotFound | ObjectNotFound ex) {
            throw new NotFoundException(String.format("%s, paymentInstitutionRef='%s', revisionReference='%s'", ex.getClass().getSimpleName(), paymentInstitutionRef, revisionReference), ex);
        } catch (TException ex) {
            throw new RuntimeException(String.format("Failed to get payment institution, paymentInstitutionRef='%s', revisionReference='%s'", paymentInstitutionRef, revisionReference), ex);
        }
    }

    @Override
    public Calendar getCalendar(CalendarRef calendarRef) throws NotFoundException {
        return getCalendar(calendarRef, Reference.head(new Head()));
    }

    @Override
    public Calendar getCalendar(CalendarRef calendarRef, Reference revisionReference) throws NotFoundException {
        log.info("Trying to get calendar, calendarRef='{}', revisionReference='{}'", calendarRef, revisionReference);
        try {
            com.rbkmoney.damsel.domain.Reference reference = new com.rbkmoney.damsel.domain.Reference();
            reference.setCalendar(calendarRef);
            VersionedObject versionedObject = dominantClient.checkoutObject(revisionReference, reference);
            Calendar calendar = versionedObject.getObject().getCalendar().getData();
            log.info("Calendar has been found, calendarRef='{}', revisionReference='{}', calendar='{}'", calendarRef, revisionReference, calendar);
            return calendar;
        } catch (VersionNotFound | ObjectNotFound ex) {
            throw new NotFoundException(String.format("%s not found, calendarRef='%s', revisionReference='%s'", ex.getClass().getSimpleName(), calendarRef, revisionReference), ex);
        } catch (TException ex) {
            throw new RuntimeException(String.format("Failed to get calendar, calendarRef='%s', revisionReference='%s'", calendarRef, revisionReference), ex);
        }
    }
}
