package com.example.daehyunbackend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
public class AsyncConfig {

    @Bean(name = "tribunalAiTaskExecutor")
    public Executor tribunalAiTaskExecutor(
            @Value("${tribunal.ai.executor.core-pool-size:1}") int corePoolSize,
            @Value("${tribunal.ai.executor.max-pool-size:2}") int maxPoolSize,
            @Value("${tribunal.ai.executor.queue-capacity:100}") int queueCapacity
    ) {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(corePoolSize);
        executor.setMaxPoolSize(maxPoolSize);
        executor.setQueueCapacity(queueCapacity);
        executor.setThreadNamePrefix("tribunal-ai-");
        executor.initialize();
        return executor;
    }
}
