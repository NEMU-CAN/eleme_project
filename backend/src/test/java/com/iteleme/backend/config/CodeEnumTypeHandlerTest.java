package com.iteleme.backend.config;

import com.iteleme.backend.constant.BusinessStatus;
import com.iteleme.backend.constant.OrderStatus;
import org.apache.ibatis.type.JdbcType;
import org.junit.jupiter.api.Test;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CodeEnumTypeHandlerTest {

    @Test
    void shouldWriteEnumCodeAsInteger() throws Exception {
        PreparedStatement statement = mock(PreparedStatement.class);
        CodeEnumTypeHandler<BusinessStatus> handler =
                new CodeEnumTypeHandler<>(BusinessStatus.class);

        handler.setNonNullParameter(statement, 2, BusinessStatus.OPEN, JdbcType.INTEGER);

        verify(statement).setInt(2, 1);
    }

    @Test
    void shouldReadEnumFromIntegerColumn() throws Exception {
        ResultSet resultSet = mock(ResultSet.class);
        when(resultSet.getInt("order_status")).thenReturn(-1);
        when(resultSet.wasNull()).thenReturn(false);
        CodeEnumTypeHandler<OrderStatus> handler =
                new CodeEnumTypeHandler<>(OrderStatus.class);

        assertSame(OrderStatus.CANCELED, handler.getNullableResult(resultSet, "order_status"));
    }

    @Test
    void shouldReadSqlNullAsNull() throws Exception {
        ResultSet resultSet = mock(ResultSet.class);
        when(resultSet.getInt(1)).thenReturn(0);
        when(resultSet.wasNull()).thenReturn(true);
        CodeEnumTypeHandler<OrderStatus> handler =
                new CodeEnumTypeHandler<>(OrderStatus.class);

        assertNull(handler.getNullableResult(resultSet, 1));
    }

    @Test
    void shouldRejectUnknownDatabaseCodes() throws Exception {
        ResultSet resultSet = mock(ResultSet.class);
        when(resultSet.getInt("status")).thenReturn(99);
        when(resultSet.wasNull()).thenReturn(false);
        CodeEnumTypeHandler<BusinessStatus> handler =
                new CodeEnumTypeHandler<>(BusinessStatus.class);

        assertThrows(SQLException.class, () -> handler.getNullableResult(resultSet, "status"));
    }
}
