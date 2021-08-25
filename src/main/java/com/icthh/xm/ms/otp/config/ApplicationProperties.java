package com.icthh.xm.ms.otp.config;

import com.icthh.xm.commons.lep.TenantScriptStorage;
import com.icthh.xm.ms.otp.domain.enumeration.ReceiverTypeKey;
import io.github.jhipster.config.JHipsterProperties;
import java.util.Map;
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
 * See {@link JHipsterProperties} for a good example.
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
    private String emailPathPattern;
    private String dbSchemaSuffix;
    private String loginPage;
    private final Retry retry = new Retry();
    private final Communication communication = new Communication();

    private final Lep lep = new Lep();

    private List<String> tenantIgnoredPathList = Collections.emptyList();
    private List<String> timelineIgnoredHttpMethods = Collections.emptyList();

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

    @Data
    public static class Communication {
        private Map<ReceiverTypeKey, String> messageTypes = Collections.emptyMap();
    }
}
