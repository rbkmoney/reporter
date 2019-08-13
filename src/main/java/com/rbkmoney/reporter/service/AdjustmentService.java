package com.rbkmoney.reporter.service;

import com.rbkmoney.reporter.domain.tables.pojos.Adjustment;
import com.rbkmoney.reporter.exception.NotFoundException;
import com.rbkmoney.reporter.exception.StorageException;

public interface AdjustmentService {

    Long save(Adjustment adjustment) throws StorageException;

    Adjustment get(String invoiceId, String paymentId, String adjustmentId) throws StorageException, NotFoundException;

}
