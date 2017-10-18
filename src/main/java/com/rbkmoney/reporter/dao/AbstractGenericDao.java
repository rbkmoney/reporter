package com.rbkmoney.reporter.dao;

import com.rbkmoney.reporter.exception.DaoException;
import org.jooq.*;
import org.jooq.conf.ParamType;
import org.jooq.impl.DSL;
import org.jooq.impl.DefaultConfiguration;
import org.springframework.core.NestedRuntimeException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.JdbcUpdateAffectedIncorrectNumberOfRowsException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.SingleColumnRowMapper;
import org.springframework.jdbc.core.namedparam.*;
import org.springframework.jdbc.support.KeyHolder;

import javax.sql.DataSource;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public abstract class AbstractGenericDao extends NamedParameterJdbcDaoSupport implements GenericDao {

    private final DSLContext dslContext;

    public AbstractGenericDao(DataSource dataSource) {
        setDataSource(dataSource);
        Configuration configuration = new DefaultConfiguration();
        configuration.set(SQLDialect.POSTGRES_9_5);
        this.dslContext = DSL.using(configuration);
    }

    protected DSLContext getDslContext() {
        return dslContext;
    }

    @Override
    public <T> T fetchOne(Query query, Class<T> type) throws DaoException {
        return fetchOne(query, type, getNamedParameterJdbcTemplate());
    }

    @Override
    public <T> T fetchOne(Query query, Class<T> type, NamedParameterJdbcTemplate namedParameterJdbcTemplate) throws DaoException {
        return fetchOne(query, new SingleColumnRowMapper<>(type), namedParameterJdbcTemplate);
    }

    @Override
    public <T> T fetchOne(Query query, RowMapper<T> rowMapper) throws DaoException {
        return fetchOne(query, rowMapper, getNamedParameterJdbcTemplate());
    }

    @Override
    public <T> T fetchOne(String namedSql, SqlParameterSource parameterSource, RowMapper<T> rowMapper) throws DaoException {
        return fetchOne(namedSql, parameterSource, rowMapper, getNamedParameterJdbcTemplate());
    }

    @Override
    public <T> T fetchOne(Query query, RowMapper<T> rowMapper, NamedParameterJdbcTemplate namedParameterJdbcTemplate) throws DaoException {
        return fetchOne(query.getSQL(ParamType.NAMED), toSqlParameterSource(query.getParams()), rowMapper, namedParameterJdbcTemplate);
    }

    @Override
    public <T> T fetchOne(String namedSql, SqlParameterSource parameterSource, RowMapper<T> rowMapper, NamedParameterJdbcTemplate namedParameterJdbcTemplate) throws DaoException {
        try {
            return namedParameterJdbcTemplate.queryForObject(
                    namedSql,
                    parameterSource,
                    rowMapper
            );
        } catch (EmptyResultDataAccessException ex) {
            return null;
        } catch (NestedRuntimeException ex) {
            throw new DaoException(ex);
        }
    }

    @Override
    public <T> List<T> fetch(Query query, RowMapper<T> rowMapper) throws DaoException {
        return fetch(query, rowMapper, getNamedParameterJdbcTemplate());
    }

    @Override
    public <T> List<T> fetch(String namedSql, SqlParameterSource parameterSource, RowMapper<T> rowMapper) throws DaoException {
        return fetch(namedSql, parameterSource, rowMapper, getNamedParameterJdbcTemplate());
    }

    @Override
    public <T> List<T> fetch(Query query, RowMapper<T> rowMapper, NamedParameterJdbcTemplate namedParameterJdbcTemplate) throws DaoException {
        return fetch(query.getSQL(ParamType.NAMED), toSqlParameterSource(query.getParams()), rowMapper, namedParameterJdbcTemplate);
    }

    @Override
    public <T> List<T> fetch(String namedSql, SqlParameterSource parameterSource, RowMapper<T> rowMapper, NamedParameterJdbcTemplate namedParameterJdbcTemplate) throws DaoException {
        try {
            return namedParameterJdbcTemplate.query(
                    namedSql,
                    parameterSource,
                    rowMapper
            );
        } catch (NestedRuntimeException e) {
            throw new DaoException(e);
        }
    }

    @Override
    public void executeOne(Query query) throws DaoException {
        execute(query, 1);
    }

    @Override
    public void execute(Query query) throws DaoException {
        execute(query, -1);
    }

    @Override
    public void execute(Query query, int expectedRowsAffected) throws DaoException {
        execute(query, expectedRowsAffected, getNamedParameterJdbcTemplate());
    }

    @Override
    public void execute(Query query, int expectedRowsAffected, NamedParameterJdbcTemplate namedParameterJdbcTemplate) throws DaoException {
        execute(query.getSQL(ParamType.NAMED), toSqlParameterSource(query.getParams()), expectedRowsAffected, namedParameterJdbcTemplate);
    }

    @Override
    public void executeOne(String namedSql, SqlParameterSource parameterSource) throws DaoException {
        execute(namedSql, parameterSource, 1);
    }

    @Override
    public void execute(String namedSql) throws DaoException {
        execute(namedSql, EmptySqlParameterSource.INSTANCE);
    }

    @Override
    public void execute(String namedSql, SqlParameterSource parameterSource) throws DaoException {
        execute(namedSql, parameterSource, -1);
    }

    @Override
    public void execute(String namedSql, SqlParameterSource parameterSource, int expectedRowsAffected) throws DaoException {
        execute(namedSql, parameterSource, expectedRowsAffected, getNamedParameterJdbcTemplate());
    }

    @Override
    public void execute(String namedSql, SqlParameterSource parameterSource, int expectedRowsAffected, NamedParameterJdbcTemplate namedParameterJdbcTemplate) throws DaoException {
        try {
            int rowsAffected = namedParameterJdbcTemplate.update(
                    namedSql,
                    parameterSource);

            if (expectedRowsAffected != -1 && rowsAffected != expectedRowsAffected) {
                throw new JdbcUpdateAffectedIncorrectNumberOfRowsException(namedSql, expectedRowsAffected, rowsAffected);
            }
        } catch (NestedRuntimeException ex) {
            throw new DaoException(ex);
        }
    }

    @Override
    public void executeOneWithReturn(Query query, KeyHolder keyHolder) throws DaoException {
        executeOneWithReturn(query.getSQL(ParamType.NAMED), toSqlParameterSource(query.getParams()), keyHolder);
    }

    @Override
    public void executeOneWithReturn(String namedSql, SqlParameterSource parameterSource, KeyHolder keyHolder) throws DaoException {
        executeWithReturn(namedSql, parameterSource, 1, keyHolder);
    }

    @Override
    public void executeWithReturn(Query query, KeyHolder keyHolder) throws DaoException {
        executeWithReturn(query.getSQL(ParamType.NAMED), toSqlParameterSource(query.getParams()), -1, keyHolder);
    }

    @Override
    public void executeWithReturn(Query query, int expectedRowsAffected, KeyHolder keyHolder) throws DaoException {
        executeWithReturn(query.getSQL(ParamType.NAMED), toSqlParameterSource(query.getParams()), expectedRowsAffected, getNamedParameterJdbcTemplate(), keyHolder);
    }

    @Override
    public void executeWithReturn(Query query, int expectedRowsAffected, NamedParameterJdbcTemplate namedParameterJdbcTemplate, KeyHolder keyHolder) throws DaoException {
        executeWithReturn(query.getSQL(ParamType.NAMED), toSqlParameterSource(query.getParams()), expectedRowsAffected, namedParameterJdbcTemplate, keyHolder);
    }

    @Override
    public void executeWithReturn(String namedSql, KeyHolder keyHolder) throws DaoException {
        executeWithReturn(namedSql,  EmptySqlParameterSource.INSTANCE, keyHolder);
    }

    @Override
    public void executeWithReturn(String namedSql, SqlParameterSource parameterSource, KeyHolder keyHolder) throws DaoException {
        executeWithReturn(namedSql, parameterSource, -1, keyHolder);
    }

    @Override
    public void executeWithReturn(String namedSql, SqlParameterSource parameterSource, int expectedRowsAffected, KeyHolder keyHolder) throws DaoException {
        executeWithReturn(namedSql, parameterSource, expectedRowsAffected, getNamedParameterJdbcTemplate(), keyHolder);
    }

    @Override
    public void executeWithReturn(String namedSql, SqlParameterSource parameterSource, int expectedRowsAffected, NamedParameterJdbcTemplate namedParameterJdbcTemplate, KeyHolder keyHolder) throws DaoException {
        try {
            int rowsAffected = namedParameterJdbcTemplate.update(
                    namedSql,
                    parameterSource,
                    keyHolder);

            if (expectedRowsAffected != -1 && rowsAffected != expectedRowsAffected) {
                throw new JdbcUpdateAffectedIncorrectNumberOfRowsException(namedSql, expectedRowsAffected, rowsAffected);
            }
        } catch (NestedRuntimeException ex) {
            throw new DaoException(ex);
        }
    }

    public SqlParameterSource toSqlParameterSource(Map<String, Param<?>> params) {
        MapSqlParameterSource sqlParameterSource = new MapSqlParameterSource();
        for (Map.Entry<String, Param<?>> entry : params.entrySet()) {
            Param<?> param = entry.getValue();
            if (param.getValue() instanceof LocalDateTime || param.getValue() instanceof EnumType) {
                sqlParameterSource.addValue(entry.getKey(), param.getValue(), Types.OTHER);
            } else {
                sqlParameterSource.addValue(entry.getKey(), param.getValue());
            }
        }
        return sqlParameterSource;
    }

}
