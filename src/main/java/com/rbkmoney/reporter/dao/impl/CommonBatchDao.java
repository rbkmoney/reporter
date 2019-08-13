package com.rbkmoney.reporter.dao.impl;

import com.rbkmoney.dao.impl.AbstractGenericDao;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CommonBatchDao extends AbstractGenericDao {

    @Autowired
    public CommonBatchDao(HikariDataSource dataSource) {
        super(dataSource);
    }

}
