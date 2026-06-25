package com.mes.event.support;

import com.mes.event.exception.SqlException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDateTime;

/**
 * JDBC 보일러플레이트(반복 코드)를 한곳에 모은 작은 도우미.
 *
 * <p><b>왜 필요한가?</b> 순수 JDBC 로 SQL 을 실행하면 매번 다음을 반복해야 한다.
 * <ol>
 *   <li>{@link PreparedStatement} 를 만들고(try-with-resources 로 자동 close),</li>
 *   <li>물음표(?) 자리에 값을 채우고(=바인딩),</li>
 *   <li>실행한 뒤 {@link SQLException} 을 처리한다.</li>
 * </ol>
 * 이 클래스가 그 반복을 대신 해 주므로, 각 이벤트 처리기는 "어떤 SQL 에 어떤 값을 넣을지"에만 집중하면 된다.
 *
 * <p><b>중요:</b> 여기서는 {@link Connection} 을 절대 새로 열거나 닫지 않는다.
 * 커넥션(=DB 연결)과 트랜잭션(=commit/rollback 경계)은 호출하는 쪽이 소유한다.
 * (이 모듈에서는 {@code TransactionManager} 또는 기존 MES 시스템이 그 역할을 한다.)
 *
 * <p>모든 메서드가 {@code static} 이라 인스턴스를 만들 필요 없이 {@code Jdbc.update(...)} 처럼 바로 쓴다.
 */
public final class Jdbc {

    private Jdbc() {
        // 도우미 클래스이므로 인스턴스 생성 금지.
    }

    /**
     * "PreparedStatement 의 물음표(?) 자리에 값을 채우는 방법"을 담는 함수형 인터페이스.
     *
     * <p>람다로 넘기면 된다. 예) {@code ps -> { Jdbc.setString(ps, 1, lotId); }}
     * 값 바인딩 도중 {@link SQLException} 이 날 수 있으므로 throws 를 허용한다.
     */
    @FunctionalInterface
    public interface Binder {
        void bind(PreparedStatement ps) throws SQLException;
    }

    /**
     * INSERT / UPDATE 같은 변경 SQL 을 실행한다.
     *
     * @param conn   외부에서 받은 커넥션 (이 메서드는 닫지 않는다)
     * @param sql    실행할 SQL (물음표 ? 포함)
     * @param binder 물음표 자리에 값을 채우는 코드(람다)
     * @return 영향받은(insert/update 된) 행 수
     */
    public static int update(Connection conn, String sql, Binder binder) {
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            binder.bind(ps);
            return ps.executeUpdate();
        } catch (SQLException e) {
            // SQLException 을 그대로 던지지 않고, 실패한 SQL 을 메시지에 담아 추적하기 쉽게 만든다.
            throw new SqlException("SQL 실행 실패: " + sql, e);
        }
    }

    /**
     * 주어진 키(key)에 해당하는 행이 테이블에 이미 존재하는지 확인한다.
     *
     * <p>보통 {@code "SELECT 1 FROM 테이블 WHERE 키컬럼 = ?"} 형태의 SQL 을 넘긴다.
     * 행이 한 건이라도 있으면 {@code true}.
     *
     * @param conn 외부에서 받은 커넥션 (닫지 않는다)
     * @param sql  존재 여부 조회 SQL (물음표 1개)
     * @param key  찾을 키 값
     */
    public static boolean exists(Connection conn, String sql, String key) {
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, key);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next(); // 결과 행이 하나라도 있으면 존재함
            }
        } catch (SQLException e) {
            throw new SqlException("존재 여부 조회 실패: " + sql + " (key=" + key + ")", e);
        }
    }

    // ===== null 을 안전하게 처리하는 값 바인딩 도우미들 =====
    // JDBC 의 ps.setXxx 는 값이 null 일 때 처리가 까다롭다.
    // 아래 메서드들은 값이 null 이면 "DB 의 NULL" 로, 아니면 정상 값으로 넣어 준다.

    /** 문자열 값을 채운다. null 이면 DB NULL 로 저장. */
    public static void setString(PreparedStatement ps, int index, String value) throws SQLException {
        if (value == null) {
            ps.setNull(index, Types.VARCHAR);
        } else {
            ps.setString(index, value);
        }
    }

    /**
     * 정수 값을 채운다. null 이면 DB NULL 로 저장.
     *
     * <p>(주의: 일반 {@code ps.setInt} 는 null 을 받을 수 없고, 조회 시 null 을 0 으로 왜곡할 수 있다.
     * 그래서 null 을 명시적으로 다루는 이 메서드를 쓴다.)
     */
    public static void setInteger(PreparedStatement ps, int index, Integer value) throws SQLException {
        if (value == null) {
            ps.setNull(index, Types.INTEGER);
        } else {
            ps.setInt(index, value);
        }
    }

    /** enum 값을 문자열(name)로 채운다. 예) TxnCode.MOVE → "MOVE". null 이면 DB NULL. */
    public static void setEnum(PreparedStatement ps, int index, Enum<?> value) throws SQLException {
        if (value == null) {
            ps.setNull(index, Types.VARCHAR);
        } else {
            ps.setString(index, value.name());
        }
    }

    /** 날짜/시각 값을 채운다. Java 의 LocalDateTime 을 DB 의 Timestamp 로 변환. null 이면 DB NULL. */
    public static void setTimestamp(PreparedStatement ps, int index, LocalDateTime value) throws SQLException {
        if (value == null) {
            ps.setNull(index, Types.TIMESTAMP);
        } else {
            ps.setTimestamp(index, Timestamp.valueOf(value));
        }
    }
}
