package com.mes.event.processor;

import com.mes.event.dto.WaferEvent;
import com.mes.event.support.Jdbc;
import java.sql.Connection;

/**
 * Wafer(웨이퍼: 반도체 원판) 이벤트 처리기.
 *
 * <ul>
 *   <li>마스터 테이블: {@code MES_WF_MAS} (Wafer 1건당 최신 상태 한 줄)</li>
 *   <li>이력 테이블:   {@code MES_WF_HIS} (이벤트가 올 때마다 한 줄 누적)</li>
 *   <li>마스터 키:     {@code WAFER_ID}</li>
 * </ul>
 *
 * <p>구조와 읽는 법은 {@link LotEventProcessor} 와 동일하다(테이블/컬럼만 다름).
 * // TODO[MES] 실제 테이블/컬럼명에 맞춰 SQL 을 보정할 것.
 */
public final class WaferEventProcessor extends AbstractEventProcessor<WaferEvent> {

    private static final String EXISTS_MAS =
            "SELECT 1 FROM MES_WF_MAS WHERE WAFER_ID = ?";

    private static final String INSERT_MAS = """
            INSERT INTO MES_WF_MAS
                (WAFER_ID, LOT_ID, SLOT_NO, GRADE_CODE,
                 LAST_TXN_CODE, FACTORY_ID, EQUIPMENT_ID,
                 LAST_EVENT_ID, LAST_EVENT_TIME, UPD_USER, REMARK, UPD_DT)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)
            """;

    private static final String UPDATE_MAS = """
            UPDATE MES_WF_MAS
               SET LOT_ID          = ?,
                   SLOT_NO         = ?,
                   GRADE_CODE      = ?,
                   LAST_TXN_CODE   = ?,
                   FACTORY_ID      = ?,
                   EQUIPMENT_ID    = ?,
                   LAST_EVENT_ID   = ?,
                   LAST_EVENT_TIME = ?,
                   UPD_USER        = ?,
                   REMARK          = ?,
                   UPD_DT          = CURRENT_TIMESTAMP
             WHERE WAFER_ID = ?
            """;

    private static final String INSERT_HIS = """
            INSERT INTO MES_WF_HIS
                (EVENT_ID, WAFER_ID, LOT_ID, SLOT_NO, GRADE_CODE,
                 TXN_CODE, FACTORY_ID, EQUIPMENT_ID,
                 EVENT_TIME, USER_ID, REMARK, CRT_DT)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)
            """;

    @Override
    protected String masterKey(WaferEvent event) {
        return event.getWaferId();
    }

    @Override
    protected int upsertMaster(Connection conn, WaferEvent e) {
        if (Jdbc.exists(conn, EXISTS_MAS, e.getWaferId())) {
            return Jdbc.update(conn, UPDATE_MAS, ps -> {
                int i = 1;
                Jdbc.setString(ps, i++, e.getLotId());
                Jdbc.setInteger(ps, i++, e.getSlotNo());
                Jdbc.setString(ps, i++, e.getGradeCode());
                Jdbc.setEnum(ps, i++, e.getTxnCode());
                Jdbc.setString(ps, i++, e.getFactoryId());
                Jdbc.setString(ps, i++, e.getEquipmentId());
                Jdbc.setString(ps, i++, e.getEventId());
                Jdbc.setTimestamp(ps, i++, e.getEventTime());
                Jdbc.setString(ps, i++, e.getUserId());
                Jdbc.setString(ps, i++, e.getComment());
                Jdbc.setString(ps, i, e.getWaferId()); // 맨 끝 WHERE WAFER_ID = ?
            });
        }
        return Jdbc.update(conn, INSERT_MAS, ps -> {
            int i = 1;
            Jdbc.setString(ps, i++, e.getWaferId());
            Jdbc.setString(ps, i++, e.getLotId());
            Jdbc.setInteger(ps, i++, e.getSlotNo());
            Jdbc.setString(ps, i++, e.getGradeCode());
            Jdbc.setEnum(ps, i++, e.getTxnCode());
            Jdbc.setString(ps, i++, e.getFactoryId());
            Jdbc.setString(ps, i++, e.getEquipmentId());
            Jdbc.setString(ps, i++, e.getEventId());
            Jdbc.setTimestamp(ps, i++, e.getEventTime());
            Jdbc.setString(ps, i++, e.getUserId());
            Jdbc.setString(ps, i, e.getComment());
        });
    }

    @Override
    protected int insertHistory(Connection conn, WaferEvent e) {
        return Jdbc.update(conn, INSERT_HIS, ps -> {
            int i = 1;
            Jdbc.setString(ps, i++, e.getEventId());
            Jdbc.setString(ps, i++, e.getWaferId());
            Jdbc.setString(ps, i++, e.getLotId());
            Jdbc.setInteger(ps, i++, e.getSlotNo());
            Jdbc.setString(ps, i++, e.getGradeCode());
            Jdbc.setEnum(ps, i++, e.getTxnCode());
            Jdbc.setString(ps, i++, e.getFactoryId());
            Jdbc.setString(ps, i++, e.getEquipmentId());
            Jdbc.setTimestamp(ps, i++, e.getEventTime());
            Jdbc.setString(ps, i++, e.getUserId());
            Jdbc.setString(ps, i, e.getComment());
        });
    }
}
