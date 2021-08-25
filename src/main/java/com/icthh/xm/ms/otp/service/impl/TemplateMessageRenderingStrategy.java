package com.icthh.xm.ms.otp.service.impl;

import com.icthh.xm.ms.otp.service.dto.OneTimePasswordDto;
import com.icthh.xm.ms.otp.service.template.TemplateService;
import java.util.HashMap;
import org.apache.commons.lang3.StringUtils;

import com.icthh.xm.commons.tenant.TenantContextHolder;
import com.icthh.xm.commons.tenant.TenantContextUtils;
import com.icthh.xm.commons.tenant.TenantKey;
import com.icthh.xm.ms.otp.domain.OtpSpec;
import java.util.Locale;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TemplateMessageRenderingStrategy implements MessageRenderingStrategy {

    private final TemplateService templateService;
    private final TenantContextHolder tenantContextHolder;
    private final static String MSISDN_MODEL_KEY = "msisdn";

    @Override
    public String render(OtpSpec.OtpTypeSpec otpTypeSpec, String langKey, Map<String, Object> model) {
        TenantKey tenantKey = TenantContextUtils.getRequiredTenantKey(tenantContextHolder.getContext());

        String messageTemplate = otpTypeSpec.getMessageTemplate();
        log.info("render: messageTemplate: '{}' langKey: '{}'", messageTemplate, langKey);
        return templateService.renderFromTemplate(tenantKey, new Locale(langKey), messageTemplate, model);
    }

    @Override
    public boolean isApplicable(OtpSpec.OtpTypeSpec otpTypeSpec) {
        return StringUtils.isNotBlank(otpTypeSpec.getMessageTemplate());
    }

    @Override
    public Map<String, Object> prepareModel(String otp, OneTimePasswordDto oneTimePasswordDto) {
        Map<String, Object> model = new HashMap<>();
        model.put(OTP, otp);
        if (oneTimePasswordDto != null) {
            model.put(MSISDN_MODEL_KEY, oneTimePasswordDto.getReceiver());
        }

        return model;
    }
}
