package com.rbkmoney.reporter.config;

import com.rbkmoney.testcontainers.annotations.DefaultSpringBootTest;
import com.rbkmoney.testcontainers.annotations.ceph.CephTestcontainerSingleton;
import com.rbkmoney.testcontainers.annotations.postgresql.PostgresqlTestcontainer;
import org.junit.jupiter.api.Disabled;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@PostgresqlTestcontainer
@CephTestcontainerSingleton
@DefaultSpringBootTest
@Disabled("disabled tests with ceph usage, if needed check it locally")
public @interface CephPostgresqlSpringBootITest {
}
