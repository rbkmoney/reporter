package com.rbkmoney.reporter.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

@Configuration
@ConfigurationProperties(prefix = "bustermaze.polling")
@Data
public class BustermazeProperties {

    private Resource url;
    private int delay;
    private int maxPoolSize;
    private int housekeeperTimeout;
    private boolean enable;

}
