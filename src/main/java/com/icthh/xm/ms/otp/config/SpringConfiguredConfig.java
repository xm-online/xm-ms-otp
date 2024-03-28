package com.icthh.xm.ms.otp.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.aspectj.EnableSpringConfigured;

@Configuration
@EnableSpringConfigured
@ConditionalOnProperty(value = "spring.configured.enable", havingValue = "true", matchIfMissing = true)
public class SpringConfiguredConfig {}
