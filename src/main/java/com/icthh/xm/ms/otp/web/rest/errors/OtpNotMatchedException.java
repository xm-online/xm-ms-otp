package com.icthh.xm.ms.otp.web.rest.errors;

import static com.icthh.xm.ms.otp.web.rest.errors.ErrorConstants.OTP_NOT_MATCHED;

public class OtpNotMatchedException extends InvalidPasswordException {

    public OtpNotMatchedException() {
        super("OTP is not correct", OTP_NOT_MATCHED);
    }
}
