package com.mes.event.dto;

import com.mes.event.type.EventType;
import com.mes.event.type.TxnCode;
import java.time.LocalDateTime;

/**
 * 모든 MES 이벤트의 공통(감사성) 필드를 보유하는 상위 타입.
 *
 * <p>의도적으로 {@code sealed} 를 쓰지 않는다. 새 이벤트 종류를 별도 패키지/모듈에서 추가할 수 있도록
 * 확장 개방성을 우선했다(이 클래스를 수정하지 않고 상속만으로 추가 가능).
 *
 * <p>공통 필드와 이벤트별 필드를 명확히 분리하기 위해, 이벤트별 필드는 각 하위 클래스에만 존재한다.
 * (모든 컬럼을 한 DTO 에 몰아넣는 "God DTO" 안티패턴을 피한다.)
 */
public abstract class MesEvent {

    // ===== 공통 감사 필드 (모든 테이블에 유사하게 반영) =====
    private final String eventId;          // 이벤트 고유 ID (HIS PK 후보)
    private final LocalDateTime eventTime; // 이벤트 발생 시각
    private final TxnCode txnCode;         // 거래 코드(enum)
    private final String userId;           // 처리 사용자
    private final String comment;          // 비고 (nullable)
    private final String factoryId;        // 공장
    private final String equipmentId;      // 설비 (nullable)

    protected MesEvent(String eventId,
                       LocalDateTime eventTime,
                       TxnCode txnCode,
                       String userId,
                       String comment,
                       String factoryId,
                       String equipmentId) {
        this.eventId = eventId;
        this.eventTime = eventTime;
        this.txnCode = txnCode;
        this.userId = userId;
        this.comment = comment;
        this.factoryId = factoryId;
        this.equipmentId = equipmentId;
    }

    /** 이 이벤트의 타입. Factory 가 Processor 선택에 사용한다. */
    public abstract EventType eventType();

    public String getEventId() {
        return eventId;
    }

    public LocalDateTime getEventTime() {
        return eventTime;
    }

    public TxnCode getTxnCode() {
        return txnCode;
    }

    public String getUserId() {
        return userId;
    }

    public String getComment() {
        return comment;
    }

    public String getFactoryId() {
        return factoryId;
    }

    public String getEquipmentId() {
        return equipmentId;
    }
}
