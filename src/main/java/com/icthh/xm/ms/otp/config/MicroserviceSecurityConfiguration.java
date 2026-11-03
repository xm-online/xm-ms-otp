package com.icthh.xm.ms.otp.config;

import com.icthh.xm.commons.permission.access.XmPermissionEvaluator;
import com.icthh.xm.commons.security.XmPermissionCheckService;
import com.icthh.xm.commons.security.jwt.TokenProvider;
import com.icthh.xm.commons.security.spring.config.SecurityConfiguration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;

@Configuration
public class MicroserviceSecurityConfiguration extends SecurityConfiguration {

    public MicroserviceSecurityConfiguration(TokenProvider tokenProvider,
                                             @Value("${jhipster.security.content-security-policy}")
                                             String contentSecurityPolicy,
                                             XmPermissionCheckService permissionCheckService) {
        super(tokenProvider, contentSecurityPolicy, permissionCheckService);
    }

    /**
     * The OAuth2 login flow endpoints are public, the rest are the common rules. The Spring Security OAuth2
     * resource server permitted requests matched by no rule, while Spring Security 6 denies them, so the same
     * fallback is kept. Frame options stay disabled as before (the login page may be embedded).
     */
    @Override
    protected HttpSecurity applyUrlSecurity(HttpSecurity http) {
        http
            .headers(headers -> headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::disable))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/login").permitAll()
                .requestMatchers("/api/userinfo").permitAll()
                .requestMatchers("/api/oauth/token").permitAll());
        super.applyUrlSecurity(http);
        return http.authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
    }

    @Primary
    @Bean
    static MethodSecurityExpressionHandler expressionHandler(XmPermissionEvaluator customPermissionEvaluator) {
        DefaultMethodSecurityExpressionHandler expressionHandler = new DefaultMethodSecurityExpressionHandler();
        expressionHandler.setPermissionEvaluator(customPermissionEvaluator);
        return expressionHandler;
    }
}
