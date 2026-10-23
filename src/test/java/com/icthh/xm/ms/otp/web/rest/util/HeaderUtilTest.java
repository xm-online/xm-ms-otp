package com.icthh.xm.ms.otp.web.rest.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;

public class HeaderUtilTest {

    @Test
    public void createFailureAlertTest() {
        HttpHeaders headers = HeaderUtil.createFailureAlert("entity", "errorKey", "default");
        assertNotNull(headers);
        assertNotNull(headers.get("X-otpApp-error"));
        assertNotNull(headers.get("X-otpApp-params"));
        assertNotNull(headers.get("X-otpApp-errorKey"));
        assertEquals(headers.getFirst("X-otpApp-error"), "default");
        assertEquals(headers.getFirst("X-otpApp-params"), "entity");
        assertEquals(headers.getFirst("X-otpApp-errorKey"), "errorKey");
    }

    @Test
    public void createAlertTest() {
        HttpHeaders headers = HeaderUtil.createAlert("message", "param");
        assertNotNull(headers);
        assertNotNull(headers.get("X-otpApp-alert"));
        assertNotNull(headers.get("X-otpApp-params"));
        assertEquals(headers.getFirst("X-otpApp-alert"), "message");
        assertEquals(headers.getFirst("X-otpApp-params"), "param");
    }
}
