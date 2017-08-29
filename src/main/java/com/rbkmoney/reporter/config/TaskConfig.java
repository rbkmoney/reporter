package com.rbkmoney.reporter.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

/**
 * Created by tolkonepiu on 17/07/2017.
 */
@Configuration
public class TaskConfig {

    @Bean
    @DependsOn("dataSource")
    public TaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler taskScheduler = new ThreadPoolTaskScheduler();
        taskScheduler.setDaemon(true);
        return taskScheduler;
    }

}
