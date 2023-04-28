package xyz.hyrio.common.typeHandler;

import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;
import org.apache.ibatis.type.TypeHandler;

import java.sql.*;
import java.time.LocalDate;
import java.util.Optional;

@MappedTypes(LocalDate.class)
@MappedJdbcTypes(JdbcType.DATE)
public class LocalDate2DateHandler implements TypeHandler<LocalDate> {
    @Override
    public void setParameter(PreparedStatement ps, int i, LocalDate parameter, JdbcType jdbcType) throws SQLException {
        ps.setDate(i, parameter == null ? null : Date.valueOf(parameter));
    }

    @Override
    public LocalDate getResult(ResultSet rs, String columnName) throws SQLException {
        return Optional.ofNullable(rs.getDate(columnName)).map(Date::toLocalDate).orElse(null);
    }

    @Override
    public LocalDate getResult(ResultSet rs, int columnIndex) {
        return null;
    }

    @Override
    public LocalDate getResult(CallableStatement cs, int columnIndex) {
        return null;
    }
}
