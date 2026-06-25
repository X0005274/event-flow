package com.mes.event.dao;

import com.mes.event.command.MasUpsertCommand;
import java.sql.Connection;

/**
 * 마스터(MAS) 테이블 DAO 계약.
 *
 * <p>구현체는 "존재하면 update, 없으면 insert" 규칙(upsert)을 따른다.
 */
public interface MasDao {

    /**
     * @param conn 외부 주입 커넥션(트랜잭션 경계는 호출자 소유)
     * @return 영향받은 행 수
     */
    int upsert(Connection conn, MasUpsertCommand command);
}
