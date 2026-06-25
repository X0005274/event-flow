package com.mes.event.type;

/**
 * MES 이벤트의 대분류.
 *
 * <p>새 이벤트 종류를 추가할 때 이 enum 에 값을 1개 추가하고,
 * {@code EventProcessorFactory} 에 해당 Processor 를 등록하면 된다.
 */
public enum EventType {
    LOT,
    WAFER,
    DURABLE
}
