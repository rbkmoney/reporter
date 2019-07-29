package com.rbkmoney.reporter.config;

import com.rbkmoney.reporter.factory.AutowiringSpringBeanJobFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by tolkonepiu on 17/07/2017.
 */
@Configuration
public class SchedulerConfig {

    @Value("${info.scheduler-pool-size}")
    private int schedulerPoolSize;

    @Bean
    @DependsOn("dataSource")
    public TaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler taskScheduler = new ThreadPoolTaskScheduler();
        taskScheduler.setDaemon(true);
        taskScheduler.setPoolSize(schedulerPoolSize);
        ThreadGroup threadGroup = new ThreadGroup("Schedulers");
        taskScheduler.setThreadFactory(
                new ThreadFactory() {
                    AtomicInteger counter = new AtomicInteger();

                    @Override
                    public Thread newThread(Runnable r) {
                        Thread thread = new Thread(threadGroup, r, "Scheduler-" + counter.incrementAndGet());
                        thread.setDaemon(true);
                        return thread;
                    }
                }
        );
        return taskScheduler;
    }

    @Bean
    public SchedulerFactoryBean schedulerFactory(ApplicationContext applicationContext) {
        SchedulerFactoryBean factoryBean = new SchedulerFactoryBean();
        AutowiringSpringBeanJobFactory jobFactory = new AutowiringSpringBeanJobFactory();
        jobFactory.setApplicationContext(applicationContext);

        factoryBean.setJobFactory(jobFactory);
        factoryBean.setApplicationContextSchedulerContextKey("applicationContext");
        return factoryBean;
    }

}
