package com.rbkmoney.reporter.scheduler.impl;

import com.rbkmoney.reporter.dao.ContractMetaDao;
import com.rbkmoney.reporter.domain.tables.pojos.ContractMeta;
import com.rbkmoney.reporter.exception.DaoException;
import com.rbkmoney.reporter.exception.ScheduleProcessingException;
import com.rbkmoney.reporter.scheduler.ScheduledJob;
import com.rbkmoney.reporter.service.JobService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.SchedulerException;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class SynchronizationInnerJobsScheduledJobImpl implements ScheduledJob {

    private final ContractMetaDao contractMetaDao;

    private final JobService jobService;

    @Scheduled(fixedDelay = 60 * 1000)
    @Override
    public void run() {
        try {
            log.info("Starting synchronization of jobs...");
            List<ContractMeta> activeContracts = contractMetaDao.getAllActiveContracts();
            if (activeContracts.isEmpty()) {
                log.info("No active contracts found, nothing to do");
                return;
            }

            for (ContractMeta contractMeta : activeContracts) {
                jobService.createJob(contractMeta);
            }
        } catch (DaoException | SchedulerException ex) {
            throw new ScheduleProcessingException("Failed to sync jobs", ex);
        } finally {
            log.info("End synchronization of jobs");
        }
    }
}
