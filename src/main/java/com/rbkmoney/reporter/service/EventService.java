package com.rbkmoney.reporter.service;

import java.util.Optional;

public interface EventService {

    Optional<Long> getPayoutLastEventId();

}
