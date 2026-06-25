package com.mes.event.processor;

import com.mes.event.dto.LotEvent;
import com.mes.event.support.Jdbc;
import java.sql.Connection;

/**
 * Lot(랏: 웨이퍼 묶음) 이벤트 처리기.
 *
 * <p>이 한 파일 안에 "Lot 이벤트가 어느 테이블의 어느 컬럼에 어떻게 들어가는지"가 모두 들어 있다.
 * SQL 은 바로 아래 상수에, 값 채우기는 각 메서드에 있어 위에서 아래로 읽으면 전체 흐름이 보인다.
 *
 * <ul>
 *   <li>마스터 테이블: {@code MES_LOT_MAS} (Lot 1건당 최신 상태 한 줄)</li>
 *   <li>이력 테이블:   {@code MES_LOT_HIS} (이벤트가 올 때마다 한 줄 누적)</li>
 *   <li>마스터 키:     {@code LOT_ID}</li>
 * </ul>
 *
 * <p>// TODO[MES] 실제 테이블/컬럼명에 맞춰 SQL 을 보정할 것.
 * 시각 컬럼은 DB 종류에 덜 의존하도록 표준 {@code CURRENT_TIMESTAMP} 를 사용한다(Oracle 이면 SYSTIMESTAMP 도 가능).
 */
public final class LotEventProcessor extends AbstractEventProcessor<LotEvent> {

    // 같은 LOT_ID 가 이미 있는지 확인하는 SQL (있으면 UPDATE, 없으면 INSERT 하기 위함)
    private static final String EXISTS_MAS =
            "SELECT 1 FROM MES_LOT_MAS WHERE LOT_ID = ?";

    private static final String INSERT_MAS = """
            INSERT INTO MES_LOT_MAS
                (LOT_ID, PRODUCT_ID, STEP_ID, WAFER_QTY,
                 LAST_TXN_CODE, FACTORY_ID, EQUIPMENT_ID,
                 LAST_EVENT_ID, LAST_EVENT_TIME, UPD_USER, REMARK, UPD_DT)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)
            """;

    private static final String UPDATE_MAS = """
            UPDATE MES_LOT_MAS
               SET PRODUCT_ID      = ?,
                   STEP_ID         = ?,
                   WAFER_QTY       = ?,
                   LAST_TXN_CODE   = ?,
                   FACTORY_ID      = ?,
                   EQUIPMENT_ID    = ?,
                   LAST_EVENT_ID   = ?,
                   LAST_EVENT_TIME = ?,
                   UPD_USER        = ?,
                   REMARK          = ?,
                   UPD_DT          = CURRENT_TIMESTAMP
             WHERE LOT_ID = ?
            """;

    private static final String INSERT_HIS = """
            INSERT INTO MES_LOT_HIS
                (EVENT_ID, LOT_ID, PRODUCT_ID, STEP_ID, WAFER_QTY,
                 TXN_CODE, FACTORY_ID, EQUIPMENT_ID,
                 EVENT_TIME, USER_ID, REMARK, CRT_DT)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)
            """;

    @Override
    protected String masterKey(LotEvent event) {
        return event.getLotId();
    }

    @Override
    protected int upsertMaster(Connection conn, LotEvent e) {
        // 1) 같은 LOT_ID 가 이미 있는지 확인
        if (Jdbc.exists(conn, EXISTS_MAS, e.getLotId())) {
            // 2-a) 있으면 UPDATE — 물음표(?) 순서는 위 UPDATE_MAS 의 컬럼 순서와 정확히 같아야 한다.
            return Jdbc.update(conn, UPDATE_MAS, ps -> {
                int i = 1;
                Jdbc.setString(ps, i++, e.getProductId());
                Jdbc.setString(ps, i++, e.getStepId());
                Jdbc.setInteger(ps, i++, e.getWaferQty());
                Jdbc.setEnum(ps, i++, e.getTxnCode());
                Jdbc.setString(ps, i++, e.getFactoryId());
                Jdbc.setString(ps, i++, e.getEquipmentId());
                Jdbc.setString(ps, i++, e.getEventId());
                Jdbc.setTimestamp(ps, i++, e.getEventTime());
                Jdbc.setString(ps, i++, e.getUserId());
                Jdbc.setString(ps, i++, e.getComment());
                Jdbc.setString(ps, i, e.getLotId()); // 맨 끝 WHERE LOT_ID = ?
            });
        }
        // 2-b) 없으면 INSERT — 물음표 순서는 위 INSERT_MAS 의 컬럼 순서와 같다.
        return Jdbc.update(conn, INSERT_MAS, ps -> {
            int i = 1;
            Jdbc.setString(ps, i++, e.getLotId());
            Jdbc.setString(ps, i++, e.getProductId());
            Jdbc.setString(ps, i++, e.getStepId());
            Jdbc.setInteger(ps, i++, e.getWaferQty());
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
    protected int insertHistory(Connection conn, LotEvent e) {
        // 이력은 항상 새 줄 INSERT. 물음표 순서는 위 INSERT_HIS 의 컬럼 순서와 같다.
        return Jdbc.update(conn, INSERT_HIS, ps -> {
            int i = 1;
            Jdbc.setString(ps, i++, e.getEventId());
            Jdbc.setString(ps, i++, e.getLotId());
            Jdbc.setString(ps, i++, e.getProductId());
            Jdbc.setString(ps, i++, e.getStepId());
            Jdbc.setInteger(ps, i++, e.getWaferQty());
            Jdbc.setEnum(ps, i++, e.getTxnCode());
            Jdbc.setString(ps, i++, e.getFactoryId());
            Jdbc.setString(ps, i++, e.getEquipmentId());
            Jdbc.setTimestamp(ps, i++, e.getEventTime());
            Jdbc.setString(ps, i++, e.getUserId());
            Jdbc.setString(ps, i, e.getComment());
        });
    }
}
