package com.rbkmoney.reporter.config;

import com.rbkmoney.reporter.dao.ContractMetaDao;
import com.rbkmoney.reporter.scheduler.ScheduledJob;
import com.rbkmoney.reporter.scheduler.impl.PendingReportScheduledJobImpl;
import com.rbkmoney.reporter.scheduler.impl.SynchronizationInnerJobsScheduledJobImpl;
import com.rbkmoney.reporter.service.JobService;
import com.rbkmoney.reporter.service.ReportService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling
public class ScheduledJobsBeanEnableConfig {

    @Bean
    @ConditionalOnProperty(value = "info.single-instance-mode", havingValue = "true")
    public ScheduledJob synchronizationInnerJobsScheduledJob(ContractMetaDao contractMetaDao, JobService jobService) {
        return new SynchronizationInnerJobsScheduledJobImpl(contractMetaDao, jobService);
    }

    @Bean
    @ConditionalOnProperty(value = "info.single-instance-mode", havingValue = "true")
    public ScheduledJob pendingReportScheduledJob(ReportService reportService) {
        return new PendingReportScheduledJobImpl(reportService);
    }
}