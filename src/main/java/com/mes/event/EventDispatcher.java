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
 *       commit/rollback 은 기존 시스템이 수행.</li>
 * </ul>
 *
 * <p>두 메서드 모두 이벤트를 가변인자로 받는다. 1건이면 단일 처리, 여러 건이면 입력 순서대로
 * <b>한 트랜잭션 안에서</b> 복합 처리된다(하나라도 실패하면 전체 롤백). 이벤트마다 타입에 맞는
 * 프로세서를 {@link EventProcessorFactory} 가 찾아 실행하므로, 서로 다른 종류를 섞어 묶어도 된다.
 *
 * <pre>{@code
 * dispatcher.dispatch(lotEvent);                          // 단일
 * dispatcher.dispatch(lotEvent, waferEvent, durableEvent); // 복합(한 트랜잭션)
 * }</pre>
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
     * 새 트랜잭션을 열어 이벤트(1건 이상)를 입력 순서대로 처리한다(commit/rollback 포함).
     * 하나라도 실패하면 전체 롤백된다.
     */
    public void dispatch(MesEvent... events) {
        if (transactionManager == null) {
            throw new IllegalStateException(
                    "TransactionManager not configured. Use dispatchWithin(conn, events) "
                            + "to join an existing transaction.");
        }
        transactionManager.executeInTransaction(conn -> dispatchWithin(conn, events));
    }

    /**
     * 외부에서 주입된 커넥션(=기존 트랜잭션)으로 이벤트(1건 이상)를 입력 순서대로 처리한다.
     * commit/rollback 은 호출자 책임이며, 묶인 이벤트는 같은 트랜잭션에서 처리된다.
     */
    public void dispatchWithin(Connection conn, MesEvent... events) {
        for (MesEvent event : events) {
            dispatchOne(conn, event);
        }
    }

    private void dispatchOne(Connection conn, MesEvent event) {
        EventProcessor<MesEvent> processor = factory.resolve(event.eventType());
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
