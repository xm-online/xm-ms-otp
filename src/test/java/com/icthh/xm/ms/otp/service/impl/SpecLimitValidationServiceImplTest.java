package com.icthh.xm.ms.otp.service.impl;

import static org.junit.jupiter.api.Assertions.assertThrows;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.icthh.xm.ms.otp.OtpApp;
import com.icthh.xm.ms.otp.config.SecurityBeanOverrideConfiguration;
import com.icthh.xm.ms.otp.config.tenant.WebappTenantOverrideConfiguration;
import com.icthh.xm.ms.otp.domain.OtpSpec;
import com.icthh.xm.ms.otp.repository.OneTimePasswordRepository;
import com.icthh.xm.ms.otp.service.dto.LimitValidationType;
import com.icthh.xm.ms.otp.service.dto.OneTimePasswordDto;
import com.icthh.xm.ms.otp.web.rest.errors.InvalidPasswordException;
import com.icthh.xm.ms.otp.web.rest.errors.OtpGenerationLimitReachedException;
import lombok.extern.slf4j.Slf4j;
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
public class SpecLimitValidationServiceImplTest {

    @Autowired
    SpecLimitValidationServiceImpl validationService;

    @MockitoBean
    OneTimePasswordRepository repo;

    @Test
    public void shouldThrowOtpGenerationLimitReachedException() {
        assertThrows(OtpGenerationLimitReachedException.class, () -> {
            OneTimePasswordDto dto = new OneTimePasswordDto();
            dto.setTypeKey("TEST-TYPE-KEY");
    
            OtpSpec.OtpTypeSpec spec = buildOtpTypeSpec(true);
            when(repo.countAllByReceiverAndTypeKeyAndStateKeyInAndStartDateGreaterThanEqual(any(), any(), any(), any()))
                .thenReturn(5);
    
            validationService.validateSpecificationLimit(dto, spec);
        });
    }

    @Test
    public void shouldNotThrowMaxOtpAttemptsExceededException() {
        assertThrows(InvalidPasswordException.class, () -> {
            OneTimePasswordDto dto = new OneTimePasswordDto();
            dto.setTypeKey("TEST-TYPE-KEY");
    
            OtpSpec.OtpTypeSpec spec = buildOtpTypeSpec(false);
            when(repo.countAllByReceiverAndTypeKeyAndStateKeyInAndStartDateGreaterThanEqual(any(), any(), any(), any()))
                .thenReturn(5);
    
            validationService.validateSpecificationLimit(dto, spec);
        });
    }

    private static OtpSpec.OtpTypeSpec buildOtpTypeSpec(boolean discloseCheckError) {
        OtpSpec.GenerationLimit limit = new OtpSpec.GenerationLimit();
        limit.setValidationType(LimitValidationType.DB);
        limit.setPeriodSeconds(300);
        limit.setValue(5);

        OtpSpec.OtpTypeSpec spec = new OtpSpec.OtpTypeSpec();
        spec.setDiscloseCheckErrors(discloseCheckError);
        spec.setMaxRetries(5);
        spec.setGenerationLimit(limit);

        return spec;
    }
}
