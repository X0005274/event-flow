package com.mes.event.dao;

import com.mes.event.command.HisInsertCommand;
import java.sql.Connection;

/**
 * 히스토리(HIS) 테이블 DAO 계약. 항상 insert(append-only).
 */
public interface HisDao {

    /**
     * @param conn 외부 주입 커넥션(트랜잭션 경계는 호출자 소유)
     * @return 영향받은 행 수
     */
    int insert(Connection conn, HisInsertCommand command);
}
