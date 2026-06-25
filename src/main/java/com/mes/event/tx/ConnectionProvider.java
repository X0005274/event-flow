package com.mes.event.tx;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * 커넥션 획득 이음새(seam).
 *
 * <p>이 모듈은 특정 커넥션 풀에 의존하지 않는다. 기존 MES 의 풀/DataSource 에 람다 한 줄로 연결한다:
 * <pre>{@code
 * ConnectionProvider provider = () -> myDataSource.getConnection();
 * // 또는 사내 레거시 풀:
 * ConnectionProvider provider = LegacyConnPool::borrow;
 * }</pre>
 */
@FunctionalInterface
public interface ConnectionProvider {

    Connection getConnection() throws SQLException;
}
