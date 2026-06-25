package com.mes.event.sql;

/**
 * Lot 도메인 SQL 상수.
 *
 * <p>// TODO[MES] 실제 테이블/컬럼명에 맞춰 보정할 것. 감사 시각 컬럼은 DB 비종속을 위해
 * ANSI 표준 {@code CURRENT_TIMESTAMP} 를 사용한다. (Oracle 은 {@code SYSTIMESTAMP} 로 바꿔도 무방)
 */
public final class LotSql {

    private LotSql() {
    }

    public static final String EXISTS_MAS =
            "SELECT 1 FROM MES_LOT_MAS WHERE LOT_ID = ?";

    public static final String INSERT_MAS = """
            INSERT INTO MES_LOT_MAS
                (LOT_ID, PRODUCT_ID, STEP_ID, WAFER_QTY,
                 LAST_TXN_CODE, FACTORY_ID, EQUIPMENT_ID,
                 LAST_EVENT_ID, LAST_EVENT_TIME, UPD_USER, REMARK, UPD_DT)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)
            """;

    public static final String UPDATE_MAS = """
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

    public static final String INSERT_HIS = """
            INSERT INTO MES_LOT_HIS
                (EVENT_ID, LOT_ID, PRODUCT_ID, STEP_ID, WAFER_QTY,
                 TXN_CODE, FACTORY_ID, EQUIPMENT_ID,
                 EVENT_TIME, USER_ID, REMARK, CRT_DT)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)
            """;
}
