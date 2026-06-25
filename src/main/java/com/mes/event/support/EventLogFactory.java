package com.mes.event.support;

import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * {@link EventLog} 인스턴스 생성을 담당하는 팩토리.
 *
 * <p>기본 구현은 {@code java.util.logging} 기반이다. 사내 로깅으로 바꾸려면 기동 코드에서:
 * <pre>{@code
 * EventLogFactory.setProvider(owner -> new MyLog4jEventLog(owner));
 * }</pre>
 * 한 줄만 설정하면 모든 클래스의 로그가 사내 표준으로 흐른다.
 */
public final class EventLogFactory {

    private static volatile Function<Class<?>, EventLog> provider = JulEventLog::new;

    private EventLogFactory() {
    }

    public static void setProvider(Function<Class<?>, EventLog> newProvider) {
        if (newProvider == null) {
            throw new IllegalArgumentException("provider must not be null");
        }
        provider = newProvider;
    }

    public static EventLog get(Class<?> owner) {
        return provider.apply(owner);
    }

    /** 기본 JUL 어댑터. */
    private static final class JulEventLog implements EventLog {

        private final Logger logger;

        private JulEventLog(Class<?> owner) {
            this.logger = Logger.getLogger(owner.getName());
        }

        @Override
        public boolean isDebugEnabled() {
            return logger.isLoggable(Level.FINE);
        }

        @Override
        public void debug(String message) {
            logger.fine(message);
        }

        @Override
        public void info(String message) {
            logger.info(message);
        }

        @Override
        public void warn(String message) {
            logger.warning(message);
        }

        @Override
        public void warn(String message, Throwable cause) {
            logger.log(Level.WARNING, message, cause);
        }

        @Override
        public void error(String message, Throwable cause) {
            logger.log(Level.SEVERE, message, cause);
        }
    }
}
