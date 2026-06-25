package com.mes.event.processor;

import com.mes.event.dao.durable.DurableHisDao;
import com.mes.event.dao.durable.DurableMasDao;
import com.mes.event.dao.lot.LotHisDao;
import com.mes.event.dao.lot.LotMasDao;
import com.mes.event.dao.wf.WaferHisDao;
import com.mes.event.dao.wf.WaferMasDao;
import com.mes.event.dto.MesEvent;
import com.mes.event.exception.EventProcessingException;
import com.mes.event.type.EventType;
import java.util.EnumMap;
import java.util.Map;

/**
 * {@link EventType} → {@link EventProcessor} 매핑(Factory).
 *
 * <p>여기가 컴포지션 루트(조립 지점)다. Spring 같은 DI 컨테이너가 없으므로 DAO 와 Processor 의 와이어링을
 * 이 클래스에서 수행한다.
 *
 * <p>DAO 는 무상태(stateless)이므로 Processor 인스턴스를 재사용해도 스레드 안전하다.
 * (커넥션은 매 호출 시 외부 주입되며 인스턴스 상태로 보관하지 않는다.)
 *
 * <p>테스트나 특수 케이스를 위해 {@link #register} 로 Processor 를 교체/추가할 수 있다.
 */
public final class EventProcessorFactory {

    private final Map<EventType, EventProcessor<? extends MesEvent>> registry =
            new EnumMap<>(EventType.class);

    /** 기본 구성: 표준 DAO 로 3종 Processor 를 등록한다. */
    public EventProcessorFactory() {
        register(EventType.LOT, new LotEventProcessor(new LotMasDao(), new LotHisDao()));
        register(EventType.WAFER, new WaferEventProcessor(new WaferMasDao(), new WaferHisDao()));
        register(EventType.DURABLE, new DurableEventProcessor(new DurableMasDao(), new DurableHisDao()));
    }

    public void register(EventType type, EventProcessor<? extends MesEvent> processor) {
        registry.put(type, processor);
    }

    @SuppressWarnings("unchecked")
    public <E extends MesEvent> EventProcessor<E> resolve(EventType type) {
        EventProcessor<? extends MesEvent> processor = registry.get(type);
        if (processor == null) {
            throw new EventProcessingException("No processor registered for eventType=" + type);
        }
        return (EventProcessor<E>) processor;
    }
}
