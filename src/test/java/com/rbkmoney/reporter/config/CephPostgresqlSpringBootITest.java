package com.rbkmoney.reporter.config;

import com.rbkmoney.testcontainers.annotations.DefaultSpringBootTest;
import com.rbkmoney.testcontainers.annotations.ceph.CephTestcontainer;
import com.rbkmoney.testcontainers.annotations.postgresql.PostgresqlTestcontainer;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@PostgresqlTestcontainer
@CephTestcontainer
@DefaultSpringBootTest
public @interface CephPostgresqlSpringBootITest {
}
