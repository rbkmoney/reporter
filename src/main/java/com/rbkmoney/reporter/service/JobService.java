package com.rbkmoney.reporter.service;

import com.rbkmoney.damsel.domain.BusinessScheduleRef;
import com.rbkmoney.damsel.domain.CalendarRef;
import com.rbkmoney.reporter.domain.tables.pojos.ContractMeta;
import org.quartz.SchedulerException;

public interface JobService {

    void createJob(String partyId, String contractId, CalendarRef calendarRef, BusinessScheduleRef scheduleRef) throws SchedulerException;

    void createJob(ContractMeta contractMeta) throws SchedulerException;

    void removeJob(ContractMeta contractMeta) throws SchedulerException;
}
