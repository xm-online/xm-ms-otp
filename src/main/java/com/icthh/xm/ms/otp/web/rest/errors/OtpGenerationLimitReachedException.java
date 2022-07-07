package com.icthh.xm.ms.otp.web.rest.errors;

import static com.icthh.xm.ms.otp.web.rest.errors.ErrorConstants.OTP_GENERATION_LIMIT_REACHED;

public class OtpGenerationLimitReachedException extends InvalidPasswordException {

    public OtpGenerationLimitReachedException() {
        super("OTP generation limit reached", OTP_GENERATION_LIMIT_REACHED);
    }
}
