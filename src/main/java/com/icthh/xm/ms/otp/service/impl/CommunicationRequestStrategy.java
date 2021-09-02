package com.icthh.xm.ms.otp.service.impl;

import com.icthh.xm.ms.otp.client.domain.CommunicationMessage;
import com.icthh.xm.ms.otp.domain.OtpSpec.OtpTypeSpec;
import com.icthh.xm.ms.otp.service.dto.OneTimePasswordDto;

public interface CommunicationRequestStrategy {

    String OTP_MODEL_KEY = "otp";
    String TEMPLATE_NAME = "templateName";

    CommunicationMessage prepareRequest(String otp,
                                        OtpTypeSpec otpTypeSpec,
                                        OneTimePasswordDto otpDto);

    boolean isApplicable(OtpTypeSpec otpTypeSpec, String langKey);
}
