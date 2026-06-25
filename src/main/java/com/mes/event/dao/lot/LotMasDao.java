package com.mes.event.dao.lot;

import com.mes.event.command.CommonColumns;
import com.mes.event.command.MasUpsertCommand;
import com.mes.event.dao.AbstractMasDao;
import com.mes.event.sql.LotSql;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Map;

/**
 * MES_LOT_MAS upsert DAO.
 */
public final class LotMasDao extends AbstractMasDao {

    @Override
    protected String existsSql() {
        return LotSql.EXISTS_MAS;
    }

    @Override
    protected String insertSql() {
        return LotSql.INSERT_MAS;
    }

    @Override
    protected String updateSql() {
        return LotSql.UPDATE_MAS;
    }

    @Override
    protected void bindInsert(PreparedStatement ps, MasUpsertCommand command) throws SQLException {
        CommonColumns c = command.common();
        Map<String, Object> s = command.specific();
        int i = 1;
        setString(ps, i++, command.key());                 // LOT_ID
        setString(ps, i++, (String) s.get("PRODUCT_ID"));
        setString(ps, i++, (String) s.get("STEP_ID"));
        setInteger(ps, i++, (Integer) s.get("WAFER_QTY"));
        setEnum(ps, i++, c.txnCode());                      // LAST_TXN_CODE
        setString(ps, i++, c.factoryId());
        setString(ps, i++, c.equipmentId());
        setString(ps, i++, c.eventId());                    // LAST_EVENT_ID
        setTimestamp(ps, i++, c.eventTime());               // LAST_EVENT_TIME
        setString(ps, i++, c.userId());                     // UPD_USER
        setString(ps, i, c.comment());                      // REMARK
    }

    @Override
    protected void bindUpdate(PreparedStatement ps, MasUpsertCommand command) throws SQLException {
        CommonColumns c = command.common();
        Map<String, Object> s = command.specific();
        int i = 1;
        setString(ps, i++, (String) s.get("PRODUCT_ID"));
        setString(ps, i++, (String) s.get("STEP_ID"));
        setInteger(ps, i++, (Integer) s.get("WAFER_QTY"));
        setEnum(ps, i++, c.txnCode());
        setString(ps, i++, c.factoryId());
        setString(ps, i++, c.equipmentId());
        setString(ps, i++, c.eventId());
        setTimestamp(ps, i++, c.eventTime());
        setString(ps, i++, c.userId());
        setString(ps, i++, c.comment());
        setString(ps, i, command.key());                    // WHERE LOT_ID = ?
    }
}
