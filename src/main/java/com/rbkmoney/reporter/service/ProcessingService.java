package com.rbkmoney.reporter.service;

import java.util.Optional;

public interface ProcessingService {

    Optional<Long> getLastEventId();
}
