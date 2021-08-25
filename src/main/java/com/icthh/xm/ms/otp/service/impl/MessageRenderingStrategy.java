package com.icthh.xm.ms.otp.service.impl;

import com.icthh.xm.ms.otp.domain.OtpSpec.OtpTypeSpec;
import com.icthh.xm.ms.otp.service.dto.OneTimePasswordDto;
import java.util.HashMap;
import java.util.Map;

public interface MessageRenderingStrategy {

    String OTP = "otp";

    String render(OtpTypeSpec otpTypeSpec, String langKey, Map<String, Object> model);

    boolean isApplicable(OtpTypeSpec otpTypeSpec);

    default Map<String, Object> prepareModel(String otp, OneTimePasswordDto oneTimePasswordDto) {
        Map<String, Object> model = new HashMap<>();
        model.put(OTP, otp);
        return model;
    }
}
