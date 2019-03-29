package com.rbkmoney.reporter.util.json;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.jooq.*;
import org.jooq.impl.DSL;

import java.io.IOException;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.Types;
import java.util.Objects;

@Slf4j
public class PayoutSummaryBinding extends JSONBBinding<PayoutSummary> {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Converter<Object, PayoutSummary> converter() {
        return new Converter<Object, PayoutSummary>() {

            @Override
            public PayoutSummary from(Object o) {
                try {
                    return o == null ? null : objectMapper.readValue(o.toString(), PayoutSummary.class);
                } catch (IOException e) {
                    log.warn("ObjectMapper readValue error", e);
                    throw new RuntimeException(e);
                }
            }

            @Override
            public Object to(PayoutSummary jsonbElement) {
                try {
                    return jsonbElement == null ? null : objectMapper.writeValueAsString(jsonbElement);
                } catch (JsonProcessingException e) {
                    log.error("ObjectMapper writeValueAsString error", e);
                    throw new RuntimeException(e);
                }
            }

            @Override
            public Class<Object> fromType() {
                return Object.class;
            }

            @Override
            public Class<PayoutSummary> toType() {
                return PayoutSummary.class;
            }
        };
    }

    @Override
    public void sql(BindingSQLContext<PayoutSummary> ctx) throws SQLException {
        ctx.render().visit(DSL.inline(ctx.convert(converter()).value())).sql("::jsonb");

    }

    @Override
    public void register(BindingRegisterContext<PayoutSummary> ctx) throws SQLException {
        ctx.statement().registerOutParameter(ctx.index(), Types.VARCHAR);
    }

    @Override
    public void set(BindingSetStatementContext<PayoutSummary> ctx) throws SQLException {
        ctx.statement().setString(ctx.index(), Objects.toString(ctx.convert(converter()).value(), null));
    }

    @Override
    public void get(BindingGetStatementContext<PayoutSummary> ctx) throws SQLException {
        ctx.convert(converter()).value(ctx.statement().getString(ctx.index()));

    }

    @Override
    public void get(BindingGetResultSetContext<PayoutSummary> ctx) throws SQLException {
        ctx.convert(converter()).value(ctx.resultSet().getString(ctx.index()));
    }

    @Override
    public void set(BindingSetSQLOutputContext<PayoutSummary> ctx) throws SQLException {
        throw new SQLFeatureNotSupportedException();

    }

    @Override
    public void get(BindingGetSQLInputContext<PayoutSummary> ctx) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }
}