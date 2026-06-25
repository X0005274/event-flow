package com.mes.event;

import com.mes.event.dto.MesEvent;
import com.mes.event.processor.EventProcessor;
import com.mes.event.processor.EventProcessorFactory;
import com.mes.event.support.EventLog;
import com.mes.event.tx.TransactionManager;
import java.sql.Connection;

/**
 * 이벤트 처리 진입점.
 *
 * <p>두 가지 사용 모드를 제공한다:
 * <ul>
 *   <li><b>{@link #dispatch}</b> - 이 모듈이 트랜잭션을 직접 관리(내장 {@link TransactionManager} 사용).
 *       독립 실행/단순 연동에 적합.</li>
 *   <li><b>{@link #dispatchWithin}</b> - 기존 MES 가 이미 연 커넥션/트랜잭션에 합류.
 *       commit/rollback 은 기존 시스템이 수행. 한 트랜잭션에 여러 작업을 묶을 때 사용.</li>
 * </ul>
 */
public final class EventDispatcher {

    private static final EventLog log = EventLog.forClass(EventDispatcher.class);

    private final TransactionManager transactionManager; // nullable (dispatchWithin 전용 사용 시)
    private final EventProcessorFactory factory;

    /** 트랜잭션 관리까지 위임하는 구성. */
    public EventDispatcher(TransactionManager transactionManager, EventProcessorFactory factory) {
        this.transactionManager = transactionManager;
        this.factory = factory;
    }

    /** 기존 트랜잭션에 합류하는 용도로만 쓸 때(트랜잭션 매니저 불필요). */
    public EventDispatcher(EventProcessorFactory factory) {
        this(null, factory);
    }

    /**
     * 새 트랜잭션을 열어 이벤트를 처리한다(commit/rollback 포함).
     */
    public <E extends MesEvent> void dispatch(E event) {
        if (transactionManager == null) {
            throw new IllegalStateException(
                    "TransactionManager not configured. Use dispatchWithin(conn, event) "
                            + "to join an existing transaction.");
        }
        transactionManager.executeInTransaction(conn -> dispatchWithin(conn, event));
    }

    /**
     * 외부에서 주입된 커넥션(=기존 트랜잭션)으로 이벤트를 처리한다. commit/rollback 은 호출자 책임.
     */
    public <E extends MesEvent> void dispatchWithin(Connection conn, E event) {
        EventProcessor<E> processor = factory.resolve(event.eventType());
        long startedAt = System.nanoTime();
        try {
            processor.process(conn, event);
        } finally {
            long elapsedMs = (System.nanoTime() - startedAt) / 1_000_000;
            log.debug(() -> "dispatched type=%s eventId=%s elapsedMs=%d"
                    .formatted(event.eventType(), event.getEventId(), elapsedMs));
        }
    }
}
