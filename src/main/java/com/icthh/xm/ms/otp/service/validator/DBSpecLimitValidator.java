package com.icthh.xm.ms.otp.service.validator;

import com.icthh.xm.ms.otp.domain.OtpSpec;
import com.icthh.xm.ms.otp.domain.OtpSpec.OtpTypeSpec;
import com.icthh.xm.ms.otp.domain.enumeration.StateKey;
import com.icthh.xm.ms.otp.repository.OneTimePasswordRepository;
import com.icthh.xm.ms.otp.service.dto.LimitValidationType;
import com.icthh.xm.ms.otp.service.dto.OneTimePasswordDto;
import com.icthh.xm.ms.otp.web.rest.errors.OtpInvalidPasswordException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

import static com.icthh.xm.ms.otp.domain.enumeration.StateKey.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class DBSpecLimitValidator implements SpecLimitValidator {

    public static final List<StateKey> ACTIVE_STATE_KEYS = List.of(ACTIVE, NOT_VALID, EXPIRED);

    private final OneTimePasswordRepository oneTimePasswordRepository;

    @Override
    public void validate(final OneTimePasswordDto oneTimePasswordDto,
                         final OtpTypeSpec otpSpec) {

        OtpSpec.GenerationLimit generationLimit = otpSpec.getGenerationLimit();
        if (isValidSpecLimit(generationLimit, otpSpec.getKey())) {
            log.info("validate: going to check generation limit: {}", generationLimit);

            Integer periodSeconds = generationLimit.getPeriodSeconds();
            Integer limitValue = generationLimit.getValue();

            Instant startDate = Instant.now().minusSeconds(periodSeconds);
            String receiver = oneTimePasswordDto.getReceiver();
            Integer count = oneTimePasswordRepository
                .countAllByReceiverAndTypeKeyAndStateKeyInAndStartDateGreaterThanEqual(
                    receiver,
                    otpSpec.getKey(),
                    ACTIVE_STATE_KEYS,
                    startDate
                );

            if (count >= limitValue) {
                log.error("validate: generation limit reached for receiver: {}", receiver);
                throw new OtpInvalidPasswordException();
            }
        } else {
            log.warn("validate: skipping invalid limit config for spec: {}", otpSpec.getKey());
        }
    }

    @Override
    public LimitValidationType getType() {
        return LimitValidationType.DB;
    }

    private boolean isValidSpecLimit(OtpSpec.GenerationLimit generationLimit, String otpSpecKey) {
        if (generationLimit == null) {
            log.debug("isValidSpecLimit: no limit configured for spec: {}", otpSpecKey);
            return false;
        }
        Integer periodSeconds = generationLimit.getPeriodSeconds();
        Integer limitValue = generationLimit.getValue();

        if (periodSeconds < 0 || limitValue < 0) {
            log.warn("isValidSpecLimit: spec: {} has invalid limit configuration: {}", otpSpecKey, generationLimit);
            return false;
        }

        return true;
    }
}
