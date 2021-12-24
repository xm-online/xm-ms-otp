package com.icthh.xm.ms.otp.service;

import com.icthh.xm.ms.otp.domain.OtpSpec.OtpTypeSpec;
import com.icthh.xm.ms.otp.service.dto.OneTimePasswordDto;

public interface SpecLimitValidationService {

    void validateSpecificationLimit(final OneTimePasswordDto oneTimePasswordDto,
                                    final OtpTypeSpec otpSpec);
}
