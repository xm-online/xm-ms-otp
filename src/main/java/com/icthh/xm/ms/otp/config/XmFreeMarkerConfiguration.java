package com.icthh.xm.ms.otp.config;

import freemarker.cache.StringTemplateLoader;
import freemarker.cache.TemplateLoader;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;

@Configuration
public class XmFreeMarkerConfiguration {

    @Bean
    public XmFreeMarkerConfigurer xmFreeMarkerConfigurer(StringTemplateLoader emailTemplates) {
        return new XmFreeMarkerConfigurer(emailTemplates);
    }

    @Bean
    public StringTemplateLoader emailTemplates() {
        return new StringTemplateLoader();
    }

    @RequiredArgsConstructor
    public static class XmFreeMarkerConfigurer extends FreeMarkerConfigurer {

        private final StringTemplateLoader emailTemplates;

        @Override
        protected void postProcessTemplateLoaders(List<TemplateLoader> templateLoaders) {
            super.postProcessTemplateLoaders(templateLoaders);
            templateLoaders.add(emailTemplates);
        }
    }
}
