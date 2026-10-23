package com.icthh.xm.ms.otp.config;

import com.icthh.xm.commons.migration.db.tenant.SchemaResolver;
import java.sql.SQLException;
import lombok.extern.slf4j.Slf4j;
import org.h2.tools.Server;
import org.springframework.boot.jpa.autoconfigure.JpaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import tech.jhipster.config.JHipsterConstants;

/**
 * Schema per tenant: the liquibase, multi-tenant liquibase and entity manager factory beans the service
 * declared itself before the migration now come from xm-commons {@code DatabaseConfiguration}
 * (same change log, same schemas, same Hibernate multi-tenant connection provider and tenant resolver).
 */
@Slf4j
@Configuration
@EnableJpaRepositories("com.icthh.xm.ms.otp.repository")
@EnableJpaAuditing(auditorAwareRef = "springSecurityAuditorAware")
@EnableTransactionManagement
public class DatabaseConfiguration extends com.icthh.xm.commons.migration.db.config.DatabaseConfiguration {

    private static final String JPA_PACKAGES = "com.icthh.xm.ms.otp.domain";

    public DatabaseConfiguration(Environment env, JpaProperties jpaProperties, SchemaResolver schemaResolver) {
        super(env, jpaProperties, schemaResolver);
    }

    /**
     * Open the TCP port for the H2 database, so it is available remotely.
     *
     * @return the H2 database TCP server
     * @throws SQLException if the server failed to start
     */
    @Bean(initMethod = "start", destroyMethod = "stop")
    @Profile(JHipsterConstants.SPRING_PROFILE_DEVELOPMENT)
    public Server h2TCPServer() throws SQLException {
        log.debug("Starting H2 database");
        return Server.createTcpServer("-tcp", "-tcpAllowOthers");
    }

    @Override
    public String getJpaPackages() {
        return JPA_PACKAGES;
    }
}
