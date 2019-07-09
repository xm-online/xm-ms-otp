package com.icthh.xm.ms.otp.service;

import com.icthh.xm.commons.config.client.api.RefreshableConfiguration;
import com.icthh.xm.commons.logging.aop.IgnoreLogginAspect;
import com.icthh.xm.commons.tenant.TenantContextHolder;
import com.icthh.xm.commons.tenant.TenantContextUtils;
import com.icthh.xm.ms.otp.config.ApplicationProperties;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.util.AntPathMatcher;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@RequiredArgsConstructor
@Component
public class LoginPageRefreshableConfiguration implements RefreshableConfiguration {

    private static final String TENANT_NAME = "tenantName";
    private final ConcurrentHashMap<String, String> loginPages = new ConcurrentHashMap<>();
    private final AntPathMatcher matcher = new AntPathMatcher();

    private final TenantContextHolder tenantContextHolder;
    private final ApplicationProperties appProps;

    @Override
    public void onRefresh(String updatedKey, String config) {
        log.info("Setting login page");
        String pattern = appProps.getLoginPage();
        String tenant = matcher.extractUriTemplateVariables(pattern, updatedKey).get(TENANT_NAME);
        if (StringUtils.isBlank(config)) {
            loginPages.remove(tenant);
            log.info("Login page for tenant {} was removed: {}", tenant, updatedKey);
            return;
        }
        loginPages.put(tenant, config);
        log.info("Login page for tenant {} was updated: {}", tenant, updatedKey);
    }

    public String getLoginContent(){
        String tenantKey = TenantContextUtils.getRequiredTenantKeyValue(tenantContextHolder);
        return Optional.ofNullable(loginPages.get(tenantKey)).orElse(null);
    }

    @Override
    public boolean isListeningConfiguration(String updatedKey) {
        String pattern = appProps.getLoginPage();
        return matcher.match(pattern, updatedKey);
    }

    @Override
    public void onInit(String configKey, String configValue) {
        onRefresh(configKey, configValue);
    }
}
