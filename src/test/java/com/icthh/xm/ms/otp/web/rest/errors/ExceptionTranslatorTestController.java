package com.icthh.xm.ms.otp.web.rest.errors;

import com.icthh.xm.commons.exceptions.BusinessException;
import org.springframework.dao.ConcurrencyFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.*;
import org.zalando.problem.AbstractThrowableProblem;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.Map;

import static org.zalando.problem.Status.FORBIDDEN;

@RestController
public class ExceptionTranslatorTestController {

    @GetMapping("/test/concurrency-failure")
    public void concurrencyFailure() {
        throw new ConcurrencyFailureException("test concurrency failure");
    }

    @PostMapping("/test/method-argument")
    public void methodArgument(@Valid @RequestBody TestDTO testDTO) {
    }

    @GetMapping("/test/parameterized-error")
    public void parameterizedError() {
        throw new CustomParameterizedException("test parameterized error", "param0_value", "param1_value");
    }

    @GetMapping("/test/parameterized-error2")
    public void parameterizedError2() {
        Map<String, Object> params = new HashMap<>();
        params.put("foo", "foo_value");
        params.put("bar", "bar_value");
        throw new CustomParameterizedException("test parameterized error", params);
    }

    @GetMapping("/test/parameterized-error-without-message")
    public void parameterizedErrorWithoutMessage() {
        Map<String, Object> params = new HashMap<>();
        params.put("foo", "foo_value");
        params.put("bar", "bar_value");
        throw new CustomParameterizedException(null, params);
    }

    @GetMapping("/test/missing-servlet-request-part")
    public void missingServletRequestPartException(@RequestPart String part) {
    }

    @GetMapping("/test/missing-servlet-request-parameter")
    public void missingServletRequestParameterException(@RequestParam String param) {
    }

    @GetMapping("/test/access-denied")
    public void accessDenied() {
        throw new AccessDeniedException("test access denied!");
    }

    @GetMapping("/test/unauthorized")
    public void unauthorized() {
        throw new BadCredentialsException("test authentication failed!");
    }

    @GetMapping("/test/response-status")
    public void exceptionWithResponseStatus() {
        throw new TestResponseStatusException();
    }

    @GetMapping("/test/internal-server-error")
    public void internalServerError() {
        throw new RuntimeException();
    }

    @GetMapping("/test/invalid-password-error")
    public void invalidPasswordError() {
        throw new InvalidPasswordException();
    }

    @GetMapping("/test/expired-otp-error")
    public void expiredOtpError() {
        throw new ExpiredOtpException();
    }

    @GetMapping("/test/illegal-otp-state-error")
    public void illegalOtpStateError() {
        throw new IllegalOtpStateException();
    }

    @GetMapping("/test/max-otp-attempts-exceeded-error")
    public void maxOtpAttemptsExceededError() {
        throw new MaxOtpAttemptsExceededException();
    }

    @GetMapping("/test/otp-not-matched-exceeded-error")
    public void otpNotMatchedError() {
        throw new OtpNotMatchedException();
    }

    @GetMapping("/test/otp-generation-limit-reached-error")
    public void otpGenerationLimitReachedError() {
        throw new OtpGenerationLimitReachedException();
    }

    @GetMapping("/test/bad-request-alert-error")
    public void badRequestAlertError() {
        throw new BadRequestAlertException(null, "Cannot create otp", "otp", "error.otp.generation.failure");
    }

    @GetMapping("/test/illegal-argument-error")
    public void illegalArgumentError() {
        throw new IllegalArgumentException("Profile TEST-PROFILE not found");
    }

    @GetMapping("/test/illegal-state-error")
    public void illegalStateError() {
        throw new IllegalStateException("Can't send message, because tenant config is null");
    }

    @GetMapping("/test/business-error")
    public void businessError() {
        throw new BusinessException("error.otp.retrieval.failure", "Could not retrieve otp");
    }

    @GetMapping("/test/otp-internal-server-error")
    public void otpInternalServerError() {
        throw new InternalServerErrorException("Internal Server Error from OTP");
    }

    @GetMapping("/test/email-already-used")
    public void emailAlreadyUsedError() {
        throw new EmailAlreadyUsedException();
    }

    @GetMapping("/test/email-not-found")
    public void emailNotFoundError() {
        throw new EmailNotFoundException();
    }

    @GetMapping("/test/login-already-used")
    public void loginAlreadyUsedError() {
        throw new LoginAlreadyUsedException();
    }

    @GetMapping("/test/abstract-throwable-problem-impl")
    public void abstractThrowableProblemError() {
        Map<String, Object> map = new HashMap<>();
        map.put("key1", "var1");
        map.put("key2", "var2");
        throw new AbstractThrowableProblemImpl("Too often otp generation", map);
    }

    @GetMapping("/test/abstract-throwable-problem-impl-without-message")
    public void abstractThrowableProblemErrorWithoutMessage() {
        Map<String, Object> map = new HashMap<>();
        map.put("key1", "var1");
        map.put("key2", "var2");
        throw new AbstractThrowableProblemImpl(null, map);
    }

    public static class TestDTO {

        @NotNull
        private String test;

        public String getTest() {
            return test;
        }

        public void setTest(String test) {
            this.test = test;
        }
    }

    @ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = "test response status")
    @SuppressWarnings("serial")
    public static class TestResponseStatusException extends RuntimeException {
    }
}

class AbstractThrowableProblemImpl extends AbstractThrowableProblem {

    AbstractThrowableProblemImpl(String message, Map<String, Object> paramMap) {
        super(null, "OTP Generation Exception", FORBIDDEN, null, null, null, toProblemParameters(message, paramMap));
    }

    static Map<String, Object> toProblemParameters(String message, Map<String, Object> paramMap) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("message", message);
        parameters.put("params", paramMap);
        return parameters;
    }
}
