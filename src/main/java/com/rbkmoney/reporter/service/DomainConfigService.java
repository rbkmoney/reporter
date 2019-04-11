package com.rbkmoney.reporter.service;

import com.rbkmoney.damsel.domain.*;
import com.rbkmoney.damsel.domain_config.Reference;
import com.rbkmoney.reporter.exception.NotFoundException;

public interface DomainConfigService {

    BusinessSchedule getBusinessSchedule(BusinessScheduleRef scheduleRef) throws NotFoundException;

    BusinessSchedule getBusinessSchedule(BusinessScheduleRef scheduleRef, long domainRevision) throws NotFoundException;

    BusinessSchedule getBusinessSchedule(BusinessScheduleRef scheduleRef, Reference revisionReference) throws NotFoundException;

    CategoryType getCategoryType(CategoryRef categoryRef);

    CategoryType getCategoryType(CategoryRef categoryRef, long domainRevision);

    CategoryType getCategoryType(CategoryRef categoryRef, Reference revisionReference);

    PaymentInstitution getPaymentInstitution(PaymentInstitutionRef paymentInstitutionRef) throws NotFoundException;

    PaymentInstitution getPaymentInstitution(PaymentInstitutionRef paymentInstitutionRef, long domainRevision) throws NotFoundException;

    PaymentInstitution getPaymentInstitution(PaymentInstitutionRef paymentInstitutionRef, Reference revisionReference) throws NotFoundException;

    Calendar getCalendar(CalendarRef calendarRef) throws NotFoundException;

    Calendar getCalendar(CalendarRef calendarRef, long domainRevision) throws NotFoundException;

    Calendar getCalendar(CalendarRef calendarRef, Reference revisionReference) throws NotFoundException;
}
