package com.mes.event.sql;

/**
 * Wafer 도메인 SQL 상수. // TODO[MES] 실제 테이블/컬럼명에 맞춰 보정할 것.
 */
public final class WaferSql {

    private WaferSql() {
    }

    public static final String EXISTS_MAS =
            "SELECT 1 FROM MES_WF_MAS WHERE WAFER_ID = ?";

    public static final String INSERT_MAS = """
            INSERT INTO MES_WF_MAS
                (WAFER_ID, LOT_ID, SLOT_NO, GRADE_CODE,
                 LAST_TXN_CODE, FACTORY_ID, EQUIPMENT_ID,
                 LAST_EVENT_ID, LAST_EVENT_TIME, UPD_USER, REMARK, UPD_DT)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)
            """;

    public static final String UPDATE_MAS = """
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

    public static final String INSERT_HIS = """
            INSERT INTO MES_WF_HIS
                (EVENT_ID, WAFER_ID, LOT_ID, SLOT_NO, GRADE_CODE,
                 TXN_CODE, FACTORY_ID, EQUIPMENT_ID,
                 EVENT_TIME, USER_ID, REMARK, CRT_DT)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)
            """;
}
