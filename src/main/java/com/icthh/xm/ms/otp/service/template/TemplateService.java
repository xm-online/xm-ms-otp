package com.icthh.xm.ms.otp.service.template;

import com.icthh.xm.commons.logging.aop.IgnoreLogginAspect;
import com.icthh.xm.commons.tenant.TenantKey;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import java.io.IOException;
import java.util.Locale;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;

@Slf4j
@Service
@IgnoreLogginAspect
@RequiredArgsConstructor
public class TemplateService {

    private final TenantTemplateService tenantTemplateService;
    private final Configuration freeMarkerConfiguration;
    private static final String TEMPLATE_NAME = "templateName";

    public String renderFromTemplate(TenantKey tenantKey,
                                     Locale locale,
                                     String templateName,
                                     Map<String, Object> objectModel) {
        String templateKey = TemplateUtil.templateKey(tenantKey, locale.getLanguage(), templateName);
        String template = tenantTemplateService.getTemplate(templateKey);

        return render(template, objectModel, templateKey);
    }

    public String renderFromRawText(String rawText, Map<String, Object> objectModel) {
        return render(rawText, objectModel, TEMPLATE_NAME);
    }

    private String render(String rawText,
                          Map<String, Object> objectModel,
                          String templateName) {
        try {
            Template template = new Template(templateName, rawText, freeMarkerConfiguration);

            return FreeMarkerTemplateUtils.processTemplateIntoString(template, objectModel);
        } catch (TemplateException e) {
            throw new IllegalStateException("Template rendering failed");
        } catch (IOException e) {
            throw new IllegalStateException("Error while reading template");
        }
    }
}
