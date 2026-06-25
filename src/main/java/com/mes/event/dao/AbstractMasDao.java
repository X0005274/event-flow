package com.mes.event.dao;

import com.mes.event.command.MasUpsertCommand;
import com.mes.event.exception.DaoException;
import com.mes.event.support.EventLog;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * MAS 공통 규칙을 고정한 템플릿 메서드.
 *
 * <p>규칙: 키 존재여부를 확인하여 존재하면 UPDATE, 없으면 INSERT 한다.
 * 도메인 구현체는 SQL 과 바인딩 방법만 제공한다.
 */
public abstract class AbstractMasDao extends BaseDao implements MasDao {

    protected final EventLog log = EventLog.forClass(getClass());

    protected abstract String existsSql();

    protected abstract String insertSql();

    protected abstract String updateSql();

    protected abstract void bindInsert(PreparedStatement ps, MasUpsertCommand command) throws SQLException;

    protected abstract void bindUpdate(PreparedStatement ps, MasUpsertCommand command) throws SQLException;

    @Override
    public final int upsert(Connection conn, MasUpsertCommand command) {
        boolean exists = existsByKey(conn, existsSql(), command.key());
        String sql = exists ? updateSql() : insertSql();

        log.debug(() -> "MAS " + (exists ? "UPDATE" : "INSERT") + " key=" + command.key());

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            if (exists) {
                bindUpdate(ps, command);
            } else {
                bindInsert(ps, command);
            }
            return ps.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException(
                    "MAS upsert failed. key=" + command.key() + ", exists=" + exists, e);
        }
    }
}
