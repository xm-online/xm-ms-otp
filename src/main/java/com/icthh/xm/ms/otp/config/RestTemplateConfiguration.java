package com.icthh.xm.ms.otp.config;

import com.icthh.xm.ms.otp.security.HttpComponentsClientHttpRequestFactoryBasicAuth;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.loadbalancer.RestTemplateCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.support.BasicAuthenticationInterceptor;
import org.springframework.web.client.RestTemplate;

/**
 * The rest templates the service declared in its security configuration before the migration.
 */
@Configuration
public class RestTemplateConfiguration {

    @Bean
    @Qualifier("loadBalancedRestTemplate")
    public RestTemplate loadBalancedRestTemplate(ObjectProvider<RestTemplateCustomizer> customizerProvider) {
        RestTemplate restTemplate = new RestTemplate();
        // Spring Cloud LoadBalancer replaces Ribbon (ribbon.http.client.enabled is gone)
        customizerProvider.ifAvailable(customizer -> customizer.customize(restTemplate));
        return restTemplate;
    }

    @Bean
    @Qualifier("internalRestTemplate")
    public RestTemplate internalRestTemplate(ObjectProvider<RestTemplateCustomizer> customizerProvider,
                                             @Value("${jhipster.security.client-authorization.client-id}") String clientId,
                                             @Value("${jhipster.security.client-authorization.client-secret}") String clientSecret) {
        final ClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactoryBasicAuth();
        RestTemplate restTemplate = new RestTemplate(requestFactory);
        customizerProvider.ifAvailable(customizer -> customizer.customize(restTemplate));
        // BasicAuthorizationInterceptor was removed in Spring 5.2+, BasicAuthenticationInterceptor sends the same header
        restTemplate.getInterceptors().add(new BasicAuthenticationInterceptor(clientId, clientSecret));
        return restTemplate;
    }

    @Bean
    @Qualifier("vanillaRestTemplate")
    public RestTemplate vanillaRestTemplate() {
        return new RestTemplate();
    }
}
