package com.icthh.xm.ms.otp.web.rest.errors;

import static com.icthh.xm.ms.otp.web.rest.errors.ErrorConstants.MAX_OTP_ATTEMPTS_EXCEEDED;

public class MaxOtpAttemptsExceededException extends InvalidPasswordException {

    public MaxOtpAttemptsExceededException() {
        super("Maximum OTP attempts exceeded", MAX_OTP_ATTEMPTS_EXCEEDED);
    }
}
