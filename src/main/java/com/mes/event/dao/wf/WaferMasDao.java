package com.mes.event.dao.wf;

import com.mes.event.command.CommonColumns;
import com.mes.event.command.MasUpsertCommand;
import com.mes.event.dao.AbstractMasDao;
import com.mes.event.sql.WaferSql;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Map;

/**
 * MES_WF_MAS upsert DAO.
 */
public final class WaferMasDao extends AbstractMasDao {

    @Override
    protected String existsSql() {
        return WaferSql.EXISTS_MAS;
    }

    @Override
    protected String insertSql() {
        return WaferSql.INSERT_MAS;
    }

    @Override
    protected String updateSql() {
        return WaferSql.UPDATE_MAS;
    }

    @Override
    protected void bindInsert(PreparedStatement ps, MasUpsertCommand command) throws SQLException {
        CommonColumns c = command.common();
        Map<String, Object> s = command.specific();
        int i = 1;
        setString(ps, i++, command.key());                 // WAFER_ID
        setString(ps, i++, (String) s.get("LOT_ID"));
        setInteger(ps, i++, (Integer) s.get("SLOT_NO"));
        setString(ps, i++, (String) s.get("GRADE_CODE"));
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
        setString(ps, i++, (String) s.get("LOT_ID"));
        setInteger(ps, i++, (Integer) s.get("SLOT_NO"));
        setString(ps, i++, (String) s.get("GRADE_CODE"));
        setEnum(ps, i++, c.txnCode());
        setString(ps, i++, c.factoryId());
        setString(ps, i++, c.equipmentId());
        setString(ps, i++, c.eventId());
        setTimestamp(ps, i++, c.eventTime());
        setString(ps, i++, c.userId());
        setString(ps, i++, c.comment());
        setString(ps, i, command.key());                   // WHERE WAFER_ID = ?
    }
}
