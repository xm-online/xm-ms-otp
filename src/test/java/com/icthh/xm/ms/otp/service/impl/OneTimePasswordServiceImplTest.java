package com.icthh.xm.ms.otp.service.impl;

import static org.junit.jupiter.api.Assertions.assertThrows;

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
import com.icthh.xm.ms.otp.web.rest.errors.OtpNotMatchedException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.security.test.context.support.WithMockUser;

@Slf4j
@WithMockUser(authorities = {"SUPER-ADMIN"})
@SpringBootTest(classes = {
    SecurityBeanOverrideConfiguration.class,
    OtpApp.class,
    WebappTenantOverrideConfiguration.class
})
public class OneTimePasswordServiceImplTest {

    @Autowired
    private OneTimePasswordServiceImpl oneTimePasswordService;

    @MockitoBean
    private OneTimePasswordRepository oneTimePasswordRepository;

    @MockitoBean
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

        Assertions.assertSame(dtoList.size(), 2);
        verify(oneTimePasswordRepository, times(1)).findAll();
    }

    @Test
    public void testFindOne() {
        OneTimePassword oneTimePassword = new OneTimePassword();
        oneTimePassword.setId(1L);
        when(oneTimePasswordRepository.findById(1L)).thenReturn(Optional.of(oneTimePassword));
        Optional<OneTimePasswordDto> dto = oneTimePasswordService.findOne(1L);

        Assertions.assertTrue(dto.isPresent());
        Assertions.assertSame(dto.get().getId(), 1L);
        verify(oneTimePasswordRepository, times(1)).findById(1L);
    }

    @Test
    public void testDelete() {
        doNothing().when(oneTimePasswordRepository).deleteById(1L);
        oneTimePasswordService.delete(1L);
        verify(oneTimePasswordRepository, times(1)).deleteById(1L);
    }

    @Test
    public void shouldThrowInvalidPasswordException() {
        assertThrows(InvalidPasswordException.class, () -> {
            OneTimePasswordCheckDto dto = new OneTimePasswordCheckDto();
    
            oneTimePasswordService.check(dto);
        });
    }

    @Test
    public void shouldThrowExpiredOtpException() {
        assertThrows(ExpiredOtpException.class, () -> {
            OneTimePassword otp = buildOtp();
            otp.endDate(Instant.now().minus(1, ChronoUnit.DAYS));
    
            OneTimePasswordCheckDto dto = buildDto();
    
            when(oneTimePasswordRepository.findById(1L)).thenReturn(Optional.of(otp));
            OtpSpec.OtpTypeSpec spec = buildOtpTypeSpec(true);
    
            when(otpSpecService.getOtpTypeSpec(any())).thenReturn(spec);
    
            oneTimePasswordService.check(dto);
        });
    }

    @Test
    public void shouldNotThrowExpiredOtpException() {
        assertThrows(InvalidPasswordException.class, () -> {
            OneTimePassword otp = buildOtp();
            otp.endDate(Instant.now().minus(1, ChronoUnit.DAYS));
    
            OneTimePasswordCheckDto dto = buildDto();
    
            when(oneTimePasswordRepository.findById(1L)).thenReturn(Optional.of(otp));
            OtpSpec.OtpTypeSpec spec = buildOtpTypeSpec(false);
    
            when(otpSpecService.getOtpTypeSpec(any())).thenReturn(spec);
    
            oneTimePasswordService.check(dto);
        });
    }

    @Test
    public void shouldThrowIllegalOtpStateException() {
        assertThrows(IllegalOtpStateException.class, () -> {
            OneTimePassword otp = buildOtp();
            otp.stateKey(StateKey.EXPIRED);
    
            OneTimePasswordCheckDto dto = buildDto();
    
            when(oneTimePasswordRepository.findById(1L)).thenReturn(Optional.of(otp));
            OtpSpec.OtpTypeSpec spec = buildOtpTypeSpec(true);
    
            when(otpSpecService.getOtpTypeSpec(any())).thenReturn(spec);
    
            oneTimePasswordService.check(dto);
        });
    }

    @Test
    public void shouldNotThrowIllegalOtpStateException() {
        assertThrows(InvalidPasswordException.class, () -> {
            OneTimePassword otp = buildOtp();
            otp.stateKey(StateKey.EXPIRED);
    
            OneTimePasswordCheckDto dto = buildDto();
    
            when(oneTimePasswordRepository.findById(1L)).thenReturn(Optional.of(otp));
            OtpSpec.OtpTypeSpec spec = buildOtpTypeSpec(false);
    
            when(otpSpecService.getOtpTypeSpec(any())).thenReturn(spec);
    
            oneTimePasswordService.check(dto);
        });
    }

    @Test
    public void shouldThrowMaxOtpAttemptsExceededException() {
        assertThrows(MaxOtpAttemptsExceededException.class, () -> {
            OneTimePassword otp = buildOtp();
            otp.retries(5);
    
            OneTimePasswordCheckDto dto = buildDto();
    
            when(oneTimePasswordRepository.findById(1L)).thenReturn(Optional.of(otp));
            OtpSpec.OtpTypeSpec spec = buildOtpTypeSpec(true);
    
            when(otpSpecService.getOtpTypeSpec(any())).thenReturn(spec);
    
            oneTimePasswordService.check(dto);
        });
    }

    @Test
    public void shouldNotThrowMaxOtpAttemptsExceededException() {
        assertThrows(InvalidPasswordException.class, () -> {
            OneTimePassword otp = buildOtp();
            otp.retries(5);
    
            OneTimePasswordCheckDto dto = buildDto();
    
            when(oneTimePasswordRepository.findById(1L)).thenReturn(Optional.of(otp));
            OtpSpec.OtpTypeSpec spec = buildOtpTypeSpec(false);
    
            when(otpSpecService.getOtpTypeSpec(any())).thenReturn(spec);
    
            oneTimePasswordService.check(dto);
        });
    }

    @Test
    public void shouldThrowOtpNotMatchedException() {
        assertThrows(OtpNotMatchedException.class, () -> {
            OneTimePassword otp = buildOtp();
            otp.setPasswordHash("1111");
    
            OneTimePasswordCheckDto dto = buildDto();
    
            when(oneTimePasswordRepository.findById(1L)).thenReturn(Optional.of(otp));
            OtpSpec.OtpTypeSpec spec = buildOtpTypeSpec(true);
    
            when(otpSpecService.getOtpTypeSpec(any())).thenReturn(spec);
    
            oneTimePasswordService.check(dto);
        });
    }

    @Test
    public void shouldNotThrowOtpNotMatchedException() {
        assertThrows(InvalidPasswordException.class, () -> {
            OneTimePassword otp = buildOtp();
            otp.setPasswordHash("1111");
    
            OneTimePasswordCheckDto dto = buildDto();
    
            when(oneTimePasswordRepository.findById(1L)).thenReturn(Optional.of(otp));
            OtpSpec.OtpTypeSpec spec = buildOtpTypeSpec(false);
    
            when(otpSpecService.getOtpTypeSpec(any())).thenReturn(spec);
    
            oneTimePasswordService.check(dto);
        });
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
