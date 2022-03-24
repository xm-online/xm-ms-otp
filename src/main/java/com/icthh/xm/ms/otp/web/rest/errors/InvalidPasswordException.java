package com.icthh.xm.ms.otp.web.rest.errors;

import org.zalando.problem.AbstractThrowableProblem;
import org.zalando.problem.Status;

public class InvalidPasswordException extends AbstractThrowableProblem {

    private static final long serialVersionUID = 1L;

    public InvalidPasswordException() {
        super(ErrorConstants.INVALID_PASSWORD_TYPE, "Incorrect password", Status.BAD_REQUEST);
    }

    public InvalidPasswordException(String title) {
        super(ErrorConstants.INVALID_PASSWORD_TYPE, title, Status.BAD_REQUEST);
    }
}
