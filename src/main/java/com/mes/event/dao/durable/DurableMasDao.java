package com.mes.event.dao.durable;

import com.mes.event.command.CommonColumns;
import com.mes.event.command.MasUpsertCommand;
import com.mes.event.dao.AbstractMasDao;
import com.mes.event.sql.DurableSql;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Map;

/**
 * MES_DURABLE_MAS upsert DAO.
 */
public final class DurableMasDao extends AbstractMasDao {

    @Override
    protected String existsSql() {
        return DurableSql.EXISTS_MAS;
    }

    @Override
    protected String insertSql() {
        return DurableSql.INSERT_MAS;
    }

    @Override
    protected String updateSql() {
        return DurableSql.UPDATE_MAS;
    }

    @Override
    protected void bindInsert(PreparedStatement ps, MasUpsertCommand command) throws SQLException {
        CommonColumns c = command.common();
        Map<String, Object> s = command.specific();
        int i = 1;
        setString(ps, i++, command.key());                 // DURABLE_ID
        setString(ps, i++, (String) s.get("DURABLE_TYPE"));
        setInteger(ps, i++, (Integer) s.get("USAGE_COUNT"));
        setEnum(ps, i++, c.txnCode());
        setString(ps, i++, c.factoryId());
        setString(ps, i++, c.equipmentId());
        setString(ps, i++, c.eventId());
        setTimestamp(ps, i++, c.eventTime());
        setString(ps, i++, c.userId());
        setString(ps, i, c.comment());
    }

    @Override
    protected void bindUpdate(PreparedStatement ps, MasUpsertCommand command) throws SQLException {
        CommonColumns c = command.common();
        Map<String, Object> s = command.specific();
        int i = 1;
        setString(ps, i++, (String) s.get("DURABLE_TYPE"));
        setInteger(ps, i++, (Integer) s.get("USAGE_COUNT"));
        setEnum(ps, i++, c.txnCode());
        setString(ps, i++, c.factoryId());
        setString(ps, i++, c.equipmentId());
        setString(ps, i++, c.eventId());
        setTimestamp(ps, i++, c.eventTime());
        setString(ps, i++, c.userId());
        setString(ps, i++, c.comment());
        setString(ps, i, command.key());                   // WHERE DURABLE_ID = ?
    }
}
