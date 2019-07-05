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
    public static final String X_TENANT = "x-tenant";
    public static final String ACCESS_TOKEN = "access_token";
    public static final String ANONYMOUS_USER = "anonymoususer";
    public static final String MSISDN = "msisdn";
    public static final String DEFAULT_LANGUAGE = "en";

    public static final String DDL_CREATE_SCHEMA = "CREATE SCHEMA IF NOT EXISTS %s";
    public static final String CHANGE_LOG_PATH = "classpath:config/liquibase/master.xml";
    public static final String DB_SCHEMA_CREATION_ENABLED = "db.schema.creation.enabled";

    public static final Version DEFAULT_FREMARKER_VERSION = Configuration.VERSION_2_3_26;

    private Constants() {
    }
}
