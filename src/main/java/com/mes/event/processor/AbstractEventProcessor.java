package com.mes.event.processor;

import com.mes.event.command.CommonColumns;
import com.mes.event.command.HisInsertCommand;
import com.mes.event.command.MasUpsertCommand;
import com.mes.event.dao.HisDao;
import com.mes.event.dao.MasDao;
import com.mes.event.dto.MesEvent;
import com.mes.event.exception.EventProcessingException;
import com.mes.event.support.EventLog;
import java.sql.Connection;
import java.util.Map;
import java.util.Objects;

/**
 * 모든 이벤트 처리의 골격(Template Method).
 *
 * <p>고정된 처리 순서:
 * <ol>
 *   <li>검증 ({@link #validate})</li>
 *   <li>공통 컬럼 세팅 (이 클래스에서 1곳)</li>
 *   <li>이벤트별 컬럼 세팅 ({@link #specificColumns} - 하위 구현)</li>
 *   <li>MAS 반영 (insert/update 는 DAO 가 판단)</li>
 *   <li>HIS 반영 (insert)</li>
 * </ol>
 *
 * <p>하위 클래스(Strategy 포인트)는 "어떤 DAO 를 쓰고, 키가 무엇이며, 어떤 특화 컬럼을 채우는지"만 구현한다.
 *
 * @param <E> 이벤트 타입
 */
public abstract class AbstractEventProcessor<E extends MesEvent> implements EventProcessor<E> {

    protected final EventLog log = EventLog.forClass(getClass());

    // ===== Strategy 포인트 (하위 구현) =====
    protected abstract MasDao masDao();

    protected abstract HisDao hisDao();

    /** 마스터 PK 값 추출. */
    protected abstract String masterKey(E event);

    /** 이벤트별 특화 컬럼 (컬럼명 → 값). 순서 안정성을 위해 LinkedHashMap 권장. */
    protected abstract Map<String, Object> specificColumns(E event);

    /** 공통 검증. 도메인별 추가 검증이 필요하면 override 후 super 호출. */
    protected void validate(E event) {
        Objects.requireNonNull(event, "event must not be null");
        Objects.requireNonNull(event.getEventId(), "eventId must not be null");
        Objects.requireNonNull(event.getEventTime(), "eventTime must not be null");
        Objects.requireNonNull(event.getTxnCode(), "txnCode must not be null");
        if (masterKey(event) == null) {
            throw new EventProcessingException(
                    "master key is null. eventType=" + event.eventType()
                            + ", eventId=" + event.getEventId());
        }
    }

    /** 공통 컬럼 세팅 - 단 한 곳. */
    private CommonColumns toCommonColumns(E event) {
        return new CommonColumns(
                event.getEventId(),
                event.getEventTime(),
                event.getTxnCode(),
                event.getUserId(),
                event.getComment(),
                event.getFactoryId(),
                event.getEquipmentId());
    }

    @Override
    public final void process(Connection conn, E event) {
        validate(event);

        final String key = masterKey(event);
        final CommonColumns common = toCommonColumns(event);
        final Map<String, Object> specific = specificColumns(event);

        log.debug(() -> "[%s] processing eventId=%s key=%s txn=%s"
                .formatted(event.eventType(), event.getEventId(), key, event.getTxnCode()));

        // 1) MAS 반영 (존재여부에 따라 insert/update)
        int masRows = masDao().upsert(conn, new MasUpsertCommand(key, common, specific));

        // 2) HIS 반영 (항상 insert)
        int hisRows = hisDao().insert(conn, new HisInsertCommand(key, common, specific));

        log.info(() -> "[%s] done eventId=%s key=%s masRows=%d hisRows=%d"
                .formatted(event.eventType(), event.getEventId(), key, masRows, hisRows));
    }
}
