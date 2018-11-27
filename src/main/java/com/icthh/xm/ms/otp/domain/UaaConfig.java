package com.icthh.xm.ms.otp.domain;


import lombok.Data;

@Data
public class UaaConfig {
    private String systemAuthUrl;
    private String systemUsername;
    private String systemPassword;
    private String systemClientToken;
}
