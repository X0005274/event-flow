package com.mes.event.dao.wf;

import com.mes.event.command.CommonColumns;
import com.mes.event.command.HisInsertCommand;
import com.mes.event.dao.AbstractHisDao;
import com.mes.event.sql.WaferSql;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Map;

/**
 * MES_WF_HIS insert DAO.
 */
public final class WaferHisDao extends AbstractHisDao {

    @Override
    protected String insertSql() {
        return WaferSql.INSERT_HIS;
    }

    @Override
    protected void bindInsert(PreparedStatement ps, HisInsertCommand command) throws SQLException {
        CommonColumns c = command.common();
        Map<String, Object> s = command.specific();
        int i = 1;
        setString(ps, i++, c.eventId());                    // EVENT_ID
        setString(ps, i++, command.key());                  // WAFER_ID
        setString(ps, i++, (String) s.get("LOT_ID"));
        setInteger(ps, i++, (Integer) s.get("SLOT_NO"));
        setString(ps, i++, (String) s.get("GRADE_CODE"));
        setEnum(ps, i++, c.txnCode());
        setString(ps, i++, c.factoryId());
        setString(ps, i++, c.equipmentId());
        setTimestamp(ps, i++, c.eventTime());
        setString(ps, i++, c.userId());
        setString(ps, i, c.comment());
    }
}
