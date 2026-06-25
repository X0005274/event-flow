package com.mes.event.dao.durable;

import com.mes.event.command.CommonColumns;
import com.mes.event.command.HisInsertCommand;
import com.mes.event.dao.AbstractHisDao;
import com.mes.event.sql.DurableSql;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Map;

/**
 * MES_DURABLE_HIS insert DAO.
 */
public final class DurableHisDao extends AbstractHisDao {

    @Override
    protected String insertSql() {
        return DurableSql.INSERT_HIS;
    }

    @Override
    protected void bindInsert(PreparedStatement ps, HisInsertCommand command) throws SQLException {
        CommonColumns c = command.common();
        Map<String, Object> s = command.specific();
        int i = 1;
        setString(ps, i++, c.eventId());                    // EVENT_ID
        setString(ps, i++, command.key());                  // DURABLE_ID
        setString(ps, i++, (String) s.get("DURABLE_TYPE"));
        setInteger(ps, i++, (Integer) s.get("USAGE_COUNT"));
        setEnum(ps, i++, c.txnCode());
        setString(ps, i++, c.factoryId());
        setString(ps, i++, c.equipmentId());
        setTimestamp(ps, i++, c.eventTime());
        setString(ps, i++, c.userId());
        setString(ps, i, c.comment());
    }
}
