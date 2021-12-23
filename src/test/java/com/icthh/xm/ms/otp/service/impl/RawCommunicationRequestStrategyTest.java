package com.icthh.xm.ms.otp.service.impl;

import static org.junit.Assert.assertEquals;

import com.icthh.xm.ms.otp.OtpApp;
import com.icthh.xm.ms.otp.client.domain.CommunicationMessage;
import com.icthh.xm.ms.otp.config.SecurityBeanOverrideConfiguration;
import com.icthh.xm.ms.otp.config.tenant.WebappTenantOverrideConfiguration;
import com.icthh.xm.ms.otp.domain.OtpSpec;
import com.icthh.xm.ms.otp.domain.enumeration.ReceiverTypeKey;
import com.icthh.xm.ms.otp.service.dto.OneTimePasswordDto;
import java.util.SortedMap;
import java.util.TreeMap;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {
    SecurityBeanOverrideConfiguration.class,
    OtpApp.class,
    WebappTenantOverrideConfiguration.class
})
public class RawCommunicationRequestStrategyTest {

    private static final int TTL = 600;
    private static final Integer MAX_RETRIES = 3;
    private static final int LENGTH = 6;
    private static final String TYPE_KEY = "TYPE1";
    private static final String OTP_SENDER_ID = "Vodafone";
    private static final String PASSWORD = "password";

    @Autowired
    private RawCommunicationRequestStrategy requestStrategy;

    @Test
    public void testPrepareRequest() {
        OneTimePasswordDto oneTimePasswordDto = new OneTimePasswordDto();
        oneTimePasswordDto.setLangKey("EN");

        CommunicationMessage result = requestStrategy.prepareRequest(
            PASSWORD,
            generateOtpTypeSpec(),
            oneTimePasswordDto
        );

        assertEquals("Your otp password", result.getContent());
    }

    @Test
    public void testPrepareRequestWithEmptyLang() {
        CommunicationMessage result = requestStrategy.prepareRequest(
            PASSWORD,
            generateOtpTypeSpec(),
            new OneTimePasswordDto()
        );

        assertEquals("Your otp password", result.getContent());
    }

    @Test
    public void testRenderMissingLang() {
        OneTimePasswordDto oneTimePasswordDto = new OneTimePasswordDto();
        oneTimePasswordDto.setLangKey("ZZZ");

        CommunicationMessage result = requestStrategy.prepareRequest(
            PASSWORD,
            generateOtpTypeSpec(),
            new OneTimePasswordDto()
        );

        assertEquals("Your otp password", result.getContent());
    }

    @Test
    public void testPrepareRequestUa() {
        OneTimePasswordDto oneTimePasswordDto = new OneTimePasswordDto();
        oneTimePasswordDto.setLangKey("UA");

        CommunicationMessage result = requestStrategy.prepareRequest(
            PASSWORD,
            generateOtpTypeSpec(),
            oneTimePasswordDto
        );

        assertEquals("Це ваш otp password", result.getContent());
    }

    @Test
    public void testPrepareRequestRu() {
        OneTimePasswordDto oneTimePasswordDto = new OneTimePasswordDto();
        oneTimePasswordDto.setLangKey("RU");

        CommunicationMessage result = requestStrategy.prepareRequest(
            PASSWORD,
            generateOtpTypeSpec(),
            oneTimePasswordDto
        );

        assertEquals("Это ваш otp password", result.getContent());
    }

    @Test(expected = IllegalStateException.class)
    public void testPrepareRequestEmptyMessage() {
        OtpSpec.OtpTypeSpec otpTypeSpec = generateOtpTypeSpec();
        otpTypeSpec.setMessage(null);

        requestStrategy.prepareRequest(PASSWORD, otpTypeSpec, new OneTimePasswordDto());
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
            OTP_SENDER_ID,
            null,
            null,
            null
        );
    }
}
