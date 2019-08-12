package com.rbkmoney.reporter.service;

import com.rbkmoney.reporter.domain.tables.pojos.Payout;
import com.rbkmoney.reporter.exception.NotFoundException;
import com.rbkmoney.reporter.exception.StorageException;

public interface PayoutService {

    Long save(Payout payout) throws StorageException;

    Payout get(String payoutId) throws StorageException, NotFoundException;

}
