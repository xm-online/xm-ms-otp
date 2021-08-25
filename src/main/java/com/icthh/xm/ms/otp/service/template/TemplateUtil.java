package com.icthh.xm.ms.otp.service.template;

import com.icthh.xm.commons.tenant.TenantKey;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;

/**
 * Utility class for working with templates.
 */
@UtilityClass
final class TemplateUtil {

    private static final char SLASH_SIGN = '/';

    /**
     * Email template key generator.
     *
     * @param tenantKey    tenant key
     * @param langKey      localization key
     * @param templateName template file name
     * @return generated key
     */
    static String templateKey(TenantKey tenantKey, String langKey, String templateName) {
        if (StringUtils.isBlank(langKey) || StringUtils.isBlank(templateName)
            || tenantKey == null
            || StringUtils.isBlank(tenantKey.getValue())) {
            throw new IllegalStateException("Language key, template name and tenant must be not blank");
        }
        return tenantKey.getValue() + SLASH_SIGN + langKey + SLASH_SIGN + templateName;
    }
}
