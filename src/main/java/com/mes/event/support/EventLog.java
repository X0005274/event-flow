package com.mes.event.support;

/**
 * 로깅 이음새(seam).
 *
 * <p>이 모듈은 특정 로깅 프레임워크에 의존하지 않는다. 기본 구현은 {@code java.util.logging}
 * 이지만, 사내 표준(Log4j2 / SLF4J / Logback)으로 교체하려면
 * {@link EventLogFactory#setProvider(java.util.function.Function)} 를 애플리케이션 기동 시 1회 호출하면 된다.
 *
 * <p>메시지는 람다(Supplier) 형태로도 받을 수 있어, 로그 레벨이 꺼져 있으면 문자열 조립 비용이 발생하지 않는다.
 */
public interface EventLog {

    boolean isDebugEnabled();

    void debug(String message);

    default void debug(java.util.function.Supplier<String> message) {
        if (isDebugEnabled()) {
            debug(message.get());
        }
    }

    void info(String message);

    default void info(java.util.function.Supplier<String> message) {
        info(message.get());
    }

    void warn(String message);

    void warn(String message, Throwable cause);

    void error(String message, Throwable cause);

    static EventLog forClass(Class<?> owner) {
        return EventLogFactory.get(owner);
    }
}
