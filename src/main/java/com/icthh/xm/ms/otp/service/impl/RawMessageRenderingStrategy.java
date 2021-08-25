package com.icthh.xm.ms.otp.service.impl;

import com.icthh.xm.ms.otp.domain.OtpSpec;
import com.icthh.xm.ms.otp.service.template.TemplateService;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RawMessageRenderingStrategy implements MessageRenderingStrategy {

    private final TemplateService templateService;

    @Override
    public String render(OtpSpec.OtpTypeSpec otpTypeSpec, String langKey, Map<String, Object> model) {
        if (langKey == null || StringUtils.isEmpty(otpTypeSpec.getMessage().get(langKey))) {
            langKey = otpTypeSpec.getMessage().firstKey();
        }
        String messageText = otpTypeSpec.getMessage().get(langKey);

        return templateService.renderFromRawText(messageText, model);
    }

    @Override
    public boolean isApplicable(OtpSpec.OtpTypeSpec otpTypeSpec) {
        return MapUtils.isNotEmpty(otpTypeSpec.getMessage());
    }
}
