package com.icthh.xm.ms.otp.web.rest.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.LinkedList;

import org.junit.Test;
import org.springframework.http.HttpHeaders;

public class HeaderUtilTest {

    @Test
    public void createFailureAlertTest() {
        HttpHeaders headers = HeaderUtil.createFailureAlert("entity", "errorKey", "default");
        assertNotNull(headers);
        assertNotNull(headers.get("X-otpApp-error"));
        assertNotNull(headers.get("X-otpApp-params"));
        assertNotNull(headers.get("X-otpApp-errorKey"));
        assertEquals(((LinkedList<String>)headers.get("X-otpApp-error")).getFirst(), "default");
        assertEquals(((LinkedList<String>)headers.get("X-otpApp-params")).getFirst(), "entity");
        assertEquals(((LinkedList<String>)headers.get("X-otpApp-errorKey")).getFirst(), "errorKey");
    }

    @Test
    public void createAlertTest() {
        HttpHeaders headers = HeaderUtil.createAlert("message", "param");
        assertNotNull(headers);
        assertNotNull(headers.get("X-otpApp-alert"));
        assertNotNull(headers.get("X-otpApp-params"));
        assertEquals(((LinkedList<String>)headers.get("X-otpApp-alert")).getFirst(), "message");
        assertEquals(((LinkedList<String>)headers.get("X-otpApp-params")).getFirst(), "param");
    }
}
