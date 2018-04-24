package com.rbkmoney.reporter.job;

import com.rbkmoney.reporter.exception.StorageException;
import com.rbkmoney.reporter.trigger.FreezeTimeCronTrigger;
import com.rbkmoney.woody.api.flow.error.WRuntimeException;
import org.quartz.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

import static com.rbkmoney.geck.common.util.TypeUtil.toLocalDateTime;

@Component
public class GenerateReportJob implements Job {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    public static final String PARTY_ID = "party_id";

    public static final String CONTRACT_ID = "contract_id";

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        JobDetail jobDetail = jobExecutionContext.getJobDetail();
        FreezeTimeCronTrigger trigger = (FreezeTimeCronTrigger) jobExecutionContext.getTrigger();

        JobDataMap jobDataMap = jobDetail.getJobDataMap();
        String partyId = jobDataMap.getString(PARTY_ID);
        String contractId = jobDataMap.getString(CONTRACT_ID);

        log.info("Trying to create report for contract, partyId='{}', contractId='{}', trigger='{}', jobExecutionContext='{}'",
                partyId, contractId, trigger, jobExecutionContext);
        try {
            LocalDateTime toTime = toLocalDateTime(trigger.getCurrentCronTime().toInstant());


            log.info("Report for contract have been successfully created, partyId='{}', contractId='{}', trigger='{}', jobExecutionContext='{}'",
                    partyId, contractId, trigger, jobExecutionContext);
        } catch (StorageException | WRuntimeException ex) {
            throw new JobExecutionException(String.format("Job execution failed (partyId='%s', contractId='%s', trigger='%s', jobExecutionContext='%s'), retry",
                    partyId, contractId, trigger, jobExecutionContext), ex, true);
        } catch (Exception ex) {
            JobExecutionException jobExecutionException = new JobExecutionException(
                    String.format("Job execution failed (partyId='%s', contractId='%s', trigger='%s', jobExecutionContext='%s'), stop triggers",
                            partyId, contractId, trigger, jobExecutionContext), ex);
            jobExecutionException.setUnscheduleAllTriggers(true);
            throw jobExecutionException;
        }
    }
}
