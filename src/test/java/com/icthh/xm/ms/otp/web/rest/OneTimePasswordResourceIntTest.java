package com.icthh.xm.ms.otp.web.rest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.icthh.xm.ms.otp.OtpApp;
import com.icthh.xm.ms.otp.config.ApplicationProperties;
import com.icthh.xm.ms.otp.config.SecurityBeanOverrideConfiguration;
import com.icthh.xm.ms.otp.config.tenant.WebappTenantOverrideConfiguration;
import com.icthh.xm.ms.otp.domain.OneTimePassword;
import com.icthh.xm.ms.otp.domain.OtpSpec;
import com.icthh.xm.ms.otp.domain.enumeration.ReceiverTypeKey;
import com.icthh.xm.ms.otp.domain.enumeration.StateKey;
import com.icthh.xm.ms.otp.repository.OneTimePasswordRepository;
import com.icthh.xm.ms.otp.security.CommunicationService;
import com.icthh.xm.ms.otp.service.OtpSpecService;
import com.icthh.xm.ms.otp.service.dto.OneTimePasswordCheckDTO;
import com.icthh.xm.ms.otp.service.dto.OneTimePasswordDTO;
import com.icthh.xm.ms.otp.service.impl.OneTimePasswordServiceImpl;
import com.icthh.xm.ms.otp.service.mapper.OneTimePasswordMapper;
import com.icthh.xm.ms.otp.web.rest.errors.ExceptionTranslator;

import java.io.IOException;
import java.nio.charset.Charset;
import java.time.Instant;
import java.util.ArrayList;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.Validator;

import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Test class for the OneTimePasswordResource REST controller.
 *
 * @see OneTimePasswordResource
 */
@Slf4j
@RunWith(SpringRunner.class)
@WithMockUser(authorities = {"SUPER-ADMIN"})
@SpringBootTest(classes = {
    SecurityBeanOverrideConfiguration.class,
    OtpApp.class,
    WebappTenantOverrideConfiguration.class
})
public class OneTimePasswordResourceIntTest {

    private static final MediaType APPLICATION_JSON_UTF8 = new MediaType(
        MediaType.APPLICATION_JSON.getType(),
        MediaType.APPLICATION_JSON.getSubtype(),
        Charset.forName("utf8"));

    private static final int TTL = 600;
    private static final Integer MAX_RETRIES = 3;
    private static final int LENGTH = 6;
    private static final String RECEIVER = "+380631234567";
    private static final String TYPE_KEY = "TYPE1";
    private static final String OTP_SENDER_ID = "Voodaphone";

    private MockMvc restMockMvc;

    @Autowired
    private PageableHandlerMethodArgumentResolver pageableArgumentResolver;

    @Autowired
    private OneTimePasswordRepository oneTimePasswordRepository;

    @Autowired
    private ExceptionTranslator exceptionTranslator;

    @Autowired
    private MappingJackson2HttpMessageConverter jacksonMessageConverter;

    @Autowired
    private Validator validator;

    @Autowired
    private OneTimePasswordMapper oneTimePasswordMapper;

    @Autowired
    private ApplicationProperties applicationProperties;

    @Autowired
    OtpSpecService otpSpecService;


    private class CommunicationServiceMock extends CommunicationService {
        public CommunicationServiceMock() {
            super(null, null, null);
        }

        @Override
        public String getSystemToken() {
            return null;
        }

        @Override
        public void sendOneTimePassword(String message, String receiver, String senderId) {

        }
    }

    @SneakyThrows
    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        OneTimePasswordServiceImpl oneTimePasswordService = getOneTimePasswordService();
        OneTimePasswordResource otp = new OneTimePasswordResource(oneTimePasswordService);
        this.restMockMvc = MockMvcBuilders.standaloneSetup(otp)
            .setCustomArgumentResolvers(pageableArgumentResolver)
            .setControllerAdvice(exceptionTranslator)
            .setValidator(validator)
            .setMessageConverters(jacksonMessageConverter).build();
    }

    private OneTimePasswordServiceImpl getOneTimePasswordService() {
        OtpSpecService otpSpecService = new OtpSpecService(applicationProperties);
        OtpSpec otpSpec = new OtpSpec();
        otpSpec.setTypes(new ArrayList<>());
        OtpSpec.OtpMessageSpec message = new OtpSpec.OtpMessageSpec();
        message.setEn("Your otp ${otp}");
        OtpSpec.OtpTypeSpec type = new OtpSpec.OtpTypeSpec(
            TYPE_KEY,
            "[ab]{4,6}c",
            ReceiverTypeKey.PHONE_NUMBER,
            message,
            LENGTH,
            MAX_RETRIES,
            TTL,
            OTP_SENDER_ID
        );
        otpSpec.getTypes().add(type);
        otpSpecService.setOtpSpec(otpSpec);
        return new OneTimePasswordServiceImpl(
            oneTimePasswordRepository,
            oneTimePasswordMapper,
            otpSpecService,
            new CommunicationServiceMock()
        );
    }

    @Test
    public void testOtp() throws Exception {

        OneTimePasswordDTO tdo = new OneTimePasswordDTO();
        tdo.setReceiver(RECEIVER);
        tdo.setReceiverTypeKey(ReceiverTypeKey.PHONE_NUMBER);
        tdo.setTypeKey(TYPE_KEY);
        String requestJson = toJson(tdo);

        MockHttpServletRequestBuilder postContent = post("/api/one-time-password")
            .contentType(APPLICATION_JSON_UTF8)
            .content(requestJson);
        MvcResult result = restMockMvc
            .perform(postContent)
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andReturn();

        String respStr = result.getResponse().getContentAsString();
        log.info(respStr);
        OneTimePasswordDTO oneTimePasswordDTO = toDto(respStr, OneTimePasswordDTO.class);
        OneTimePassword byId = oneTimePasswordRepository.findById(oneTimePasswordDTO.getId()).get();
        long actualTtl = byId.getEndDate().toEpochMilli() - byId.getStartDate().toEpochMilli();
        Assert.assertEquals(actualTtl / 1000, TTL);
        Assert.assertEquals(byId.getReceiver(), RECEIVER);
        Assert.assertEquals(byId.getTypeKey(), TYPE_KEY);
        Assert.assertEquals(byId.getStateKey(), StateKey.ACTIVE);
        Assert.assertEquals(byId.getRetries(), MAX_RETRIES);
        Assert.assertEquals(byId.getReceiverTypeKey(), ReceiverTypeKey.PHONE_NUMBER);
    }

    @Test
    @Transactional
    public void testCheckOneTimePassword() throws Exception {

        //init DB
        OneTimePassword otp = oneTimePasswordRepository.saveAndFlush(createOtp());

        //test request
        OneTimePasswordCheckDTO dto = new OneTimePasswordCheckDTO();
        dto.setId(otp.getId());
        dto.setOtp("123");
        String requestJson = toJson(dto);

        MockHttpServletRequestBuilder postContent = post("/api/one-time-password/check")
            .contentType(APPLICATION_JSON_UTF8)
            .content(requestJson);
        MvcResult result = restMockMvc
            .perform(postContent)
            .andExpect(status().isOk())
            .andExpect(MockMvcResultMatchers.handler().methodName("checkOneTimePassword"))
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andReturn();

        OneTimePassword newOtp = oneTimePasswordRepository.getOne(otp.getId());
        assertSame(StateKey.VERIFIED, newOtp.getStateKey());
        assertEquals(newOtp.getRetries().intValue(), 1);

        log.info(result.getResponse().getContentAsString());
    }

    @Test
    @Transactional
    public void testCheckOneTimePasswordIncorrectOtp() throws Exception {

        //init DB
        OneTimePassword otp = oneTimePasswordRepository.saveAndFlush(createOtp());

        //test request
        OneTimePasswordCheckDTO dto = new OneTimePasswordCheckDTO();
        dto.setId(otp.getId());
        dto.setOtp("1234");
        String requestJson = toJson(dto);

        MockHttpServletRequestBuilder postContent = post("/api/one-time-password/check")
            .contentType(APPLICATION_JSON_UTF8)
            .content(requestJson);
        MvcResult result = restMockMvc
            .perform(postContent)
            .andExpect(status().is4xxClientError())
            .andExpect(MockMvcResultMatchers.handler().methodName("checkOneTimePassword"))
            .andExpect(jsonPath("$.title").value(containsString("Incorrect password")))
            .andExpect(status().isBadRequest())
            .andReturn();
        OneTimePassword newOtp = oneTimePasswordRepository.getOne(otp.getId());
        assertSame(StateKey.ACTIVE, newOtp.getStateKey());
        assertEquals(newOtp.getRetries().intValue(), 2);

        log.info(result.getResponse().getContentAsString());
    }

    @Test
    @Transactional
    public void testCheckOneTimePasswordNotActive() throws Exception {

        //init DB
        OneTimePassword otpToCheck = createOtp();
        otpToCheck.setStateKey(StateKey.VERIFIED);
        OneTimePassword otp = oneTimePasswordRepository.saveAndFlush(otpToCheck);

        //test request
        OneTimePasswordCheckDTO dto = new OneTimePasswordCheckDTO();
        dto.setId(otp.getId());
        dto.setOtp("123");
        String requestJson = toJson(dto);

        MockHttpServletRequestBuilder postContent = post("/api/one-time-password/check")
            .contentType(APPLICATION_JSON_UTF8)
            .content(requestJson);
        MvcResult result = restMockMvc
            .perform(postContent)
            .andExpect(status().is4xxClientError())
            .andExpect(MockMvcResultMatchers.handler().methodName("checkOneTimePassword"))
            .andExpect(jsonPath("$.title").value(containsString("Incorrect password")))
            .andExpect(status().isBadRequest())
            .andReturn();

        OneTimePassword newOtp = oneTimePasswordRepository.getOne(otp.getId());
        assertSame(StateKey.VERIFIED, newOtp.getStateKey());
        assertEquals(newOtp.getRetries().intValue(), 2);

        log.info(result.getResponse().getContentAsString());
    }

    @Test
    @Transactional
    public void testCheckOneTimePasswordIncorrectDate() throws Exception {

        //init DB
        OneTimePassword otpToCheck = createOtp();
        otpToCheck.setEndDate(Instant.ofEpochMilli(0));
        OneTimePassword otp = oneTimePasswordRepository.saveAndFlush(otpToCheck);

        //test request
        OneTimePasswordCheckDTO dto = new OneTimePasswordCheckDTO();
        dto.setId(otp.getId());
        dto.setOtp("123");
        String requestJson = toJson(dto);

        MockHttpServletRequestBuilder postContent = post("/api/one-time-password/check")
            .contentType(APPLICATION_JSON_UTF8)
            .content(requestJson);
        MvcResult result = restMockMvc
            .perform(postContent)
            .andExpect(status().is4xxClientError())
            .andExpect(MockMvcResultMatchers.handler().methodName("checkOneTimePassword"))
            .andExpect(jsonPath("$.title").value(containsString("Incorrect password")))
            .andExpect(status().isBadRequest())
            .andReturn();

        OneTimePassword newOtp = oneTimePasswordRepository.getOne(otp.getId());
        assertSame(StateKey.ACTIVE, newOtp.getStateKey());
        assertEquals(newOtp.getRetries().intValue(), 2);

        log.info(result.getResponse().getContentAsString());
    }

    @Test
    @Transactional
    public void testCheckOneTimePasswordIncorrectMaxRetries() throws Exception {

        //init DB
        OneTimePassword otpToCheck = createOtp();
        otpToCheck.setRetries(3);
        OneTimePassword otp = oneTimePasswordRepository.saveAndFlush(otpToCheck);

        //test request
        OneTimePasswordCheckDTO dto = new OneTimePasswordCheckDTO();
        dto.setId(otp.getId());
        dto.setOtp("123");
        String requestJson = toJson(dto);

        MockHttpServletRequestBuilder postContent = post("/api/one-time-password/check")
            .contentType(APPLICATION_JSON_UTF8)
            .content(requestJson);
        MvcResult result = restMockMvc
            .perform(postContent)
            .andExpect(status().is4xxClientError())
            .andExpect(MockMvcResultMatchers.handler().methodName("checkOneTimePassword"))
            .andExpect(jsonPath("$.title").value(containsString("Incorrect password")))
            .andExpect(status().isBadRequest())
            .andReturn();

        OneTimePassword newOtp = oneTimePasswordRepository.getOne(otp.getId());
        assertSame(StateKey.ACTIVE, newOtp.getStateKey());
        assertEquals(newOtp.getRetries().intValue(), 4);

        log.info(result.getResponse().getContentAsString());
    }

    private String toJson(Object tdo) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
        ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter();
        return ow.writeValueAsString(tdo);
    }

    private <T> T toDto(String response, Class<T> cls) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(response, cls);
    }

    /**
     * Creates the otp entity.
     *
     * @return OneTimePassword entity
     */
    private OneTimePassword createOtp() {
        OneTimePassword oneTimePassword = new OneTimePassword();
        oneTimePassword.setPasswordHash(DigestUtils.sha256Hex("123"));
        oneTimePassword.setEndDate(Instant.MAX);
        oneTimePassword.setReceiver("receiver");
        oneTimePassword.setReceiverTypeKey(ReceiverTypeKey.IP);
        oneTimePassword.setStartDate(Instant.MIN);
        oneTimePassword.setTypeKey("TYPE1");
        oneTimePassword.setStateKey(StateKey.ACTIVE);
        oneTimePassword.setRetries(1);

        return oneTimePassword;
    }

}
