package com.mes.event.tx;

import com.mes.event.exception.EventProcessingException;
import com.mes.event.support.EventLog;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * 트랜잭션 경계 관리자.
 *
 * <p><b>선택 사항</b>이다. 기존 MES 가 이미 트랜잭션/커넥션을 관리한다면 이 클래스를 쓰지 말고
 * {@code EventDispatcher#dispatchWithin(conn, event)} 로 기존 트랜잭션에 합류시키면 된다.
 *
 * <p>커넥션은 {@link ConnectionProvider} 로 외부 주입받으며, commit/rollback/close 및
 * autoCommit 복원을 이 클래스가 단독으로 책임진다(트랜잭션 경계 일원화).
 */
public final class TransactionManager {

    private static final EventLog log = EventLog.forClass(TransactionManager.class);

    private final ConnectionProvider connectionProvider;

    public TransactionManager(ConnectionProvider connectionProvider) {
        if (connectionProvider == null) {
            throw new IllegalArgumentException("connectionProvider must not be null");
        }
        this.connectionProvider = connectionProvider;
    }

    public void executeInTransaction(TransactionalWork work) {
        try (Connection conn = connectionProvider.getConnection()) {
            boolean previousAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);
            try {
                work.execute(conn);
                conn.commit();
            } catch (Exception ex) {
                safeRollback(conn);
                throw new EventProcessingException("transaction failed and was rolled back", ex);
            } finally {
                restoreAutoCommit(conn, previousAutoCommit);
            }
        } catch (SQLException e) {
            throw new EventProcessingException("connection acquisition/close error", e);
        }
    }

    private void safeRollback(Connection conn) {
        try {
            conn.rollback();
            log.warn("transaction rolled back");
        } catch (SQLException rollbackEx) {
            log.error("rollback failed", rollbackEx);
        }
    }

    private void restoreAutoCommit(Connection conn, boolean previousAutoCommit) {
        try {
            conn.setAutoCommit(previousAutoCommit);
        } catch (SQLException e) {
            log.warn("failed to restore autoCommit flag", e);
        }
    }
}
