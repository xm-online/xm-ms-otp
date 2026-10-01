package com.icthh.xm.ms.otp.config;

import com.icthh.xm.commons.permission.access.XmPermissionEvaluator;
import com.icthh.xm.commons.security.RoleConstant;
import com.icthh.xm.commons.security.jwt.TokenProvider;
import com.icthh.xm.commons.security.spring.config.SecurityConfiguration;
import com.icthh.xm.ms.otp.security.AuthoritiesConstants;
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
                                             String contentSecurityPolicy) {
        super(tokenProvider, contentSecurityPolicy);
    }

    /**
     * The URL rules the service had before the migration: the OAuth2 login flow endpoints are public,
     * the rest of {@code /api/**} requires a token. The Spring Security OAuth2 resource server permitted
     * requests matched by no rule, while Spring Security 6 denies them, so the same fallback is kept.
     * Frame options stay disabled as before (the login page may be embedded).
     */
    @Override
    protected HttpSecurity applyUrlSecurity(HttpSecurity http) {
        try {
            return http
                .headers(headers -> headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::disable))
                .authorizeHttpRequests(auth -> auth
                    .requestMatchers("/api/login").permitAll()
                    .requestMatchers("/api/userinfo").permitAll()
                    .requestMatchers("/api/oauth/token").permitAll()
                    .requestMatchers("/api/**").authenticated()
                    .requestMatchers("/management/health").permitAll()
                    .requestMatchers("/management/info").permitAll()
                    .requestMatchers("/management/prometheus/**").permitAll()
                    .requestMatchers("/management/**").hasAuthority(AuthoritiesConstants.ADMIN)
                    .requestMatchers("/v3/api-docs/**").hasAnyAuthority(RoleConstant.SUPER_ADMIN, RoleConstant.ADMIN)
                    .anyRequest().permitAll());
        } catch (Exception e) {
            throw new IllegalStateException("Cannot configure URL security", e);
        }
    }

    @Primary
    @Bean
    static MethodSecurityExpressionHandler expressionHandler(XmPermissionEvaluator customPermissionEvaluator) {
        DefaultMethodSecurityExpressionHandler expressionHandler = new DefaultMethodSecurityExpressionHandler();
        expressionHandler.setPermissionEvaluator(customPermissionEvaluator);
        return expressionHandler;
    }
}
