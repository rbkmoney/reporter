package com.rbkmoney.reporter.config;

import com.rbkmoney.reporter.config.testconfiguration.MockedUnimportantServicesConfig;
import com.rbkmoney.testcontainers.annotations.DefaultSpringBootTest;
import com.rbkmoney.testcontainers.annotations.postgresql.PostgresqlTestcontainer;
import org.springframework.context.annotation.Import;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@PostgresqlTestcontainer
@DefaultSpringBootTest
@Import(MockedUnimportantServicesConfig.class)
public @interface PostgresqlSpringBootITest {
}
