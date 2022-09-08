package com.icthh.xm.ms.otp.web.rest.errors;

import com.icthh.xm.commons.i18n.error.domain.vm.ErrorVM;
import com.icthh.xm.commons.i18n.error.domain.vm.ParameterizedErrorVM;
import com.icthh.xm.commons.i18n.error.web.ExceptionTranslator;
import com.icthh.xm.commons.i18n.spring.service.LocalizationMessageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.ResponseEntity.BodyBuilder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.zalando.problem.AbstractThrowableProblem;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@ControllerAdvice
@ConditionalOnProperty(prefix = "application", name = "exception-translator", havingValue = "commons-style")
@Primary
public class CommonsStyleExceptionTranslator extends ExceptionTranslator {

    private static final String ERROR_PREFIX = "error.";

    @Autowired
    public CommonsStyleExceptionTranslator(LocalizationMessageService localizationErrorMessageService) {
        super(localizationErrorMessageService);
    }

    @ExceptionHandler(InvalidPasswordException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ErrorVM handleInvalidPasswordException(InvalidPasswordException ex) {
        log.debug("Invalid password", ex);
        return new ErrorVM(ex.getErrorKey(), ex.getTitle());
    }

    @ExceptionHandler(BadRequestAlertException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ErrorVM handleBadRequestAlertException(BadRequestAlertException ex) {
        log.debug("Bad Request Alert", ex);
        return new ErrorVM(ex.getErrorKey(), ex.getTitle());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ErrorVM handleIllegalArgumentException(IllegalArgumentException ex) {
        log.debug("Illegal Argument", ex);
        return new ErrorVM(ErrorConstants.ILLEGAL_ARGUMENT, ex.getMessage());
    }

    @ExceptionHandler(IllegalStateException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ResponseBody
    public ErrorVM handleIllegalStateException(IllegalStateException ex) {
        log.debug("Illegal State", ex);
        return new ErrorVM(ErrorConstants.ILLEGAL_STATE, ex.getMessage());
    }

    @ExceptionHandler(InternalServerErrorException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ResponseBody
    public ErrorVM handleInternalServerErrorException(InternalServerErrorException ex) {
        log.debug("Internal Server Error", ex);
        return new ErrorVM(ErrorConstants.OTP_INTERNAL_SERVER_ERROR, ex.getMessage());
    }

    @ExceptionHandler(CustomParameterizedException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ErrorVM handleCustomParameterizedException(CustomParameterizedException ex) {
        log.debug("Custom Parameterized Exception", ex);
        return createParameterizedErrorVM(ex, ErrorConstants.CUSTOM_PARAMETRIZED_ERROR);
    }

    @ExceptionHandler(EmailAlreadyUsedException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ErrorVM handleEmailAlreadyUsedException(EmailAlreadyUsedException ex) {
        log.debug("Email Already Used", ex);
        return new ErrorVM(ErrorConstants.EMAIL_ALREADY_USED_ERROR, ex.getMessage());
    }

    @ExceptionHandler(EmailNotFoundException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ErrorVM handleEmailNotFoundException(EmailNotFoundException ex) {
        log.debug("Email Not Found", ex);
        return new ErrorVM(ErrorConstants.EMAIL_NOT_FOUND_ERROR, ex.getMessage());
    }

    @ExceptionHandler(LoginAlreadyUsedException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ErrorVM handleLoginAlreadyUsedException(LoginAlreadyUsedException ex) {
        log.debug("Login Already Used", ex);
        return new ErrorVM(ErrorConstants.LOGIN_ALREADY_USED_ERROR, ex.getMessage());
    }

    @ExceptionHandler(AbstractThrowableProblem.class)
    @ResponseBody
    public ResponseEntity<ParameterizedErrorVM> handleAbstractThrowableProblem(AbstractThrowableProblem ex) {
        log.debug("Abstract Throwable Problem", ex);
        HttpStatus status = Optional.ofNullable(ex.getStatus())
            .map(it -> HttpStatus.valueOf(it.getStatusCode()))
            .orElse(HttpStatus.INTERNAL_SERVER_ERROR);
        BodyBuilder builder = ResponseEntity.status(status);
        ParameterizedErrorVM errorVM = createParameterizedErrorVM(ex, ERROR_PREFIX + status.value());

        return builder.body(errorVM);
    }

    private static ParameterizedErrorVM createParameterizedErrorVM(AbstractThrowableProblem ex, String errorCode) {
        Map<String, Object> exParameters = ex.getParameters();
        String message = getFromMap(exParameters, "message", ex.getTitle());
        Map<String, Object> params = getFromMap(exParameters, "params", new HashMap<>());
        Map<String, String> errorParams = params.entrySet().stream()
            .collect(Collectors.toMap(Map.Entry::getKey, e -> String.valueOf(e.getValue())));

        return new ParameterizedErrorVM(errorCode, message, errorParams);
    }

    private static <T> T getFromMap(Map<String, Object> map, String key, T defaultValue) {
        try {
            return Optional.of(map).map(m -> (T) m.get(key)).orElse(defaultValue);
        } catch (ClassCastException ex) {
            return defaultValue;
        }
    }
}
