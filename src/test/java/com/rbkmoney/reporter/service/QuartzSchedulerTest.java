package com.rbkmoney.reporter.service;

import com.rbkmoney.damsel.domain_config.RepositoryClientSrv;
import com.rbkmoney.damsel.payment_processing.PartyManagementSrv;
import com.rbkmoney.eventstock.client.EventPublisher;
import com.rbkmoney.reporter.AbstractIntegrationTest;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.quartz.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.TimeZone;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertTrue;
import static org.quartz.CronScheduleBuilder.cronSchedule;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;

public class QuartzSchedulerTest extends AbstractIntegrationTest {

    @Autowired
    private Scheduler scheduler;

    @MockBean
    private StatisticService statisticService;

    @MockBean
    private SignService signService;

    @MockBean
    private EventPublisher eventPublisher;

    @MockBean
    private RepositoryClientSrv.Iface dominantClient;

    @MockBean
    private PartyManagementSrv.Iface partyManagementClient;

    private static AtomicInteger schedulerCounter = new AtomicInteger(0);

    @Test
    public void schedulerTest() throws SchedulerException, InterruptedException {
        JobDetail job = newJob(SampleJob.class)
                .withIdentity("job1", "group1")
                .build();
        CronTrigger trigger = newTrigger()
                .withIdentity("a", "t")
                .withSchedule(cronSchedule("0/5 * * * * ?").inTimeZone(TimeZone.getDefault()))
                .forJob(job)
                .build();

        scheduler.scheduleJob(job, trigger);

        Thread.sleep(15000);
        assertTrue("The number of trigger runs is less than expected", schedulerCounter.get() >= 2);
    }

    @Slf4j
    public static class SampleJob implements Job {

        public void execute(JobExecutionContext context) throws JobExecutionException {
            schedulerCounter.incrementAndGet();
            log.warn("Sample Job Executing {}", schedulerCounter.get());
        }
    }

}
