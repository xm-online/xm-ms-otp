package com.icthh.xm.ms.otp.service.impl;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.icthh.xm.ms.otp.OtpApp;
import com.icthh.xm.ms.otp.client.domain.CommunicationMessage;
import com.icthh.xm.ms.otp.client.domain.CommunicationMessage.CommunicationMessageCharacteristic;
import com.icthh.xm.ms.otp.config.SecurityBeanOverrideConfiguration;
import com.icthh.xm.ms.otp.config.tenant.WebappTenantOverrideConfiguration;
import com.icthh.xm.ms.otp.domain.OtpSpec.OtpTypeSpec;
import com.icthh.xm.ms.otp.domain.enumeration.ReceiverTypeKey;
import com.icthh.xm.ms.otp.service.dto.OneTimePasswordDto;
import java.util.HashMap;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {
    SecurityBeanOverrideConfiguration.class,
    OtpApp.class,
    WebappTenantOverrideConfiguration.class
})
public class TemplateCommunicationRequestStrategyTest {

    private static final int TTL = 600;
    private static final Integer MAX_RETRIES = 3;
    private static final int LENGTH = 6;
    private static final String TYPE_KEY = "TYPE2";
    private static final String OTP_SENDER_ID = "Vodafone";
    private static final String PASSWORD = "password";
    private static final String MESSAGE_TEMPLATE = "messageTemplate";

    @Autowired
    private TemplateCommunicationRequestStrategy requestStrategy;

    @Test
    public void testPrepareRequest() {
        String receiver = "email receiver";
        String msisdnReceiver = "msisdn receiver";

        HashMap<String, Object> model = new HashMap<>();
        model.put("msisdn", msisdnReceiver);

        OneTimePasswordDto oneTimePasswordDto = new OneTimePasswordDto();
        oneTimePasswordDto.setModel(model);
        oneTimePasswordDto.setLangKey("EN");
        oneTimePasswordDto.setReceiver(receiver);

        OtpTypeSpec otpTypeSpec = generateOtpTypeSpec();

        CommunicationMessage result = requestStrategy.prepareRequest(
            PASSWORD,
            otpTypeSpec,
            oneTimePasswordDto
        );

        assertEquals(MESSAGE_TEMPLATE, result.getContent());
        assertEquals("TemplatedEmail", result.getType());
        assertEquals(otpTypeSpec.getOtpSenderId(), result.getSender().getId());
        assertEquals(oneTimePasswordDto.getReceiver(), result.getReceiver().get(0).getEmail());

        List<CommunicationMessageCharacteristic> characteristics = result.getCharacteristic();
        assertThat(characteristics, hasSize(4));
        assertThat(characteristics, containsInAnyOrder(
            new CommunicationMessageCharacteristic("otp", PASSWORD),
            new CommunicationMessageCharacteristic("language", "EN"),
            new CommunicationMessageCharacteristic("templateName", MESSAGE_TEMPLATE),
            new CommunicationMessageCharacteristic("msisdn", msisdnReceiver)
        ));
    }

    @Test
    public void testIsApplicable() {
        OtpTypeSpec otpTypeSpec = generateOtpTypeSpec();

        assertFalse(requestStrategy.isApplicable(new OtpTypeSpec(), null));
        assertFalse(requestStrategy.isApplicable(otpTypeSpec, null));
        assertTrue(requestStrategy.isApplicable(otpTypeSpec, "en"));
    }

    private OtpTypeSpec generateOtpTypeSpec() {
        return new OtpTypeSpec(
            TYPE_KEY,
            "[ab]{4,6}c",
            ReceiverTypeKey.EMAIL,
            null,
            LENGTH,
            MAX_RETRIES,
            TTL,
            OTP_SENDER_ID,
            MESSAGE_TEMPLATE,
            List.of("msisdn"),
            null,
            false
        );
    }
}
