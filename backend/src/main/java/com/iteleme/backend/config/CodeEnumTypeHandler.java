package com.iteleme.backend.config;

import com.iteleme.backend.constant.CodeEnum;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * 让 MyBatis 以整数编码读写业务枚举，而不是使用枚举名称。
 */
public class CodeEnumTypeHandler<E extends Enum<E> & CodeEnum> extends BaseTypeHandler<E> {
    private final Class<E> enumType;

    public CodeEnumTypeHandler(Class<E> enumType) {
        if (enumType == null) {
            throw new IllegalArgumentException("枚举类型不能为空");
        }
        this.enumType = enumType;
    }

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, E parameter, JdbcType jdbcType)
            throws SQLException {
        ps.setInt(i, parameter.getCode());
    }

    @Override
    public E getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return fromDatabase(rs.getInt(columnName), rs.wasNull());
    }

    @Override
    public E getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return fromDatabase(rs.getInt(columnIndex), rs.wasNull());
    }

    @Override
    public E getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return fromDatabase(cs.getInt(columnIndex), cs.wasNull());
    }

    private E fromDatabase(int code, boolean wasNull) throws SQLException {
        if (wasNull) {
            return null;
        }
        try {
            return CodeEnum.fromCode(enumType, code);
        } catch (IllegalArgumentException e) {
            throw new SQLException("数据库中存在非法的" + enumType.getSimpleName() + "编码: " + code, e);
        }
    }
}
