package com.mes.event.exception;

/**
 * 이벤트 처리(검증/디스패치/트랜잭션) 단계에서 발생한 도메인 예외.
 *
 * <p>저수준 {@link java.sql.SQLException} 등은 {@link DbAccessException} 으로 1차 래핑되고,
 * 비즈니스/오케스트레이션 레벨 오류는 이 예외로 표현한다.
 */
public class EventProcessingException extends RuntimeException {

    public EventProcessingException(String message) {
        super(message);
    }

    public EventProcessingException(String message, Throwable cause) {
        super(message, cause);
    }
}
