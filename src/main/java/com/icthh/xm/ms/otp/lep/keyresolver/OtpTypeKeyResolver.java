package com.icthh.xm.ms.otp.lep.keyresolver;

import com.icthh.xm.commons.lep.AppendLepKeyResolver;
import com.icthh.xm.lep.api.LepManagerService;
import com.icthh.xm.lep.api.LepMethod;
import com.icthh.xm.lep.api.commons.SeparatorSegmentedLepKey;
import com.icthh.xm.ms.otp.service.dto.OneTimePasswordDto;
import org.springframework.stereotype.Component;

@Component
public class OtpTypeKeyResolver extends AppendLepKeyResolver {

    @Override
    protected String[] getAppendSegments(SeparatorSegmentedLepKey baseKey,
                                         LepMethod method,
                                         LepManagerService managerService) {
        OneTimePasswordDto otp = getRequiredParam(method, "oneTimePasswordDto", OneTimePasswordDto.class);
        String translated = translateToLepConvention(otp.getTypeKey());
        return new String[]{translated};
    }
}
