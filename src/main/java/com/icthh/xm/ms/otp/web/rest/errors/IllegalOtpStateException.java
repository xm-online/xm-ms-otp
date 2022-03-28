package com.icthh.xm.ms.otp.web.rest.errors;

import static com.icthh.xm.ms.otp.web.rest.errors.ErrorConstants.ILLEGAL_OTP_STATE;

public class IllegalOtpStateException extends InvalidPasswordException {

    public IllegalOtpStateException() {
        super("Illegal OTP State", ILLEGAL_OTP_STATE);
    }
}
