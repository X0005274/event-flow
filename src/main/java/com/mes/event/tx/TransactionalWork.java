package com.mes.event.tx;

import java.sql.Connection;

/**
 * 단일 트랜잭션 안에서 실행할 작업.
 */
@FunctionalInterface
public interface TransactionalWork {

    void execute(Connection conn) throws Exception;
}
