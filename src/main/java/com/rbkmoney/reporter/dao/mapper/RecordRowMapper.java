package com.rbkmoney.reporter.dao.mapper;

import com.rbkmoney.geck.common.util.TypeUtil;
import com.rbkmoney.reporter.util.JSONBBinding;
import lombok.RequiredArgsConstructor;
import org.jooq.Field;
import org.jooq.Table;
import org.jooq.TableRecord;
import org.jooq.impl.TableRecordImpl;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

@RequiredArgsConstructor
public class RecordRowMapper<T> implements RowMapper<T> {

    private final Table table;
    private final Class<T> type;

    @Override
    public T mapRow(ResultSet resultSet, int i) throws SQLException {
        ResultSetMetaData rsMetaData = resultSet.getMetaData();
        int columnCount = rsMetaData.getColumnCount();

        TableRecord record = new TableRecordImpl(table);
        for (int column = 1; column <= columnCount; column++) {
            String columnName = rsMetaData.getColumnName(column);
            Field field = record.field(columnName);

            Object value = getFieldValue(field, resultSet);
            if (!resultSet.wasNull()) {
                record.set(field, value);
            }
        }
        return record.into(type);
    }

    private Object getFieldValue(Field field, ResultSet resultSet) throws SQLException {
        if (field.getDataType().isBinary()) {
            return resultSet.getBytes(field.getName());
        }
        if (field.getType().isEnum()) {
            return TypeUtil.toEnumField(resultSet.getString(field.getName()), field.getType());
        }
        if (field.getBinding() instanceof JSONBBinding) {
            return field.getBinding().converter().from(resultSet.getString(resultSet.findColumn(field.getName())));
        }
        return resultSet.getObject(field.getName(), field.getType());
    }
}
