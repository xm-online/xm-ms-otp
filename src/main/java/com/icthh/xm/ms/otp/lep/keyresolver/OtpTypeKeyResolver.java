package com.icthh.xm.ms.otp.lep.keyresolver;

import com.icthh.xm.lep.api.LepKeyResolver;
import com.icthh.xm.lep.api.LepMethod;
import com.icthh.xm.ms.otp.service.dto.OneTimePasswordDto;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * Appends the OTP type key to the LEP key, e.g. {@code Generate$$SMS_LOGIN}.
 * xm-commons 5 also looks up the legacy script name ({@code -} to {@code _}, {@code .} to {@code $}),
 * which the xm-commons 2 resolver used to build.
 */
@Component
public class OtpTypeKeyResolver implements LepKeyResolver {

    @Override
    public List<String> segments(LepMethod method) {
        return List.of(method.getParameter("oneTimePasswordDto", OneTimePasswordDto.class).getTypeKey());
    }
}
