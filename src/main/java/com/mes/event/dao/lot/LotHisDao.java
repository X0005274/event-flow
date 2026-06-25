package com.mes.event.dao.lot;

import com.mes.event.command.CommonColumns;
import com.mes.event.command.HisInsertCommand;
import com.mes.event.dao.AbstractHisDao;
import com.mes.event.sql.LotSql;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Map;

/**
 * MES_LOT_HIS insert DAO.
 */
public final class LotHisDao extends AbstractHisDao {

    @Override
    protected String insertSql() {
        return LotSql.INSERT_HIS;
    }

    @Override
    protected void bindInsert(PreparedStatement ps, HisInsertCommand command) throws SQLException {
        CommonColumns c = command.common();
        Map<String, Object> s = command.specific();
        int i = 1;
        setString(ps, i++, c.eventId());                    // EVENT_ID
        setString(ps, i++, command.key());                  // LOT_ID
        setString(ps, i++, (String) s.get("PRODUCT_ID"));
        setString(ps, i++, (String) s.get("STEP_ID"));
        setInteger(ps, i++, (Integer) s.get("WAFER_QTY"));
        setEnum(ps, i++, c.txnCode());                      // TXN_CODE
        setString(ps, i++, c.factoryId());
        setString(ps, i++, c.equipmentId());
        setTimestamp(ps, i++, c.eventTime());               // EVENT_TIME
        setString(ps, i++, c.userId());                     // USER_ID
        setString(ps, i, c.comment());                      // REMARK
    }
}
