package com.mes.event.exception;

/**
 * JDBC(DB 접근) 단계에서 발생한 예외를 도메인 예외로 래핑한다.
 *
 * <p>{@link java.sql.SQLException} 을 그대로 상위로 던지지 않고
 * 실패한 SQL / 키 등 컨텍스트를 메시지에 담아 추적성을 확보한다.
 */
public class SqlException extends RuntimeException {

    public SqlException(String message, Throwable cause) {
        super(message, cause);
    }
}
