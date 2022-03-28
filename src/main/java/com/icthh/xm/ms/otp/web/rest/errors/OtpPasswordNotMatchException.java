package com.icthh.xm.ms.otp.web.rest.errors;

import static com.icthh.xm.ms.otp.web.rest.errors.ErrorConstants.OTP_PASSWORD_NOT_MATCH;

public class OtpPasswordNotMatchException extends InvalidPasswordException {

    public OtpPasswordNotMatchException() {
        super("OTP Password is not correct", OTP_PASSWORD_NOT_MATCH);
    }
}
