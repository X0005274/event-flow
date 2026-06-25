package com.mes.event.dto;

import com.mes.event.type.EventType;
import com.mes.event.type.TxnCode;
import java.time.LocalDateTime;

/**
 * Lot 이벤트. MES_LOT_MAS / MES_LOT_HIS 중심으로 반영된다.
 */
public final class LotEvent extends MesEvent {

    // ===== Lot 특화 필드 =====
    private final String lotId;       // 마스터 키
    private final String productId;
    private final String stepId;
    private final Integer waferQty;   // nullable

    public LotEvent(String eventId,
                    LocalDateTime eventTime,
                    TxnCode txnCode,
                    String userId,
                    String comment,
                    String factoryId,
                    String equipmentId,
                    String lotId,
                    String productId,
                    String stepId,
                    Integer waferQty) {
        super(eventId, eventTime, txnCode, userId, comment, factoryId, equipmentId);
        this.lotId = lotId;
        this.productId = productId;
        this.stepId = stepId;
        this.waferQty = waferQty;
    }

    @Override
    public EventType eventType() {
        return EventType.LOT;
    }

    public String getLotId() {
        return lotId;
    }

    public String getProductId() {
        return productId;
    }

    public String getStepId() {
        return stepId;
    }

    public Integer getWaferQty() {
        return waferQty;
    }
}
