package xyz.hyrio.common.typeHandler;

import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;
import org.apache.ibatis.type.TypeHandler;

import java.sql.*;
import java.util.Optional;

/**
 * The DATETIME type converter used in the Mapper of MyBatis.
 *
 * @author Hyrio 2021/06/04 12:10
 */
@MappedTypes({long.class, Long.class})
@MappedJdbcTypes(JdbcType.TIMESTAMP)
@Slf4j
public class Long2DatetimeHandler implements TypeHandler<Long> {
    @Override
    public void setParameter(PreparedStatement ps, int i, Long parameter, JdbcType jdbcType) throws SQLException {
        ps.setTimestamp(i, parameter == null ? null : new Timestamp(parameter));
    }

    @Override
    public Long getResult(ResultSet rs, String columnName) throws SQLException {
        return Optional.ofNullable(rs.getTimestamp(columnName)).map(Timestamp::getTime).orElse(null);
    }

    @Override
    public Long getResult(ResultSet rs, int columnIndex) {
        return null;
    }

    @Override
    public Long getResult(CallableStatement cs, int columnIndex) {
        return null;
    }
}
