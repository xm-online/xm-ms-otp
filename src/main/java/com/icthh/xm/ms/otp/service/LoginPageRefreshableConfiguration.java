package com.icthh.xm.ms.otp.service;

import com.icthh.xm.commons.config.client.api.RefreshableConfiguration;
import com.icthh.xm.ms.otp.config.ApplicationProperties;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class LoginPageRefreshableConfiguration implements RefreshableConfiguration {

    private final ApplicationProperties appProps;
    @Getter private String loginHtmlContent;

    @Override
    public void onRefresh(String updatedKey, String config) {
        log.info("Setting login page");
        loginHtmlContent = config;
    }

    @Override
    public boolean isListeningConfiguration(String updatedKey) {
        return Optional.ofNullable(appProps.getLoginPage()).equals(Optional.of(updatedKey));
    }

    @Override
    public void onInit(String configKey, String configValue) {
        onRefresh(configKey, configValue);
    }
}
