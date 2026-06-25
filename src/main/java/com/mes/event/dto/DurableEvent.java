package com.mes.event.dto;

import com.mes.event.type.EventType;
import com.mes.event.type.TxnCode;
import java.time.LocalDateTime;

/**
 * Durable(내구재: 지그/마스크/캐리어 등) 이벤트.
 * MES_DURABLE_MAS / MES_DURABLE_HIS 중심으로 반영된다.
 */
public final class DurableEvent extends MesEvent {

    // ===== Durable 특화 필드 =====
    private final String durableId;     // 마스터 키
    private final String durableType;
    private final Integer usageCount;   // nullable - 누적 사용 횟수

    public DurableEvent(String eventId,
                        LocalDateTime eventTime,
                        TxnCode txnCode,
                        String userId,
                        String comment,
                        String factoryId,
                        String equipmentId,
                        String durableId,
                        String durableType,
                        Integer usageCount) {
        super(eventId, eventTime, txnCode, userId, comment, factoryId, equipmentId);
        this.durableId = durableId;
        this.durableType = durableType;
        this.usageCount = usageCount;
    }

    @Override
    public EventType eventType() {
        return EventType.DURABLE;
    }

    public String getDurableId() {
        return durableId;
    }

    public String getDurableType() {
        return durableType;
    }

    public Integer getUsageCount() {
        return usageCount;
    }
}
