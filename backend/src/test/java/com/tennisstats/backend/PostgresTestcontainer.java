package com.tennisstats.backend;

import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.postgresql.PostgreSQLContainer;

// A static field rather than a @Bean: a bean container is stopped with its context, so every cached or
// dirtied context would start its own Postgres. This one is started on first use and lives for the JVM.
public interface PostgresTestcontainer {

    @ServiceConnection
    PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:16");
}
