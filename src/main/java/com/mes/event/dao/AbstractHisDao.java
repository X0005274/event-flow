package com.mes.event.dao;

import com.mes.event.command.HisInsertCommand;
import com.mes.event.exception.DaoException;
import com.mes.event.support.EventLog;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * HIS 공통 규칙을 고정한 템플릿 메서드. 항상 INSERT.
 */
public abstract class AbstractHisDao extends BaseDao implements HisDao {

    protected final EventLog log = EventLog.forClass(getClass());

    protected abstract String insertSql();

    protected abstract void bindInsert(PreparedStatement ps, HisInsertCommand command) throws SQLException;

    @Override
    public final int insert(Connection conn, HisInsertCommand command) {
        log.debug(() -> "HIS INSERT key=" + command.key() + ", eventId=" + command.common().eventId());

        try (PreparedStatement ps = conn.prepareStatement(insertSql())) {
            bindInsert(ps, command);
            return ps.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException(
                    "HIS insert failed. key=" + command.key()
                            + ", eventId=" + command.common().eventId(), e);
        }
    }
}
