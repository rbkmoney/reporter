package com.rbkmoney.reporter.dao.impl;

import com.rbkmoney.reporter.dao.AbstractGenericDao;
import com.rbkmoney.reporter.exception.DaoException;
import org.jooq.Query;
import org.jooq.conf.ParamType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.NestedRuntimeException;
import org.springframework.jdbc.JdbcUpdateAffectedIncorrectNumberOfRowsException;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class CommonBatchDao extends AbstractGenericDao {

    @Autowired
    public CommonBatchDao(DataSource dataSource) {
        super(dataSource);
    }

    public void batchExecute(List<Query> queries) throws DaoException {
        batchExecute(queries, -1);
    }

    public void batchExecute(List<Query> queries, int expectedRowsPerQueryAffected) throws DaoException {
        batchExecute(queries, expectedRowsPerQueryAffected, getNamedParameterJdbcTemplate());
    }

    public void batchExecute(List<Query> queries, int expectedRowsPerQueryAffected, NamedParameterJdbcTemplate namedParameterJdbcTemplate) throws DaoException {
        queries.stream()
                .collect(
                        Collectors.groupingBy(
                                query -> query.getSQL(ParamType.NAMED),
                                LinkedHashMap::new,
                                Collectors.mapping(query -> toSqlParameterSource(query.getParams()), Collectors.toList())
                        )
                )
                .forEach(
                        (namedSql, parameterSources) -> batchExecute(
                                namedSql,
                                parameterSources,
                                expectedRowsPerQueryAffected,
                                namedParameterJdbcTemplate
                        )
                );
    }

    public void batchExecute(String namedSql, List<SqlParameterSource> parameterSources) throws DaoException {
        batchExecute(namedSql, parameterSources, -1);
    }

    public void batchExecute(String namedSql, List<SqlParameterSource> parameterSources, int expectedRowsPerQueryAffected) throws DaoException {
        batchExecute(namedSql, parameterSources, expectedRowsPerQueryAffected, getNamedParameterJdbcTemplate());
    }

    public void batchExecute(String namedSql, List<SqlParameterSource> parameterSources, int expectedRowsPerQueryAffected, NamedParameterJdbcTemplate namedParameterJdbcTemplate) throws DaoException {
        try {
            int[] rowsPerBatchAffected = namedParameterJdbcTemplate.batchUpdate(namedSql, parameterSources.toArray(new SqlParameterSource[0]));

            if (rowsPerBatchAffected.length != parameterSources.size()) {
                throw new JdbcUpdateAffectedIncorrectNumberOfRowsException(namedSql, parameterSources.size(), rowsPerBatchAffected.length);
            }

            for (int rowsAffected : rowsPerBatchAffected) {
                if (expectedRowsPerQueryAffected != -1 && rowsAffected != expectedRowsPerQueryAffected) {
                    throw new JdbcUpdateAffectedIncorrectNumberOfRowsException(namedSql, expectedRowsPerQueryAffected, rowsAffected);
                }
            }
        } catch (NestedRuntimeException ex) {
            throw new DaoException(ex);
        }
    }
}
