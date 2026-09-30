package com.icthh.xm.ms.otp.lep;

import com.icthh.xm.commons.config.client.service.TenantConfigService;
import com.icthh.xm.commons.lep.api.BaseLepContext;
import com.icthh.xm.commons.permission.service.PermissionCheckService;
import com.icthh.xm.ms.otp.repository.OneTimePasswordRepository;
import com.icthh.xm.ms.otp.service.OtpSpecService;
import org.springframework.web.client.RestTemplate;

/**
 * Keeps the names LEP scripts used with the xm-commons 2 bindings ({@link LepMsConstants}):
 * {@code lepContext.services.tenantConfigService}, {@code services.permissionService}, {@code services.specService},
 * {@code lepContext.otpRepository}, {@code templates.rest}. {@code lepContext.commons} is set by xm-commons.
 */
public class LepContext extends BaseLepContext {

    public LepServices services;
    public OneTimePasswordRepository otpRepository;
    public LepTemplates templates;

    public static class LepServices {
        public TenantConfigService tenantConfigService;
        public PermissionCheckService permissionService;
        public OtpSpecService specService;
    }

    public static class LepTemplates {
        public RestTemplate rest;
    }
}
