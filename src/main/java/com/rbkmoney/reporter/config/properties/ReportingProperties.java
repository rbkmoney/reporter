package com.rbkmoney.reporter.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.time.ZoneId;

@Configuration
@ConfigurationProperties(prefix = "reporting")
@Data
public class ReportingProperties {

    private int reportsLimit;
    private ZoneId defaultTimeZone;

}