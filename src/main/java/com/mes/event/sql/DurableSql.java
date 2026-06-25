package com.mes.event.sql;

/**
 * Durable 도메인 SQL 상수. // TODO[MES] 실제 테이블/컬럼명에 맞춰 보정할 것.
 */
public final class DurableSql {

    private DurableSql() {
    }

    public static final String EXISTS_MAS =
            "SELECT 1 FROM MES_DURABLE_MAS WHERE DURABLE_ID = ?";

    public static final String INSERT_MAS = """
            INSERT INTO MES_DURABLE_MAS
                (DURABLE_ID, DURABLE_TYPE, USAGE_COUNT,
                 LAST_TXN_CODE, FACTORY_ID, EQUIPMENT_ID,
                 LAST_EVENT_ID, LAST_EVENT_TIME, UPD_USER, REMARK, UPD_DT)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)
            """;

    public static final String UPDATE_MAS = """
            UPDATE MES_DURABLE_MAS
               SET DURABLE_TYPE    = ?,
                   USAGE_COUNT     = ?,
                   LAST_TXN_CODE   = ?,
                   FACTORY_ID      = ?,
                   EQUIPMENT_ID    = ?,
                   LAST_EVENT_ID   = ?,
                   LAST_EVENT_TIME = ?,
                   UPD_USER        = ?,
                   REMARK          = ?,
                   UPD_DT          = CURRENT_TIMESTAMP
             WHERE DURABLE_ID = ?
            """;

    public static final String INSERT_HIS = """
            INSERT INTO MES_DURABLE_HIS
                (EVENT_ID, DURABLE_ID, DURABLE_TYPE, USAGE_COUNT,
                 TXN_CODE, FACTORY_ID, EQUIPMENT_ID,
                 EVENT_TIME, USER_ID, REMARK, CRT_DT)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)
            """;
}
