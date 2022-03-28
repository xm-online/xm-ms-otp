package com.icthh.xm.ms.otp.web.rest.errors;

import org.zalando.problem.AbstractThrowableProblem;
import org.zalando.problem.Status;

public class InvalidPasswordException extends AbstractThrowableProblem {

    private static final long serialVersionUID = 1L;
    protected String entityName;
    protected String errorKey;

    public InvalidPasswordException() {
        this("Incorrect password");
    }

    public InvalidPasswordException(String title) {
        this(title, "error.otp.invalid.password");
    }

    public InvalidPasswordException(String title, String errorKey) {
        this(title, errorKey, "otp");
    }

    public InvalidPasswordException(String title, String errorKey, String entityName) {
        super(ErrorConstants.INVALID_PASSWORD_TYPE, title, Status.BAD_REQUEST);
        this.errorKey = errorKey;
        this.entityName = entityName;
    }

    public String getErrorKey() {
        return errorKey;
    }

    public String getEntityName() {
        return entityName;
    }
}
