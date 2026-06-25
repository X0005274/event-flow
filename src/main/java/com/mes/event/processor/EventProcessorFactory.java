package com.mes.event.processor;

import com.mes.event.dto.MesEvent;
import com.mes.event.exception.EventProcessingException;
import com.mes.event.type.EventType;
import java.util.EnumMap;
import java.util.Map;

/**
 * 이벤트 타입({@link EventType}) → 처리기({@link EventProcessor}) 연결표.
 *
 * <p>{@code EventDispatcher} 는 들어온 이벤트의 타입만 보고 "이건 누가 처리하지?" 를 이 표에서 찾는다.
 * Spring 같은 자동 조립(DI) 도구가 없으므로, 여기서 직접 처리기를 만들어 등록한다(조립이 한곳에 모임).
 *
 * <p>처리기는 상태를 갖지 않으므로(커넥션은 매번 외부에서 받음) 인스턴스를 재사용해도 안전하다.
 *
 * <p><b>새 이벤트 종류 추가 방법</b> (예: Reticle):
 * <ol>
 *   <li>{@code EventType} 에 값 추가</li>
 *   <li>{@code ReticleEvent}(입력 데이터) 와 {@code ReticleEventProcessor}(처리 SQL) 작성</li>
 *   <li>아래 생성자에 {@code register(...)} 한 줄 추가</li>
 * </ol>
 */
public final class EventProcessorFactory {

    private final Map<EventType, EventProcessor<? extends MesEvent>> registry =
            new EnumMap<>(EventType.class);

    /** 기본 구성: 기본 제공 처리기 3종을 등록한다. */
    public EventProcessorFactory() {
        register(EventType.LOT, new LotEventProcessor());
        register(EventType.WAFER, new WaferEventProcessor());
        register(EventType.DURABLE, new DurableEventProcessor());
    }

    /** 처리기를 등록/교체한다. (테스트에서 가짜 처리기로 바꿔 끼울 때도 사용) */
    public void register(EventType type, EventProcessor<? extends MesEvent> processor) {
        registry.put(type, processor);
    }

    /** 타입에 맞는 처리기를 찾는다. 없으면 예외. */
    @SuppressWarnings("unchecked")
    public <E extends MesEvent> EventProcessor<E> resolve(EventType type) {
        EventProcessor<? extends MesEvent> processor = registry.get(type);
        if (processor == null) {
            throw new EventProcessingException("등록된 처리기가 없습니다. eventType=" + type);
        }
        return (EventProcessor<E>) processor;
    }
}
