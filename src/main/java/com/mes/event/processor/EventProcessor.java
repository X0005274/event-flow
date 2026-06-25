package com.mes.event.processor;

import com.mes.event.dto.MesEvent;
import java.sql.Connection;

/**
 * 이벤트 처리 계약.
 *
 * <p>커넥션은 외부에서 주입되며, 트랜잭션 경계는 호출자(TransactionManager 또는 기존 MES 트랜잭션)가 소유한다.
 * Processor 는 트랜잭션을 알지 못한다(테스트 용이 + 기존 트랜잭션 합류 가능).
 *
 * @param <E> 처리 대상 이벤트 타입
 */
public interface EventProcessor<E extends MesEvent> {

    void process(Connection conn, E event);
}
