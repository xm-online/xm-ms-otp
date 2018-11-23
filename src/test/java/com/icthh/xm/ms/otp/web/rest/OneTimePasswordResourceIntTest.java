package com.icthh.xm.ms.otp.web.rest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.icthh.xm.ms.otp.OtpApp;
import com.icthh.xm.ms.otp.config.SecurityBeanOverrideConfiguration;
import com.icthh.xm.ms.otp.config.tenant.WebappTenantOverrideConfiguration;
import com.icthh.xm.ms.otp.domain.enumeration.ReceiverTypeKey;
import com.icthh.xm.ms.otp.service.dto.OneTimePasswordDTO;
import com.icthh.xm.ms.otp.service.impl.OneTimePasswordServiceImpl;
import com.icthh.xm.ms.otp.web.rest.errors.ExceptionTranslator;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
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
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.Validator;

import java.nio.charset.Charset;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
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

    public static final MediaType APPLICATION_JSON_UTF8 = new MediaType(
        MediaType.APPLICATION_JSON.getType(),
        MediaType.APPLICATION_JSON.getSubtype(),
        Charset.forName("utf8")
    );

    private MockMvc restMockMvc;

    @Autowired
    private PageableHandlerMethodArgumentResolver pageableArgumentResolver;

    @Autowired
    private ExceptionTranslator exceptionTranslator;

    @Autowired
    private MappingJackson2HttpMessageConverter jacksonMessageConverter;

    @Autowired
    private Validator validator;


    @SneakyThrows
    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        OneTimePasswordServiceImpl oneTimePswd = new OneTimePasswordServiceImpl(null, null);
        OneTimePasswordResource otp = new OneTimePasswordResource(oneTimePswd);
        this.restMockMvc = MockMvcBuilders.standaloneSetup(otp)
            .setCustomArgumentResolvers(pageableArgumentResolver)
            .setControllerAdvice(exceptionTranslator)
            .setValidator(validator)
            .setMessageConverters(jacksonMessageConverter).build();
    }

    @Test
    public void getUserSubscriptionInformation() throws Exception {

        OneTimePasswordDTO tdo = new OneTimePasswordDTO();
        tdo.setId(23L);
        tdo.setReceiver("receiver");
        tdo.setReceiverTypeKey(ReceiverTypeKey.USER_ID);
        tdo.setTypeKey("typeKey");
        String requestJson = toJson(tdo);

        MockHttpServletRequestBuilder postContent = post("/api/one-time-password")
            .contentType(APPLICATION_JSON_UTF8)
            .content(requestJson);
        MvcResult result = restMockMvc
            .perform(postContent)
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andReturn();

        log.info(result.getResponse().getContentAsString());
    }

    private String toJson(OneTimePasswordDTO tdo) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
        ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter();
        return ow.writeValueAsString(tdo );
    }

}
