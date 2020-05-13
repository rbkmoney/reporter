package com.rbkmoney.reporter.service.impl;

import com.rbkmoney.damsel.base.TimeSpan;
import com.rbkmoney.damsel.domain.BusinessSchedule;
import com.rbkmoney.damsel.domain.BusinessScheduleRef;
import com.rbkmoney.damsel.domain.Calendar;
import com.rbkmoney.damsel.domain.CalendarRef;
import com.rbkmoney.reporter.domain.enums.ReportType;
import com.rbkmoney.reporter.domain.tables.pojos.ContractMeta;
import com.rbkmoney.reporter.job.GenerateReportJob;
import com.rbkmoney.reporter.service.DomainConfigService;
import com.rbkmoney.reporter.service.JobService;
import com.rbkmoney.reporter.trigger.FreezeTimeCronScheduleBuilder;
import com.rbkmoney.reporter.util.SchedulerUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.quartz.impl.calendar.HolidayCalendar;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class JobServiceImpl implements JobService {

    private final DomainConfigService domainConfigService;

    private final Scheduler scheduler;

    @Override
    public void createJob(String partyId, String contractId, CalendarRef calendarRef, BusinessScheduleRef scheduleRef) throws SchedulerException {
        createCommonJob(partyId, contractId, calendarRef, scheduleRef);
    }

    @Override
    public void createJob(ContractMeta contractMeta) throws SchedulerException {
        JobKey jobKey = buildJobKey(contractMeta.getPartyId(), contractMeta.getContractId(), contractMeta.getCalendarId(), contractMeta.getScheduleId());
        List<? extends Trigger> triggers = scheduler.getTriggersOfJob(jobKey);
        if (triggers.isEmpty() || !isTriggerOnNormalState(triggers)) {
            if (scheduler.checkExists(jobKey)) {
                log.warn("Inactive job found, please check it manually. Job will be restored, contractMeta='{}'", contractMeta);
            }
            createCommonJob(
                    contractMeta.getPartyId(),
                    contractMeta.getContractId(),
                    new CalendarRef(contractMeta.getCalendarId()),
                    new BusinessScheduleRef(contractMeta.getScheduleId())
            );
        }
    }

    @Override
    public void removeJob(ContractMeta contractMeta) throws SchedulerException {
        if (contractMeta.getCalendarId() != null && contractMeta.getScheduleId() != null) {
            JobKey jobKey = buildJobKey(
                    contractMeta.getPartyId(),
                    contractMeta.getContractId(),
                    contractMeta.getCalendarId(),
                    contractMeta.getScheduleId()
            );
            List<TriggerKey> triggerKeys = scheduler.getTriggersOfJob(jobKey).stream()
                    .map(Trigger::getKey)
                    .collect(Collectors.toList());

            scheduler.unscheduleJobs(triggerKeys);
            scheduler.deleteJob(jobKey);
        }
    }

    private boolean isTriggerOnNormalState(List<? extends Trigger> triggers) throws SchedulerException {
        for (Trigger trigger : triggers) {
            if (!isTriggerOnNormalState(trigger)) {
                return false;
            }
        }
        return true;
    }

    private boolean isTriggerOnNormalState(Trigger trigger) throws SchedulerException {
        Trigger.TriggerState triggerState = scheduler.getTriggerState(trigger.getKey());
        log.debug("Trigger '{}' on '{}' state", trigger, triggerState);
        return triggerState == Trigger.TriggerState.NORMAL;
    }

    private void createCommonJob(String partyId, String contractId, CalendarRef calendarRef, BusinessScheduleRef scheduleRef) throws SchedulerException {
        log.info("Trying to create job, partyId='{}', contractId='{}', calendarRef='{}', scheduleRef='{}'", partyId, contractId, calendarRef, scheduleRef);

        BusinessSchedule schedule = domainConfigService.getBusinessSchedule(scheduleRef);
        Calendar calendar = domainConfigService.getCalendar(calendarRef);

        String calendarId = "calendar-" + calendarRef.getId();
        HolidayCalendar holidayCalendar = SchedulerUtil.buildCalendar(calendar);
        scheduler.addCalendar(calendarId, holidayCalendar, true, true);
        log.info("New calendar was saved, calendarRef='{}', calendarId='{}'", calendarRef, calendarId);

        JobDetail jobDetail = JobBuilder.newJob(GenerateReportJob.class)
                .withIdentity(buildJobKey(partyId, contractId, calendarRef.getId(), scheduleRef.getId()))
                .withDescription(schedule.getDescription())
                .usingJobData(GenerateReportJob.PARTY_ID, partyId)
                .usingJobData(GenerateReportJob.CONTRACT_ID, contractId)
                .usingJobData(GenerateReportJob.REPORT_TYPE, ReportType.provision_of_service.toString())
                .build();

        Set<Trigger> triggers = new HashSet<>();
        List<String> cronList = SchedulerUtil.buildCron(schedule.getSchedule());
        for (int triggerId = 0; triggerId < cronList.size(); triggerId++) {
            String cron = cronList.get(triggerId);

            FreezeTimeCronScheduleBuilder freezeTimeCronScheduleBuilder = FreezeTimeCronScheduleBuilder
                    .cronSchedule(cron)
                    .inTimeZone(TimeZone.getTimeZone(calendar.getTimezone()));
            if (schedule.isSetDelay()) {
                TimeSpan timeSpan = schedule.getDelay();
                freezeTimeCronScheduleBuilder.withYears(timeSpan.getYears())
                        .withMonths(timeSpan.getMonths())
                        .withDays(timeSpan.getDays())
                        .withHours(timeSpan.getHours())
                        .withMinutes(timeSpan.getMinutes())
                        .withSeconds(timeSpan.getSeconds());
            }

            Trigger trigger = TriggerBuilder.newTrigger()
                    .withIdentity(buildTriggerKey(partyId, contractId, calendarRef.getId(), scheduleRef.getId(), triggerId))
                    .withDescription(schedule.getDescription())
                    .forJob(jobDetail)
                    .withSchedule(freezeTimeCronScheduleBuilder)
                    .modifiedByCalendar(calendarId)
                    .build();
            triggers.add(trigger);
        }
        scheduler.scheduleJob(jobDetail, triggers, true);
        log.info("Jobs have been successfully created or updated, partyId='{}', contractId='{}', calendarRef='{}', scheduleRef='{}', jobDetail='{}', triggers='{}'", partyId, contractId, calendarRef, scheduleRef, jobDetail, triggers);
    }

    private JobKey buildJobKey(String partyId, String contractId, int calendarId, int scheduleId) {
        return JobKey.jobKey(
                String.format("job-%s-%s", partyId, contractId),
                buildGroupKey(calendarId, scheduleId)
        );
    }

    private TriggerKey buildTriggerKey(String partyId, String contractId, int calendarId, int scheduleId, int triggerId) {
        return TriggerKey.triggerKey(
                String.format("trigger-%s-%s-%d", partyId, contractId, triggerId),
                buildGroupKey(calendarId, scheduleId)
        );
    }

    private String buildGroupKey(int calendarId, int scheduleId) {
        return String.format("group-%d-%d", calendarId, scheduleId);
    }
}
