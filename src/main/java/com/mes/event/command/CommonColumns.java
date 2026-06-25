package com.mes.event.command;

import com.mes.event.type.TxnCode;
import java.time.LocalDateTime;

/**
 * 모든 테이블(MAS/HIS)에 공통으로 반영되는 감사 컬럼 묶음.
 *
 * <p>{@code AbstractEventProcessor} 가 이벤트의 공통 필드로부터 단 한 번 생성하며,
 * 이후 MAS/HIS DAO 가 동일 인스턴스를 공유한다(공통값 세팅 일원화).
 */
public record CommonColumns(
        String eventId,
        LocalDateTime eventTime,
        TxnCode txnCode,
        String userId,
        String comment,
        String factoryId,
        String equipmentId
) {
}
