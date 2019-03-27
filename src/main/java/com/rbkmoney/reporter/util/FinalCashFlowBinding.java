package com.rbkmoney.reporter.util;

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
public class FinalCashFlowBinding extends JSONBBinding<FinalCashFlow> {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Converter<Object, FinalCashFlow> converter() {
        return new Converter<Object, FinalCashFlow>() {

            @Override
            public FinalCashFlow from(Object o) {
                try {
                    return o == null ? null : objectMapper.readValue(o.toString(), FinalCashFlow.class);
                } catch (IOException e) {
                    log.warn("ObjectMapper readValue error", e);
                    throw new RuntimeException(e);
                }
            }

            @Override
            public Object to(FinalCashFlow jsonbElement) {
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
            public Class<FinalCashFlow> toType() {
                return FinalCashFlow.class;
            }
        };
    }

    @Override
    public void sql(BindingSQLContext<FinalCashFlow> ctx) throws SQLException {
        ctx.render().visit(DSL.inline(ctx.convert(converter()).value())).sql("::jsonb");

    }

    @Override
    public void register(BindingRegisterContext<FinalCashFlow> ctx) throws SQLException {
        ctx.statement().registerOutParameter(ctx.index(), Types.VARCHAR);
    }

    @Override
    public void set(BindingSetStatementContext<FinalCashFlow> ctx) throws SQLException {
        ctx.statement().setString(ctx.index(), Objects.toString(ctx.convert(converter()).value(), null));
    }

    @Override
    public void get(BindingGetStatementContext<FinalCashFlow> ctx) throws SQLException {
        ctx.convert(converter()).value(ctx.statement().getString(ctx.index()));

    }

    @Override
    public void get(BindingGetResultSetContext<FinalCashFlow> ctx) throws SQLException {
        ctx.convert(converter()).value(ctx.resultSet().getString(ctx.index()));
    }

    @Override
    public void set(BindingSetSQLOutputContext<FinalCashFlow> ctx) throws SQLException {
        throw new SQLFeatureNotSupportedException();

    }

    @Override
    public void get(BindingGetSQLInputContext<FinalCashFlow> ctx) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }
}