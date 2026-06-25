package com.mes.event.dto;

import com.mes.event.type.EventType;
import com.mes.event.type.TxnCode;
import java.time.LocalDateTime;

/**
 * Wafer 이벤트. MES_WF_MAS / MES_WF_HIS 중심으로 반영된다.
 */
public final class WaferEvent extends MesEvent {

    // ===== Wafer 특화 필드 =====
    private final String waferId;     // 마스터 키
    private final String lotId;       // 소속 Lot
    private final Integer slotNo;     // nullable
    private final String gradeCode;   // nullable

    public WaferEvent(String eventId,
                      LocalDateTime eventTime,
                      TxnCode txnCode,
                      String userId,
                      String comment,
                      String factoryId,
                      String equipmentId,
                      String waferId,
                      String lotId,
                      Integer slotNo,
                      String gradeCode) {
        super(eventId, eventTime, txnCode, userId, comment, factoryId, equipmentId);
        this.waferId = waferId;
        this.lotId = lotId;
        this.slotNo = slotNo;
        this.gradeCode = gradeCode;
    }

    @Override
    public EventType eventType() {
        return EventType.WAFER;
    }

    public String getWaferId() {
        return waferId;
    }

    public String getLotId() {
        return lotId;
    }

    public Integer getSlotNo() {
        return slotNo;
    }

    public String getGradeCode() {
        return gradeCode;
    }
}
