package com.icthh.xm.ms.otp.web.rest.errors;

public class OtpInvalidPasswordException extends InvalidPasswordException {

    public OtpInvalidPasswordException() {
        super();
    }

    public OtpInvalidPasswordException(String title) {
        super(title);
    }
}
