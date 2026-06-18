package com.user.driven.operations.app.common.logging;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.core.Appender;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.Iterator;

/**
 * Spring component that wires the DataSource into the {@link DatabaseLogAppender}
 * once the application context is fully initialized.
 * <p>
 * Logback initializes before Spring's ApplicationContext, so the appender cannot
 * receive the DataSource via dependency injection. This initializer bridges the gap
 * by finding the registered DatabaseLogAppender and injecting the DataSource after startup.
 * <p>
 * If no DatabaseLogAppender is configured in logback.xml, this component is a no-op.
 */
@Component
public class DatabaseLogAppenderInitializer {

    private final DataSource dataSource;

    public DatabaseLogAppenderInitializer(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * Finds the DatabaseLogAppender in the Logback context and injects the DataSource.
     * Triggered after the application is fully started to ensure the DataSource and
     * database schema are available.
     */
    @EventListener(ApplicationReadyEvent.class)
    public void initializeAppender() {
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        Logger rootLogger = loggerContext.getLogger(Logger.ROOT_LOGGER_NAME);

        Iterator<Appender<ch.qos.logback.classic.spi.ILoggingEvent>> appenderIterator =
                rootLogger.iteratorForAppenders();

        while (appenderIterator.hasNext()) {
            Appender<ch.qos.logback.classic.spi.ILoggingEvent> appender = appenderIterator.next();
            if (appender instanceof DatabaseLogAppender dbAppender) {
                dbAppender.setDataSource(dataSource);
            }
        }
    }
}
