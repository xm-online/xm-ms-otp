package com.icthh.xm.ms.otp.lep;

import com.icthh.xm.commons.config.client.service.TenantConfigService;
import com.icthh.xm.commons.lep.api.BaseLepContext;
import com.icthh.xm.commons.lep.api.LepContextFactory;
import com.icthh.xm.commons.permission.service.PermissionCheckService;
import com.icthh.xm.lep.api.LepMethod;
import com.icthh.xm.ms.otp.repository.OneTimePasswordRepository;
import com.icthh.xm.ms.otp.service.OtpSpecService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * Builds the LEP context with the same bindings the xm-commons 2 {@code XmMsLepProcessingApplicationListener} set.
 */
@Component
public class OtpLepContextFactory implements LepContextFactory {

    private final TenantConfigService tenantConfigService;
    private final RestTemplate restTemplate;
    private final OneTimePasswordRepository oneTimePasswordRepository;
    private final PermissionCheckService permissionCheckService;
    private final OtpSpecService otpSpecService;

    public OtpLepContextFactory(TenantConfigService tenantConfigService,
                                @Qualifier("loadBalancedRestTemplate") RestTemplate restTemplate,
                                OneTimePasswordRepository oneTimePasswordRepository,
                                PermissionCheckService permissionCheckService,
                                OtpSpecService otpSpecService) {
        this.tenantConfigService = tenantConfigService;
        this.restTemplate = restTemplate;
        this.oneTimePasswordRepository = oneTimePasswordRepository;
        this.permissionCheckService = permissionCheckService;
        this.otpSpecService = otpSpecService;
    }

    @Override
    public BaseLepContext buildLepContext(LepMethod lepMethod) {
        LepContext lepContext = new LepContext();
        lepContext.services = new LepContext.LepServices();
        lepContext.services.tenantConfigService = tenantConfigService;
        lepContext.services.permissionService = permissionCheckService;
        lepContext.services.specService = otpSpecService;
        lepContext.otpRepository = oneTimePasswordRepository;
        lepContext.templates = new LepContext.LepTemplates();
        lepContext.templates.rest = restTemplate;
        return lepContext;
    }
}
