package com.icthh.xm.ms.otp.web.rest.errors;

import static com.icthh.xm.ms.otp.web.rest.errors.ErrorConstants.EXPIRED_OTP;

public class ExpiredOtpException extends InvalidPasswordException {

    public ExpiredOtpException() {
        super("OTP expired", EXPIRED_OTP);
    }
}