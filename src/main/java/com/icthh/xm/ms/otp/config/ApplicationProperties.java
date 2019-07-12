package com.icthh.xm.ms.otp.config;

import com.icthh.xm.commons.lep.TenantScriptStorage;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Collections;
import java.util.List;

/**
 * Properties specific to Otp.
 * <p>
 * Properties are configured in the application.yml file.
 * See {@link io.github.jhipster.config.JHipsterProperties} for a good example.
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "application", ignoreUnknownFields = false)
public class ApplicationProperties {
    private String kafkaSystemTopic;
    private String kafkaSystemQueue;
    private boolean kafkaEnabled;
    private boolean timelinesEnabled;
    private String specPath;
    private String tenantPath;
    private String dbSchemaSuffix;
    private String loginPage;
    private final Retry retry = new Retry();

    private final Lep lep = new Lep();

    private List<String> tenantIgnoredPathList = Collections.emptyList();

    @Data
    private static class Retry {
        private int maxAttempts;
        private long delay;
        private int multiplier;
    }

    @Getter
    @Setter
    public static class Lep {
        private TenantScriptStorage tenantScriptStorage;
        private String lepResourcePathPattern;
    }

}
