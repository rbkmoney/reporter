package com.rbkmoney.reporter.service.impl;

import com.rbkmoney.reporter.dao.impl.CommonBatchDao;
import com.rbkmoney.reporter.exception.DaoException;
import com.rbkmoney.reporter.exception.StorageException;
import com.rbkmoney.reporter.service.BatchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jooq.Query;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class BatchServiceImpl implements BatchService {

    private final CommonBatchDao commonBatchDao;

    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public void save(List<Query> queries) {
        log.info("Trying to save batch invoicing events, size={}", queries.size());
        try {
            commonBatchDao.batchExecute(queries);
            log.info("Batch invoicing events have been saved, size={}", queries.size());
        } catch (Exception ex) {
            throw new StorageException(
                    String.format("Failed to save batch invoicing events, size='%s'", queries.size()), ex
            );
        }
    }
}
