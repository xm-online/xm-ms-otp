package com.icthh.xm.ms.otp.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class UaaConfig {
    private String systemAuthUrl;
    private String systemUsername;
    private String systemPassword;
    private String systemClientToken;
}
