package com.icthh.xm.ms.otp.service.template;

import com.icthh.xm.commons.config.client.api.RefreshableConfiguration;
import com.icthh.xm.commons.logging.LoggingAspectConfig;
import com.icthh.xm.commons.tenant.TenantKey;
import com.icthh.xm.ms.otp.config.ApplicationProperties;
import freemarker.cache.StringTemplateLoader;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.AntPathMatcher;

/**
 * Service for managing template.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TenantTemplateService implements RefreshableConfiguration {

    private static final String FILE_NAME = "fileName";
    private static final String LANG_KEY = "langKey";
    private static final String TENANT_NAME = "tenantName";

    private final ConcurrentHashMap<String, String> templates = new ConcurrentHashMap<>();
    private final AntPathMatcher matcher = new AntPathMatcher();
    private final ApplicationProperties applicationProperties;
    private final StringTemplateLoader templateLoader;

    /**
     * Search template by template key.
     *
     * @param templateKey search key
     * @return template
     */
    @LoggingAspectConfig(resultDetails = false)
    public String getTemplate(String templateKey) {
        if (!templates.containsKey(templateKey)) {
            throw new IllegalArgumentException("Template was not found");
        }
        return templates.get(templateKey);
    }

    @Override
    public void onRefresh(String key, String config) {
        String pathPattern = applicationProperties.getEmailPathPattern();

        String tenantKeyValue = matcher.extractUriTemplateVariables(pathPattern, key).get(TENANT_NAME);
        String langKey = matcher.extractUriTemplateVariables(pathPattern, key).get(LANG_KEY);
        String templateName = matcher.extractUriTemplateVariables(pathPattern, key).get(FILE_NAME);

        String templateKey = TemplateUtil.templateKey(TenantKey.valueOf(tenantKeyValue),
                                                                langKey,
                                                                templateName);
        if (StringUtils.isBlank(config)) {
            templates.remove(templateKey);
            templateLoader.removeTemplate(templateKey);
            log.info("Template '{}' with locale {} for tenant '{}' was removed", templateName,
                            langKey, tenantKeyValue);
        } else {
            templates.put(templateKey, config);
            templateLoader.putTemplate(templateKey, config);
            log.info("Template template '{}' with locale {} for tenant '{}' was updated", templateName,
                            langKey, tenantKeyValue);
        }
    }

    @Override
    public boolean isListeningConfiguration(String updatedKey) {
        String specificationPathPattern = applicationProperties.getEmailPathPattern();
        return matcher.match(specificationPathPattern, updatedKey);
    }

    @Override
    public void onInit(String key, String config) {
        if (isListeningConfiguration(key)) {
            onRefresh(key, config);
        }
    }
}
