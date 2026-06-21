package com.user.driven.operations.app.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * Configures async task execution for the generation pipeline.
 * Thread pool: 5 core threads, max 20, queue capacity 50.
 *
 * @author Jatin Raheja
 */
@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean(name = "generationTaskExecutor")
    public Executor generationTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(20);
        executor.setQueueCapacity(50);
        executor.setThreadNamePrefix("gen-");
        executor.initialize();
        return executor;
    }
}
