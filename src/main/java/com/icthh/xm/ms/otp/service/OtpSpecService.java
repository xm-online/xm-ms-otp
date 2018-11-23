package com.icthh.xm.ms.otp.service;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.icthh.xm.commons.config.client.api.RefreshableConfiguration;
import com.icthh.xm.ms.otp.config.ApplicationProperties;
import com.icthh.xm.ms.otp.domain.OtpSpec;
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

    @Override
    public void onRefresh(String updatedKey, String config) {
        try {
            this.otpSpec = mapper.readValue(config, OtpSpec.class);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public boolean isListeningConfiguration(String updatedKey) {
        return appProps.getSpecPath().equals(updatedKey);
    }

    @Override
    public void onInit(String configKey, String configValue) {
        onRefresh(configKey, configValue);
    }
}
