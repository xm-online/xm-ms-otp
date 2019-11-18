package com.icthh.xm.ms.otp.config;

import com.icthh.xm.commons.config.client.service.TenantConfigService;
import com.icthh.xm.commons.lep.commons.CommonsService;
import com.icthh.xm.commons.permission.service.PermissionCheckService;

import com.icthh.xm.ms.otp.lep.XmMsLepProcessingApplicationListener;
import com.icthh.xm.ms.otp.repository.OneTimePasswordRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * The {@link LepAppEventListenerConfiguration} class.
 */
@Configuration
public class LepAppEventListenerConfiguration {

    @Bean
    XmMsLepProcessingApplicationListener buildLepProcessingApplicationListener(
        OneTimePasswordRepository oneTimePasswordRepository,
        TenantConfigService tenantConfigService,
        @Qualifier("loadBalancedRestTemplate") RestTemplate restTemplate,
        CommonsService commonsService,
        PermissionCheckService permissionCheckService) {

        return new XmMsLepProcessingApplicationListener(
            tenantConfigService,
            restTemplate,
            commonsService,
            oneTimePasswordRepository,
            permissionCheckService);
    }

}
