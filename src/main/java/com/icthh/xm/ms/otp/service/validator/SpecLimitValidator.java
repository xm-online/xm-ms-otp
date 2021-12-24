package com.icthh.xm.ms.otp.service.validator;

import com.icthh.xm.ms.otp.domain.OtpSpec.OtpTypeSpec;
import com.icthh.xm.ms.otp.service.dto.LimitValidationType;
import com.icthh.xm.ms.otp.service.dto.OneTimePasswordDto;

public interface SpecLimitValidator {

    void validate(final OneTimePasswordDto oneTimePasswordDto,
                  final OtpTypeSpec otpSpec);

    LimitValidationType getType();
}
