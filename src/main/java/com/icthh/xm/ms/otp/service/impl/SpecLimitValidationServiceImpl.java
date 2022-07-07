package com.icthh.xm.ms.otp.service.impl;

import static java.util.Optional.ofNullable;

import com.icthh.xm.ms.otp.domain.OtpSpec;
import com.icthh.xm.ms.otp.domain.OtpSpec.OtpTypeSpec;
import com.icthh.xm.ms.otp.service.SpecLimitValidationService;
import com.icthh.xm.ms.otp.service.dto.LimitValidationType;
import com.icthh.xm.ms.otp.service.dto.OneTimePasswordDto;
import com.icthh.xm.ms.otp.service.validator.SpecLimitValidator;
import com.icthh.xm.ms.otp.web.rest.errors.InvalidPasswordException;
import com.icthh.xm.ms.otp.web.rest.errors.MaxOtpAttemptsExceededException;
import java.util.EnumMap;
import java.util.List;
import javax.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class SpecLimitValidationServiceImpl implements SpecLimitValidationService {

    private final List<SpecLimitValidator> validatorList;
    private final EnumMap<LimitValidationType, SpecLimitValidator> validators = new EnumMap<>(LimitValidationType.class);

    @PostConstruct
    void init() {
        validatorList.forEach(validator -> {
            log.info("init: processing {} type validator", validator.getType());
            validators.put(validator.getType(), validator);
        });
    }

    @Override
    public void validateSpecificationLimit(final OneTimePasswordDto oneTimePasswordDto,
                                           final OtpTypeSpec otpSpec) {
        log.info("validateSpecificationLimit: otpDto: {}, otpSpec: {}", oneTimePasswordDto, otpSpec);
        try {
            ofNullable(otpSpec)
                .map(OtpTypeSpec::getGenerationLimit)
                .map(OtpSpec.GenerationLimit::getValidationType)
                .map(validators::get)
                .ifPresent(validator -> validator.validate(oneTimePasswordDto, otpSpec));
        } catch (InvalidPasswordException originException) {
            if (otpSpec.getDiscloseCheckErrors() == Boolean.TRUE) {
                throw originException;
            } else {
                throw new InvalidPasswordException();
            }
        }

    }
}
