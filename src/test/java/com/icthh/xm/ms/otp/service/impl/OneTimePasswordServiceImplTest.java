package com.icthh.xm.ms.otp.service.impl;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.icthh.xm.ms.otp.OtpApp;
import com.icthh.xm.ms.otp.config.SecurityBeanOverrideConfiguration;
import com.icthh.xm.ms.otp.config.tenant.WebappTenantOverrideConfiguration;
import com.icthh.xm.ms.otp.domain.OneTimePassword;
import com.icthh.xm.ms.otp.domain.OtpSpec;
import com.icthh.xm.ms.otp.domain.enumeration.StateKey;
import com.icthh.xm.ms.otp.repository.OneTimePasswordRepository;
import com.icthh.xm.ms.otp.service.OtpSpecService;
import com.icthh.xm.ms.otp.service.dto.OneTimePasswordCheckDto;
import com.icthh.xm.ms.otp.service.dto.OneTimePasswordDto;
import com.icthh.xm.ms.otp.web.rest.errors.ExpiredOtpException;
import com.icthh.xm.ms.otp.web.rest.errors.IllegalOtpStateException;
import com.icthh.xm.ms.otp.web.rest.errors.InvalidPasswordException;
import com.icthh.xm.ms.otp.web.rest.errors.MaxOtpAttemptsExceededException;
import com.icthh.xm.ms.otp.web.rest.errors.OtpPasswordNotMatchException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
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

    @MockBean
    private OtpSpecService otpSpecService;

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

    @Test(expected = InvalidPasswordException.class)
    public void shouldThrowInvalidPasswordException() {
        OneTimePasswordCheckDto dto = new OneTimePasswordCheckDto();

        oneTimePasswordService.check(dto);
    }

    @Test(expected = ExpiredOtpException.class)
    public void shouldThrowExpiredOtpException() {
        OneTimePassword otp = buildOtp();
        otp.endDate(Instant.now().minus(1, ChronoUnit.DAYS));

        OneTimePasswordCheckDto dto = buildDto();

        when(oneTimePasswordRepository.findById(1L)).thenReturn(Optional.of(otp));
        OtpSpec.OtpTypeSpec spec = buildOtpTypeSpec(true);

        when(otpSpecService.getOtpTypeSpec(any())).thenReturn(spec);

        oneTimePasswordService.check(dto);
    }

    @Test(expected = InvalidPasswordException.class)
    public void shouldNotThrowExpiredOtpException() {
        OneTimePassword otp = buildOtp();
        otp.endDate(Instant.now().minus(1, ChronoUnit.DAYS));

        OneTimePasswordCheckDto dto = buildDto();

        when(oneTimePasswordRepository.findById(1L)).thenReturn(Optional.of(otp));
        OtpSpec.OtpTypeSpec spec = buildOtpTypeSpec(false);

        when(otpSpecService.getOtpTypeSpec(any())).thenReturn(spec);

        oneTimePasswordService.check(dto);
    }

    @Test(expected = IllegalOtpStateException.class)
    public void shouldThrowIllegalOtpStateException() {
        OneTimePassword otp = buildOtp();
        otp.stateKey(StateKey.EXPIRED);

        OneTimePasswordCheckDto dto = buildDto();

        when(oneTimePasswordRepository.findById(1L)).thenReturn(Optional.of(otp));
        OtpSpec.OtpTypeSpec spec = buildOtpTypeSpec(true);

        when(otpSpecService.getOtpTypeSpec(any())).thenReturn(spec);

        oneTimePasswordService.check(dto);
    }

    @Test(expected = InvalidPasswordException.class)
    public void shouldNotThrowIllegalOtpStateException() {
        OneTimePassword otp = buildOtp();
        otp.stateKey(StateKey.EXPIRED);

        OneTimePasswordCheckDto dto = buildDto();

        when(oneTimePasswordRepository.findById(1L)).thenReturn(Optional.of(otp));
        OtpSpec.OtpTypeSpec spec = buildOtpTypeSpec(false);

        when(otpSpecService.getOtpTypeSpec(any())).thenReturn(spec);

        oneTimePasswordService.check(dto);
    }

    @Test(expected = MaxOtpAttemptsExceededException.class)
    public void shouldThrowMaxOtpAttemptsExceededException() {
        OneTimePassword otp = buildOtp();
        otp.retries(5);

        OneTimePasswordCheckDto dto = buildDto();

        when(oneTimePasswordRepository.findById(1L)).thenReturn(Optional.of(otp));
        OtpSpec.OtpTypeSpec spec = buildOtpTypeSpec(true);

        when(otpSpecService.getOtpTypeSpec(any())).thenReturn(spec);

        oneTimePasswordService.check(dto);
    }

    @Test(expected = InvalidPasswordException.class)
    public void shouldNotThrowMaxOtpAttemptsExceededException() {
        OneTimePassword otp = buildOtp();
        otp.retries(5);

        OneTimePasswordCheckDto dto = buildDto();

        when(oneTimePasswordRepository.findById(1L)).thenReturn(Optional.of(otp));
        OtpSpec.OtpTypeSpec spec = buildOtpTypeSpec(false);

        when(otpSpecService.getOtpTypeSpec(any())).thenReturn(spec);

        oneTimePasswordService.check(dto);
    }

    @Test(expected = OtpPasswordNotMatchException.class)
    public void shouldThrowOtpPasswordNotMatchException() {
        OneTimePassword otp = buildOtp();
        otp.setPasswordHash("1111");

        OneTimePasswordCheckDto dto = buildDto();

        when(oneTimePasswordRepository.findById(1L)).thenReturn(Optional.of(otp));
        OtpSpec.OtpTypeSpec spec = buildOtpTypeSpec(true);

        when(otpSpecService.getOtpTypeSpec(any())).thenReturn(spec);

        oneTimePasswordService.check(dto);
    }

    @Test(expected = InvalidPasswordException.class)
    public void shouldNotThrowOtpPasswordNotMatchException() {
        OneTimePassword otp = buildOtp();
        otp.setPasswordHash("1111");

        OneTimePasswordCheckDto dto = buildDto();

        when(oneTimePasswordRepository.findById(1L)).thenReturn(Optional.of(otp));
        OtpSpec.OtpTypeSpec spec = buildOtpTypeSpec(false);

        when(otpSpecService.getOtpTypeSpec(any())).thenReturn(spec);

        oneTimePasswordService.check(dto);
    }

    private static OneTimePassword buildOtp() {
        OneTimePassword otp = new OneTimePassword();
        otp.stateKey(StateKey.ACTIVE);
        otp.retries(0);
        otp.endDate(Instant.now().plus(1, ChronoUnit.DAYS));
        otp.passwordHash(DigestUtils.sha256Hex("0000"));
        otp.typeKey("TEST-TYPE-KEY");
        return otp;
    }

    private static OneTimePasswordCheckDto buildDto() {
        OneTimePasswordCheckDto dto = new OneTimePasswordCheckDto();
        dto.setId(1L);
        dto.setOtp("0000");
        return dto;
    }

    private static OtpSpec.OtpTypeSpec buildOtpTypeSpec(boolean discloseCheckError) {
        OtpSpec.OtpTypeSpec spec = new OtpSpec.OtpTypeSpec();
        spec.setDiscloseCheckErrors(discloseCheckError);
        spec.setMaxRetries(5);
        return spec;
    }
}
