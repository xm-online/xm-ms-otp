package com.icthh.xm.ms.otp.service.impl;

import static org.junit.Assert.assertEquals;

import com.icthh.xm.ms.otp.domain.OtpSpec;
import com.icthh.xm.ms.otp.domain.enumeration.ReceiverTypeKey;
import freemarker.template.TemplateException;

import java.io.IOException;
import java.util.SortedMap;
import java.util.TreeMap;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.springframework.test.context.junit4.SpringRunner;

@Slf4j
@RunWith(SpringRunner.class)
public class OneTimePasswordServiceImplTest {

    @InjectMocks
    private OneTimePasswordServiceImpl oneTimePasswordService;

    private static final int TTL = 600;
    private static final Integer MAX_RETRIES = 3;
    private static final int LENGTH = 6;
    private static final String TYPE_KEY = "TYPE1";
    private static final String OTP_SENDER_ID = "Vodafone";

    @Test
    public void testRenderMessage() throws IOException, TemplateException {
        String result = oneTimePasswordService.renderMessage(generateOtpTypeSpec(), "password", "EN");
        assertEquals("Your otp password", result);
    }

    @Test
    public void testRenderMessageWithEmptyLang() throws IOException, TemplateException {
        String result = oneTimePasswordService.renderMessage(generateOtpTypeSpec(), "password", null);
        assertEquals("Your otp password", result);
    }

    @Test
    public void testRenderMissingLang() throws IOException, TemplateException {
        String result = oneTimePasswordService.renderMessage(generateOtpTypeSpec(), "password", "ZZZ");
        assertEquals("Your otp password", result);
    }

    @Test
    public void testRenderMessageUa() throws IOException, TemplateException {
        String result = oneTimePasswordService.renderMessage(generateOtpTypeSpec(), "password", "UA");
        assertEquals("Це ваш otp password", result);
    }

    @Test
    public void testRenderMessageRu() throws IOException, TemplateException {
        String result = oneTimePasswordService.renderMessage(generateOtpTypeSpec(), "password", "RU");
        assertEquals("Это ваш otp password", result);
    }

    @Test(expected = IllegalStateException.class)
    public void testRenderMessageEmptyMessage() throws IOException, TemplateException {
        OtpSpec.OtpTypeSpec otpTypeSpec = generateOtpTypeSpec();
        otpTypeSpec.setMessage(null);

        oneTimePasswordService.renderMessage(otpTypeSpec, "password", null);
    }

    private OtpSpec.OtpTypeSpec generateOtpTypeSpec() {

        SortedMap<String, String> langMap = new TreeMap<>();
        langMap.put("EN", "Your otp ${otp}");
        langMap.put("UA", "Це ваш otp ${otp}");
        langMap.put("RU", "Это ваш otp ${otp}");

        return new OtpSpec.OtpTypeSpec(
            TYPE_KEY,
            "[ab]{4,6}c",
            ReceiverTypeKey.PHONE_NUMBER,
            langMap,
            LENGTH,
            MAX_RETRIES,
            TTL,
            OTP_SENDER_ID
        );
    }
}
