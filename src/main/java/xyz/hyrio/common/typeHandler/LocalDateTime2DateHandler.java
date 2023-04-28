package xyz.hyrio.common.typeHandler;

import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;
import org.apache.ibatis.type.TypeHandler;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.Optional;

@MappedTypes(LocalDateTime.class)
@MappedJdbcTypes(JdbcType.DATE)
public class LocalDateTime2DateHandler implements TypeHandler<LocalDateTime> {
    @Override
    public void setParameter(PreparedStatement ps, int i, LocalDateTime parameter, JdbcType jdbcType) throws SQLException {
        ps.setDate(i, parameter == null ? null : Date.valueOf(parameter.toLocalDate()));
    }

    @Override
    public LocalDateTime getResult(ResultSet rs, String columnName) throws SQLException {
        return Optional.ofNullable(rs.getTimestamp(columnName)).map(Timestamp::toLocalDateTime).orElse(null);
    }

    @Override
    public LocalDateTime getResult(ResultSet rs, int columnIndex) {
        return null;
    }

    @Override
    public LocalDateTime getResult(CallableStatement cs, int columnIndex) {
        return null;
    }
}
