package com.mes.event.processor;

import com.mes.event.dto.MesEvent;
import com.mes.event.exception.EventProcessingException;
import com.mes.event.support.EventLog;
import java.sql.Connection;
import java.util.Objects;

/**
 * 모든 이벤트 처리의 "공통 뼈대".
 *
 * <p>이벤트 종류(Lot / Wafer / Durable)가 달라도 처리 순서는 항상 똑같다.
 * <ol>
 *   <li><b>검증</b> — 꼭 필요한 값이 들어왔는지 확인 ({@link #validate})</li>
 *   <li><b>마스터(MAS) 반영</b> — 같은 키가 이미 있으면 UPDATE, 없으면 INSERT ({@link #upsertMaster})</li>
 *   <li><b>이력(HIS) 반영</b> — 무슨 일이 있었는지 기록으로 한 줄 INSERT ({@link #insertHistory})</li>
 * </ol>
 * 이 "순서"만 여기서 고정하고, <b>실제 SQL 과 값 채우기는 각 이벤트별 하위 클래스</b>가 담당한다.
 * (이런 방식을 템플릿 메서드 패턴이라 부른다 — 변하지 않는 흐름은 부모가, 달라지는 부분만 자식이 채운다.)
 *
 * <p>용어 정리:
 * <ul>
 *   <li><b>MAS(Master)</b> 테이블 = "현재 최신 상태" 한 줄. 같은 대상이 또 오면 덮어쓴다(UPDATE).</li>
 *   <li><b>HIS(History)</b> 테이블 = "그동안 있었던 일"의 누적 기록. 매번 새 줄을 추가(INSERT)한다.</li>
 *   <li><b>커넥션(Connection)</b> = DB 연결. 트랜잭션 경계(commit/rollback)는 이 클래스가 아니라 호출하는 쪽이 가진다.</li>
 * </ul>
 *
 * @param <E> 이 처리기가 다루는 이벤트 타입 (예: LotEvent)
 */
public abstract class AbstractEventProcessor<E extends MesEvent> implements EventProcessor<E> {

    protected final EventLog log = EventLog.forClass(getClass());

    // ===== 아래 3개는 이벤트 종류마다 다르므로 하위 클래스가 직접 구현한다 =====

    /** 이 이벤트의 마스터 키 값을 꺼낸다. (예: LotEvent 이면 lotId) */
    protected abstract String masterKey(E event);

    /** 마스터(MAS) 테이블에 반영한다: 있으면 UPDATE, 없으면 INSERT. 반환값은 바뀐 행 수. */
    protected abstract int upsertMaster(Connection conn, E event);

    /** 이력(HIS) 테이블에 한 줄 추가(INSERT)한다. 반환값은 추가된 행 수(보통 1). */
    protected abstract int insertHistory(Connection conn, E event);

    /**
     * 공통 검증. 모든 이벤트가 반드시 가져야 하는 값이 비어 있지 않은지 확인한다.
     * 도메인별 추가 검증이 필요하면 하위 클래스에서 override 한 뒤 {@code super.validate(event)} 를 먼저 호출한다.
     */
    protected void validate(E event) {
        Objects.requireNonNull(event, "event 가 null 입니다");
        Objects.requireNonNull(event.getEventId(), "eventId 가 null 입니다");
        Objects.requireNonNull(event.getEventTime(), "eventTime 이 null 입니다");
        Objects.requireNonNull(event.getTxnCode(), "txnCode 가 null 입니다");
        if (masterKey(event) == null) {
            throw new EventProcessingException(
                    "마스터 키가 null 입니다. eventType=" + event.eventType()
                            + ", eventId=" + event.getEventId());
        }
    }

    /**
     * 실제 처리 진입점. 위에서 설명한 순서대로 1) 검증 → 2) MAS → 3) HIS 를 수행한다.
     *
     * <p>{@code final} 이라 하위 클래스가 이 순서를 바꿀 수 없다(흐름의 일관성 보장).
     */
    @Override
    public final void process(Connection conn, E event) {
        validate(event);

        final String key = masterKey(event);
        log.debug(() -> "[%s] 처리 시작 eventId=%s key=%s txn=%s"
                .formatted(event.eventType(), event.getEventId(), key, event.getTxnCode()));

        int masRows = upsertMaster(conn, event); // 1) 최신 상태 반영
        int hisRows = insertHistory(conn, event); // 2) 이력 기록

        log.info(() -> "[%s] 처리 완료 eventId=%s key=%s masRows=%d hisRows=%d"
                .formatted(event.eventType(), event.getEventId(), key, masRows, hisRows));
    }
}
