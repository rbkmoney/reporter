package com.rbkmoney.reporter.service;

import org.jooq.Query;

import java.util.List;

public interface BatchService {

    void save(List<Query> queries);

}
