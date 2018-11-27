package com.icthh.xm.ms.otp.service;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.icthh.xm.commons.config.client.api.RefreshableConfiguration;
import com.icthh.xm.ms.otp.config.ApplicationProperties;
import com.icthh.xm.ms.otp.domain.OtpSpec;
import com.icthh.xm.ms.otp.domain.UaaConfig;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OtpSpecService implements RefreshableConfiguration {

    private final ApplicationProperties appProps;
    private final ObjectMapper mapper = new ObjectMapper(new YAMLFactory());

    @Getter
    @Setter
    private OtpSpec otpSpec;

    @Getter
    @Setter
    private UaaConfig uaaConfig;

    @Override
    public void onRefresh(String updatedKey, String config) {
        try {
            if (appProps.getSpecPath().equals(updatedKey)) {
                this.otpSpec = mapper.readValue(config, OtpSpec.class);
            } else if (appProps.getUaaPath().equals(updatedKey)) {
                this.uaaConfig = mapper.readValue(config, UaaConfig.class);
            }
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public boolean isListeningConfiguration(String updatedKey) {
        return appProps.getSpecPath().equals(updatedKey) || appProps.getUaaPath().equals(updatedKey);
    }

    @Override
    public void onInit(String configKey, String configValue) {
        onRefresh(configKey, configValue);
    }
}
