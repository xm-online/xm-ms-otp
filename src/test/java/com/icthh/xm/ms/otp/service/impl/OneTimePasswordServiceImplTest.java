package com.icthh.xm.ms.otp.service.impl;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.icthh.xm.ms.otp.OtpApp;
import com.icthh.xm.ms.otp.config.SecurityBeanOverrideConfiguration;
import com.icthh.xm.ms.otp.config.tenant.WebappTenantOverrideConfiguration;
import com.icthh.xm.ms.otp.domain.OneTimePassword;
import com.icthh.xm.ms.otp.domain.OtpSpec;
import com.icthh.xm.ms.otp.domain.enumeration.ReceiverTypeKey;
import com.icthh.xm.ms.otp.repository.OneTimePasswordRepository;
import com.icthh.xm.ms.otp.service.dto.OneTimePasswordDto;
import freemarker.template.TemplateException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.SortedMap;
import java.util.TreeMap;

import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;

@Slf4j
@RunWith(SpringRunner.class)
@WithMockUser(authorities = {"SUPER-ADMIN"})
@SpringBootTest(classes = {
    SecurityBeanOverrideConfiguration.class,
    OtpApp.class,
    WebappTenantOverrideConfiguration.class
})
public class OneTimePasswordServiceImplTest {

    @Autowired
    private OneTimePasswordServiceImpl oneTimePasswordService;

    @MockBean
    private OneTimePasswordRepository oneTimePasswordRepository;

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

    @Test
    public void testFindAll() {
        OneTimePassword oneTimePassword1 = new OneTimePassword();
        oneTimePassword1.setId(1L);
        OneTimePassword oneTimePassword2 = new OneTimePassword();
        oneTimePassword2.setId(2L);
        List<OneTimePassword> otpList = new ArrayList<>();
        otpList.add(oneTimePassword1);
        otpList.add(oneTimePassword2);

        when(oneTimePasswordRepository.findAll()).thenReturn(otpList);
        List<OneTimePasswordDto> dtoList = oneTimePasswordService.findAll();

        Assert.assertSame(dtoList.size(), 2);
        verify(oneTimePasswordRepository, times(1)).findAll();
    }

    @Test
    public void testFindOne() {
        OneTimePassword oneTimePassword = new OneTimePassword();
        oneTimePassword.setId(1L);
        when(oneTimePasswordRepository.findById(1L)).thenReturn(Optional.of(oneTimePassword));
        Optional<OneTimePasswordDto> dto = oneTimePasswordService.findOne(1L);

        Assert.assertTrue(dto.isPresent());
        Assert.assertSame(dto.get().getId(), 1L);
        verify(oneTimePasswordRepository, times(1)).findById(1L);
    }

    @Test
    public void testDelete() {
        doNothing().when(oneTimePasswordRepository).deleteById(1L);
        oneTimePasswordService.delete(1L);
        verify(oneTimePasswordRepository, times(1)).deleteById(1L);
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
