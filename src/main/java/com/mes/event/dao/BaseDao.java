package com.mes.event.dao;

import com.mes.event.exception.DaoException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDateTime;

/**
 * JDBC 공통 헬퍼.
 *
 * <p>null-safe 바인딩, enum/Timestamp 변환, 존재여부 조회 등 보일러플레이트를 모은다.
 * Connection 은 절대 이 클래스에서 열거나 닫지 않는다(외부 주입 원칙).
 */
public abstract class BaseDao {

    /** null-safe String 바인딩. */
    protected final void setString(PreparedStatement ps, int index, String value) throws SQLException {
        if (value == null) {
            ps.setNull(index, Types.VARCHAR);
        } else {
            ps.setString(index, value);
        }
    }

    /** null-safe Integer 바인딩. (rs.getInt 와 달리 null 을 0 으로 왜곡하지 않음) */
    protected final void setInteger(PreparedStatement ps, int index, Integer value) throws SQLException {
        if (value == null) {
            ps.setNull(index, Types.INTEGER);
        } else {
            ps.setInt(index, value);
        }
    }

    /** enum → 문자열(name) 바인딩. null 허용. */
    protected final void setEnum(PreparedStatement ps, int index, Enum<?> value) throws SQLException {
        if (value == null) {
            ps.setNull(index, Types.VARCHAR);
        } else {
            ps.setString(index, value.name());
        }
    }

    /** LocalDateTime → java.sql.Timestamp 바인딩. null 허용. */
    protected final void setTimestamp(PreparedStatement ps, int index, LocalDateTime value) throws SQLException {
        if (value == null) {
            ps.setNull(index, Types.TIMESTAMP);
        } else {
            ps.setTimestamp(index, Timestamp.valueOf(value));
        }
    }

    /**
     * 단일 키 존재 여부 조회.
     *
     * @param conn 외부에서 주입된 커넥션 (닫지 않음)
     */
    protected final boolean existsByKey(Connection conn, String sql, String key) {
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, key);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new DaoException("existsByKey failed. sql=" + sql + ", key=" + key, e);
        }
    }
}
