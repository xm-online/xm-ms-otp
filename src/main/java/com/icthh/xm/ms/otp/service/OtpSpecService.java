package com.icthh.xm.ms.otp.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.icthh.xm.commons.config.client.api.RefreshableConfiguration;
import com.icthh.xm.ms.otp.config.ApplicationProperties;
import com.icthh.xm.ms.otp.domain.OtpSpec;
import com.icthh.xm.ms.otp.domain.TenantConfig;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.stereotype.Service;

import java.util.List;

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
    private TenantConfig tenantConfig;

    @Override
    public void onRefresh(String updatedKey, String config) {
        try {
            if (appProps.getSpecPath().equals(updatedKey)) {
                this.otpSpec = mapper.readValue(config, OtpSpec.class);
            } else if (appProps.getTenantPath().equals(updatedKey)) {
                this.tenantConfig = mapper.readValue(config, TenantConfig.class);
            }
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    public OtpSpec.OtpTypeSpec getOtpTypeSpec(String typeKey) {
        List<OtpSpec.OtpTypeSpec> types = this.getOtpSpec().getTypes();
        OtpSpec.OtpTypeSpec otpType = null;
        for (OtpSpec.OtpTypeSpec type : types) {
            if (type.getKey().equals(typeKey)) {
                otpType = type;
            }
        }
        if (otpType == null) {
            throw new IllegalArgumentException(
                String.format("Profile %s not found", typeKey)
            );
        }
        return otpType;
    }

    @Override
    public boolean isListeningConfiguration(String updatedKey) {
        return appProps.getSpecPath().equals(updatedKey) || appProps.getTenantPath().equals(updatedKey);
    }

    @Override
    public void onInit(String configKey, String configValue) {
        onRefresh(configKey, configValue);
    }
}
