package com.icthh.xm.ms.otp.config;

import freemarker.template.Configuration;
import freemarker.template.Version;

/**
 * Application constants.
 */
public final class Constants {

    // Regex for acceptable logins
    public static final String LOGIN_REGEX = "^[_.@A-Za-z0-9-]*$";

    public static final String SYSTEM_ACCOUNT = "system";
    public static final String ANONYMOUS_USER = "anonymoususer";
    public static final String DEFAULT_LANGUAGE = "en";

    public static final Version DEFAULT_FREMARKER_VERSION = Configuration.VERSION_2_3_26;

    private Constants() {
    }
}
