package com.icthh.xm.ms.otp.web.rest.errors;

import com.icthh.xm.ms.otp.OtpApp;
import com.icthh.xm.ms.otp.config.SecurityBeanOverrideConfiguration;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test class for the ExceptionTranslator controller advice.
 *
 * @see DefaultExceptionTranslator
 */

@ActiveProfiles("commons-style")
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {SecurityBeanOverrideConfiguration.class, OtpApp.class})
public class CommonsStyleExceptionTranslatorTest {

    @Autowired
    private ExceptionTranslatorTestController controller;

    @Autowired
    private CommonsStyleExceptionTranslator exceptionTranslator;

    @Autowired
    private MappingJackson2HttpMessageConverter jacksonMessageConverter;

    private MockMvc mockMvc;

    @Before
    public void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
            .setControllerAdvice(exceptionTranslator)
            .setMessageConverters(jacksonMessageConverter)
            .build();
    }

    @Test
    public void testInternalServerError() throws Exception {
        mockMvc.perform(get("/test/internal-server-error"))
            .andExpect(status().isInternalServerError())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
            .andExpect(jsonPath("$.error").value("error.internalServerError"))
            .andExpect(jsonPath("$.error_description").value("Internal server error, please try later"));
    }

    @Test
    public void concurrencyFailureException() throws Exception {
        mockMvc.perform(get("/test/concurrency-failure"))
            .andExpect(status().isConflict())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
            .andExpect(jsonPath("$.error").value("error.concurrencyFailure"))
            .andExpect(jsonPath("$.error_description").value("Concurrency failure"));
    }

    @Test
    public void invalidPasswordException() throws Exception {
        mockMvc.perform(get("/test/invalid-password-error"))
            .andExpect(status().isBadRequest())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
            .andExpect(jsonPath("$.error").value("error.otp.invalid.password"))
            .andExpect(jsonPath("$.error_description").value("Incorrect password"));
    }

    @Test
    public void expiredOtpException() throws Exception {
        mockMvc.perform(get("/test/expired-otp-error"))
            .andExpect(status().isBadRequest())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
            .andExpect(jsonPath("$.error").value("error.otp.expired"))
            .andExpect(jsonPath("$.error_description").value("OTP expired"));
    }

    @Test
    public void illegalOtpStateException() throws Exception {
        mockMvc.perform(get("/test/illegal-otp-state-error"))
            .andExpect(status().isBadRequest())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
            .andExpect(jsonPath("$.error").value("error.illegal.otp.state"))
            .andExpect(jsonPath("$.error_description").value("Illegal OTP State"));
    }

    @Test
    public void maxOtpAttemptsExceededException() throws Exception {
        mockMvc.perform(get("/test/max-otp-attempts-exceeded-error"))
            .andExpect(status().isBadRequest())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
            .andExpect(jsonPath("$.error").value("error.max.otp.attempts.exceeded"))
            .andExpect(jsonPath("$.error_description").value("Maximum OTP attempts exceeded"));
    }

    @Test
    public void otpNotMatchedException() throws Exception {
        mockMvc.perform(get("/test/otp-not-matched-exceeded-error"))
            .andExpect(status().isBadRequest())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
            .andExpect(jsonPath("$.error").value("error.otp.not.matched"))
            .andExpect(jsonPath("$.error_description").value("OTP is not correct"));
    }

    @Test
    public void otpGenerationLimitReachedException() throws Exception {
        mockMvc.perform(get("/test/otp-generation-limit-reached-error"))
            .andExpect(status().isBadRequest())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
            .andExpect(jsonPath("$.error").value("error.otp.generation.limit.reached"))
            .andExpect(jsonPath("$.error_description").value("OTP generation limit reached"));
    }

    @Test
    public void badRequestAlertException() throws Exception {
        mockMvc.perform(get("/test/bad-request-alert-error"))
            .andExpect(status().isBadRequest())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
            .andExpect(jsonPath("$.error").value("error.otp.generation.failure"))
            .andExpect(jsonPath("$.error_description").value("Cannot create otp"));
    }

    @Test
    public void illegalArgumentException() throws Exception {
        mockMvc.perform(get("/test/illegal-argument-error"))
            .andExpect(status().isBadRequest())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
            .andExpect(jsonPath("$.error").value("error.illegal.argument"))
            .andExpect(jsonPath("$.error_description").value("Profile TEST-PROFILE not found"));
    }

    @Test
    public void illegalStateException() throws Exception {
        mockMvc.perform(get("/test/illegal-state-error"))
            .andExpect(status().isInternalServerError())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
            .andExpect(jsonPath("$.error").value("error.illegal.state"))
            .andExpect(jsonPath("$.error_description").value("Can't send message, because tenant config is null"));
    }

    @Test
    public void businessException() throws Exception {
        mockMvc.perform(get("/test/business-error"))
            .andExpect(status().isBadRequest())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
            .andExpect(jsonPath("$.error").value("error.otp.retrieval.failure"))
            .andExpect(jsonPath("$.error_description").value("Could not retrieve otp"));
    }

    @Test
    public void otpInternalServerErrorException() throws Exception {
        mockMvc.perform(get("/test/otp-internal-server-error"))
            .andExpect(status().isInternalServerError())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
            .andExpect(jsonPath("$.error").value("error.otp.internal.server.error"))
            .andExpect(jsonPath("$.error_description").value("Internal Server Error from OTP"));
    }

    @Test
    public void customParametrizedErrorException() throws Exception {
        mockMvc.perform(get("/test/parameterized-error"))
            .andExpect(status().isBadRequest())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
            .andExpect(jsonPath("$.error").value("error.custom.parametrized.error"))
            .andExpect(jsonPath("$.error_description").value("test parameterized error"))
            .andExpect(jsonPath("$.params.param0").value("param0_value"))
            .andExpect(jsonPath("$.params.param1").value("param1_value"));
    }

    @Test
    public void customParametrizedErrorException2() throws Exception {
        mockMvc.perform(get("/test/parameterized-error2"))
            .andExpect(status().isBadRequest())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
            .andExpect(jsonPath("$.error").value("error.custom.parametrized.error"))
            .andExpect(jsonPath("$.error_description").value("test parameterized error"))
            .andExpect(jsonPath("$.params.foo").value("foo_value"))
            .andExpect(jsonPath("$.params.bar").value("bar_value"));
    }

    @Test
    public void customParametrizedErrorExceptionWithoutMessage() throws Exception {
        mockMvc.perform(get("/test/parameterized-error-without-message"))
            .andExpect(status().isBadRequest())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
            .andExpect(jsonPath("$.error").value("error.custom.parametrized.error"))
            .andExpect(jsonPath("$.error_description").value("Parameterized Exception"))
            .andExpect(jsonPath("$.params.foo").value("foo_value"))
            .andExpect(jsonPath("$.params.bar").value("bar_value"));
    }

    @Test
    public void emailAlreadyUsedException() throws Exception {
        mockMvc.perform(get("/test/email-already-used"))
            .andExpect(status().isBadRequest())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
            .andExpect(jsonPath("$.error").value("error.email.already.used"))
            .andExpect(jsonPath("$.error_description").value("Email is already in use!"));
    }

    @Test
    public void emailNotFoundException() throws Exception {
        mockMvc.perform(get("/test/email-not-found"))
            .andExpect(status().isBadRequest())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
            .andExpect(jsonPath("$.error").value("error.email.not.found"))
            .andExpect(jsonPath("$.error_description").value("Email address not registered"));
    }

    @Test
    public void loginAlreadyUsedException() throws Exception {
        mockMvc.perform(get("/test/login-already-used"))
            .andExpect(status().isBadRequest())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
            .andExpect(jsonPath("$.error").value("error.login.already.used"))
            .andExpect(jsonPath("$.error_description").value("Login name already used!"));
    }

    @Test
    public void abstractThrowableProblem() throws Exception {
        mockMvc.perform(get("/test/abstract-throwable-problem-impl"))
            .andExpect(status().isForbidden())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
            .andExpect(jsonPath("$.error_description").value("Too often otp generation"))
            .andExpect(jsonPath("$.error").value("error.403"))
            .andExpect(jsonPath("$.params.key1").value("var1"))
            .andExpect(jsonPath("$.params.key2").value("var2"));
    }

    @Test
    public void abstractThrowableProblemWithoutMessage() throws Exception {
        mockMvc.perform(get("/test/abstract-throwable-problem-impl-without-message"))
            .andExpect(status().isForbidden())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
            .andExpect(jsonPath("$.error_description").value("OTP Generation Exception"))
            .andExpect(jsonPath("$.error").value("error.403"))
            .andExpect(jsonPath("$.params.key1").value("var1"))
            .andExpect(jsonPath("$.params.key2").value("var2"));
    }
}
