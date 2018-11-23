package com.icthh.xm.ms.otp.web.rest;

import com.icthh.xm.ms.otp.OtpApp;

import com.icthh.xm.ms.otp.config.SecurityBeanOverrideConfiguration;

import com.icthh.xm.ms.otp.domain.OneTimePassword;
import com.icthh.xm.ms.otp.domain.enumeration.ReceiverTypeKey;
import com.icthh.xm.ms.otp.repository.OneTimePasswordRepository;
import com.icthh.xm.ms.otp.service.OneTimePasswordService;
import com.icthh.xm.ms.otp.service.dto.OneTimePasswordDTO;
import com.icthh.xm.ms.otp.service.mapper.OneTimePasswordMapper;
import com.icthh.xm.ms.otp.web.rest.errors.ExceptionTranslator;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;


import static com.icthh.xm.ms.otp.web.rest.TestUtil.createFormattingConversionService;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test class for the OneTimePasswordResource REST controller.
 *
 * @see OneTimePasswordResource
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {SecurityBeanOverrideConfiguration.class, OtpApp.class})
public class OneTimePasswordResourceIntTest {

    private static final String DEFAULT_RECEIVER = "AAAAAAAAAA";
    private static final String UPDATED_RECEIVER = "BBBBBBBBBB";

    private static final ReceiverTypeKey DEFAULT_RECEIVER_TYPE_KEY = ReceiverTypeKey.USER_ID;
    private static final ReceiverTypeKey UPDATED_RECEIVER_TYPE_KEY = ReceiverTypeKey.EMAIL;

    private static final String DEFAULT_TYPE_KEY = "AAAAAAAAAA";
    private static final String UPDATED_TYPE_KEY = "BBBBBBBBBB";

    private static final String DEFAULT_STATE_KEY = "AAAAAAAAAA";
    private static final String UPDATED_STATE_KEY = "BBBBBBBBBB";

    private static final Integer DEFAULT_RETRIES = 1;
    private static final Integer UPDATED_RETRIES = 2;

    private static final Instant DEFAULT_START_DATE = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_START_DATE = Instant.now().truncatedTo(ChronoUnit.MILLIS);

    private static final Instant DEFAULT_END_DATE = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_END_DATE = Instant.now().truncatedTo(ChronoUnit.MILLIS);

    private static final String DEFAULT_PASSWORD_HASH = "AAAAAAAAAA";
    private static final String UPDATED_PASSWORD_HASH = "BBBBBBBBBB";

    @Autowired
    private OneTimePasswordRepository oneTimePasswordRepository;

    @Autowired
    private OneTimePasswordMapper oneTimePasswordMapper;

    @Autowired
    private OneTimePasswordService oneTimePasswordService;

    @Autowired
    private MappingJackson2HttpMessageConverter jacksonMessageConverter;

    @Autowired
    private PageableHandlerMethodArgumentResolver pageableArgumentResolver;

    @Autowired
    private ExceptionTranslator exceptionTranslator;

    @Autowired
    private EntityManager em;

    private MockMvc restOneTimePasswordMockMvc;

    private OneTimePassword oneTimePassword;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        final OneTimePasswordResource oneTimePasswordResource = new OneTimePasswordResource(oneTimePasswordService);
        this.restOneTimePasswordMockMvc = MockMvcBuilders.standaloneSetup(oneTimePasswordResource)
            .setCustomArgumentResolvers(pageableArgumentResolver)
            .setControllerAdvice(exceptionTranslator)
            .setConversionService(createFormattingConversionService())
            .setMessageConverters(jacksonMessageConverter).build();
    }

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static OneTimePassword createEntity(EntityManager em) {
        OneTimePassword oneTimePassword = new OneTimePassword()
            .receiver(DEFAULT_RECEIVER)
            .receiverTypeKey(DEFAULT_RECEIVER_TYPE_KEY)
            .typeKey(DEFAULT_TYPE_KEY)
            .stateKey(DEFAULT_STATE_KEY)
            .retries(DEFAULT_RETRIES)
            .startDate(DEFAULT_START_DATE)
            .endDate(DEFAULT_END_DATE)
            .passwordHash(DEFAULT_PASSWORD_HASH);
        return oneTimePassword;
    }

    @Before
    public void initTest() {
        oneTimePassword = createEntity(em);
    }

    @Test
    @Transactional
    public void createOneTimePassword() throws Exception {
        int databaseSizeBeforeCreate = oneTimePasswordRepository.findAll().size();

        // Create the OneTimePassword
        OneTimePasswordDTO oneTimePasswordDTO = oneTimePasswordMapper.toDto(oneTimePassword);
        restOneTimePasswordMockMvc.perform(post("/api/one-time-passwords")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(oneTimePasswordDTO)))
            .andExpect(status().isCreated());

        // Validate the OneTimePassword in the database
        List<OneTimePassword> oneTimePasswordList = oneTimePasswordRepository.findAll();
        assertThat(oneTimePasswordList).hasSize(databaseSizeBeforeCreate + 1);
        OneTimePassword testOneTimePassword = oneTimePasswordList.get(oneTimePasswordList.size() - 1);
        assertThat(testOneTimePassword.getReceiver()).isEqualTo(DEFAULT_RECEIVER);
        assertThat(testOneTimePassword.getReceiverTypeKey()).isEqualTo(DEFAULT_RECEIVER_TYPE_KEY);
        assertThat(testOneTimePassword.getTypeKey()).isEqualTo(DEFAULT_TYPE_KEY);
        assertThat(testOneTimePassword.getStateKey()).isEqualTo(DEFAULT_STATE_KEY);
        assertThat(testOneTimePassword.getRetries()).isEqualTo(DEFAULT_RETRIES);
        assertThat(testOneTimePassword.getStartDate()).isEqualTo(DEFAULT_START_DATE);
        assertThat(testOneTimePassword.getEndDate()).isEqualTo(DEFAULT_END_DATE);
        assertThat(testOneTimePassword.getPasswordHash()).isEqualTo(DEFAULT_PASSWORD_HASH);
    }

    @Test
    @Transactional
    public void createOneTimePasswordWithExistingId() throws Exception {
        int databaseSizeBeforeCreate = oneTimePasswordRepository.findAll().size();

        // Create the OneTimePassword with an existing ID
        oneTimePassword.setId(1L);
        OneTimePasswordDTO oneTimePasswordDTO = oneTimePasswordMapper.toDto(oneTimePassword);

        // An entity with an existing ID cannot be created, so this API call must fail
        restOneTimePasswordMockMvc.perform(post("/api/one-time-passwords")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(oneTimePasswordDTO)))
            .andExpect(status().isBadRequest());

        // Validate the OneTimePassword in the database
        List<OneTimePassword> oneTimePasswordList = oneTimePasswordRepository.findAll();
        assertThat(oneTimePasswordList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    public void checkReceiverIsRequired() throws Exception {
        int databaseSizeBeforeTest = oneTimePasswordRepository.findAll().size();
        // set the field null
        oneTimePassword.setReceiver(null);

        // Create the OneTimePassword, which fails.
        OneTimePasswordDTO oneTimePasswordDTO = oneTimePasswordMapper.toDto(oneTimePassword);

        restOneTimePasswordMockMvc.perform(post("/api/one-time-passwords")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(oneTimePasswordDTO)))
            .andExpect(status().isBadRequest());

        List<OneTimePassword> oneTimePasswordList = oneTimePasswordRepository.findAll();
        assertThat(oneTimePasswordList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkReceiverTypeKeyIsRequired() throws Exception {
        int databaseSizeBeforeTest = oneTimePasswordRepository.findAll().size();
        // set the field null
        oneTimePassword.setReceiverTypeKey(null);

        // Create the OneTimePassword, which fails.
        OneTimePasswordDTO oneTimePasswordDTO = oneTimePasswordMapper.toDto(oneTimePassword);

        restOneTimePasswordMockMvc.perform(post("/api/one-time-passwords")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(oneTimePasswordDTO)))
            .andExpect(status().isBadRequest());

        List<OneTimePassword> oneTimePasswordList = oneTimePasswordRepository.findAll();
        assertThat(oneTimePasswordList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkTypeKeyIsRequired() throws Exception {
        int databaseSizeBeforeTest = oneTimePasswordRepository.findAll().size();
        // set the field null
        oneTimePassword.setTypeKey(null);

        // Create the OneTimePassword, which fails.
        OneTimePasswordDTO oneTimePasswordDTO = oneTimePasswordMapper.toDto(oneTimePassword);

        restOneTimePasswordMockMvc.perform(post("/api/one-time-passwords")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(oneTimePasswordDTO)))
            .andExpect(status().isBadRequest());

        List<OneTimePassword> oneTimePasswordList = oneTimePasswordRepository.findAll();
        assertThat(oneTimePasswordList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkStateKeyIsRequired() throws Exception {
        int databaseSizeBeforeTest = oneTimePasswordRepository.findAll().size();
        // set the field null
        oneTimePassword.setStateKey(null);

        // Create the OneTimePassword, which fails.
        OneTimePasswordDTO oneTimePasswordDTO = oneTimePasswordMapper.toDto(oneTimePassword);

        restOneTimePasswordMockMvc.perform(post("/api/one-time-passwords")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(oneTimePasswordDTO)))
            .andExpect(status().isBadRequest());

        List<OneTimePassword> oneTimePasswordList = oneTimePasswordRepository.findAll();
        assertThat(oneTimePasswordList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkRetriesIsRequired() throws Exception {
        int databaseSizeBeforeTest = oneTimePasswordRepository.findAll().size();
        // set the field null
        oneTimePassword.setRetries(null);

        // Create the OneTimePassword, which fails.
        OneTimePasswordDTO oneTimePasswordDTO = oneTimePasswordMapper.toDto(oneTimePassword);

        restOneTimePasswordMockMvc.perform(post("/api/one-time-passwords")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(oneTimePasswordDTO)))
            .andExpect(status().isBadRequest());

        List<OneTimePassword> oneTimePasswordList = oneTimePasswordRepository.findAll();
        assertThat(oneTimePasswordList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkStartDateIsRequired() throws Exception {
        int databaseSizeBeforeTest = oneTimePasswordRepository.findAll().size();
        // set the field null
        oneTimePassword.setStartDate(null);

        // Create the OneTimePassword, which fails.
        OneTimePasswordDTO oneTimePasswordDTO = oneTimePasswordMapper.toDto(oneTimePassword);

        restOneTimePasswordMockMvc.perform(post("/api/one-time-passwords")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(oneTimePasswordDTO)))
            .andExpect(status().isBadRequest());

        List<OneTimePassword> oneTimePasswordList = oneTimePasswordRepository.findAll();
        assertThat(oneTimePasswordList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkEndDateIsRequired() throws Exception {
        int databaseSizeBeforeTest = oneTimePasswordRepository.findAll().size();
        // set the field null
        oneTimePassword.setEndDate(null);

        // Create the OneTimePassword, which fails.
        OneTimePasswordDTO oneTimePasswordDTO = oneTimePasswordMapper.toDto(oneTimePassword);

        restOneTimePasswordMockMvc.perform(post("/api/one-time-passwords")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(oneTimePasswordDTO)))
            .andExpect(status().isBadRequest());

        List<OneTimePassword> oneTimePasswordList = oneTimePasswordRepository.findAll();
        assertThat(oneTimePasswordList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkPasswordHashIsRequired() throws Exception {
        int databaseSizeBeforeTest = oneTimePasswordRepository.findAll().size();
        // set the field null
        oneTimePassword.setPasswordHash(null);

        // Create the OneTimePassword, which fails.
        OneTimePasswordDTO oneTimePasswordDTO = oneTimePasswordMapper.toDto(oneTimePassword);

        restOneTimePasswordMockMvc.perform(post("/api/one-time-passwords")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(oneTimePasswordDTO)))
            .andExpect(status().isBadRequest());

        List<OneTimePassword> oneTimePasswordList = oneTimePasswordRepository.findAll();
        assertThat(oneTimePasswordList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void getAllOneTimePasswords() throws Exception {
        // Initialize the database
        oneTimePasswordRepository.saveAndFlush(oneTimePassword);

        // Get all the oneTimePasswordList
        restOneTimePasswordMockMvc.perform(get("/api/one-time-passwords?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(oneTimePassword.getId().intValue())))
            .andExpect(jsonPath("$.[*].receiver").value(hasItem(DEFAULT_RECEIVER.toString())))
            .andExpect(jsonPath("$.[*].receiverTypeKey").value(hasItem(DEFAULT_RECEIVER_TYPE_KEY.toString())))
            .andExpect(jsonPath("$.[*].typeKey").value(hasItem(DEFAULT_TYPE_KEY.toString())))
            .andExpect(jsonPath("$.[*].stateKey").value(hasItem(DEFAULT_STATE_KEY.toString())))
            .andExpect(jsonPath("$.[*].retries").value(hasItem(DEFAULT_RETRIES)))
            .andExpect(jsonPath("$.[*].startDate").value(hasItem(DEFAULT_START_DATE.toString())))
            .andExpect(jsonPath("$.[*].endDate").value(hasItem(DEFAULT_END_DATE.toString())))
            .andExpect(jsonPath("$.[*].passwordHash").value(hasItem(DEFAULT_PASSWORD_HASH.toString())));
    }

    @Test
    @Transactional
    public void getOneTimePassword() throws Exception {
        // Initialize the database
        oneTimePasswordRepository.saveAndFlush(oneTimePassword);

        // Get the oneTimePassword
        restOneTimePasswordMockMvc.perform(get("/api/one-time-passwords/{id}", oneTimePassword.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.id").value(oneTimePassword.getId().intValue()))
            .andExpect(jsonPath("$.receiver").value(DEFAULT_RECEIVER.toString()))
            .andExpect(jsonPath("$.receiverTypeKey").value(DEFAULT_RECEIVER_TYPE_KEY.toString()))
            .andExpect(jsonPath("$.typeKey").value(DEFAULT_TYPE_KEY.toString()))
            .andExpect(jsonPath("$.stateKey").value(DEFAULT_STATE_KEY.toString()))
            .andExpect(jsonPath("$.retries").value(DEFAULT_RETRIES))
            .andExpect(jsonPath("$.startDate").value(DEFAULT_START_DATE.toString()))
            .andExpect(jsonPath("$.endDate").value(DEFAULT_END_DATE.toString()))
            .andExpect(jsonPath("$.passwordHash").value(DEFAULT_PASSWORD_HASH.toString()));
    }

    @Test
    @Transactional
    public void getNonExistingOneTimePassword() throws Exception {
        // Get the oneTimePassword
        restOneTimePasswordMockMvc.perform(get("/api/one-time-passwords/{id}", Long.MAX_VALUE))
            .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updateOneTimePassword() throws Exception {
        // Initialize the database
        oneTimePasswordRepository.saveAndFlush(oneTimePassword);

        int databaseSizeBeforeUpdate = oneTimePasswordRepository.findAll().size();

        // Update the oneTimePassword
        OneTimePassword updatedOneTimePassword = oneTimePasswordRepository.findById(oneTimePassword.getId()).get();
        // Disconnect from session so that the updates on updatedOneTimePassword are not directly saved in db
        em.detach(updatedOneTimePassword);
        updatedOneTimePassword
            .receiver(UPDATED_RECEIVER)
            .receiverTypeKey(UPDATED_RECEIVER_TYPE_KEY)
            .typeKey(UPDATED_TYPE_KEY)
            .stateKey(UPDATED_STATE_KEY)
            .retries(UPDATED_RETRIES)
            .startDate(UPDATED_START_DATE)
            .endDate(UPDATED_END_DATE)
            .passwordHash(UPDATED_PASSWORD_HASH);
        OneTimePasswordDTO oneTimePasswordDTO = oneTimePasswordMapper.toDto(updatedOneTimePassword);

        restOneTimePasswordMockMvc.perform(put("/api/one-time-passwords")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(oneTimePasswordDTO)))
            .andExpect(status().isOk());

        // Validate the OneTimePassword in the database
        List<OneTimePassword> oneTimePasswordList = oneTimePasswordRepository.findAll();
        assertThat(oneTimePasswordList).hasSize(databaseSizeBeforeUpdate);
        OneTimePassword testOneTimePassword = oneTimePasswordList.get(oneTimePasswordList.size() - 1);
        assertThat(testOneTimePassword.getReceiver()).isEqualTo(UPDATED_RECEIVER);
        assertThat(testOneTimePassword.getReceiverTypeKey()).isEqualTo(UPDATED_RECEIVER_TYPE_KEY);
        assertThat(testOneTimePassword.getTypeKey()).isEqualTo(UPDATED_TYPE_KEY);
        assertThat(testOneTimePassword.getStateKey()).isEqualTo(UPDATED_STATE_KEY);
        assertThat(testOneTimePassword.getRetries()).isEqualTo(UPDATED_RETRIES);
        assertThat(testOneTimePassword.getStartDate()).isEqualTo(UPDATED_START_DATE);
        assertThat(testOneTimePassword.getEndDate()).isEqualTo(UPDATED_END_DATE);
        assertThat(testOneTimePassword.getPasswordHash()).isEqualTo(UPDATED_PASSWORD_HASH);
    }

    @Test
    @Transactional
    public void updateNonExistingOneTimePassword() throws Exception {
        int databaseSizeBeforeUpdate = oneTimePasswordRepository.findAll().size();

        // Create the OneTimePassword
        OneTimePasswordDTO oneTimePasswordDTO = oneTimePasswordMapper.toDto(oneTimePassword);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restOneTimePasswordMockMvc.perform(put("/api/one-time-passwords")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(oneTimePasswordDTO)))
            .andExpect(status().isBadRequest());

        // Validate the OneTimePassword in the database
        List<OneTimePassword> oneTimePasswordList = oneTimePasswordRepository.findAll();
        assertThat(oneTimePasswordList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    public void deleteOneTimePassword() throws Exception {
        // Initialize the database
        oneTimePasswordRepository.saveAndFlush(oneTimePassword);

        int databaseSizeBeforeDelete = oneTimePasswordRepository.findAll().size();

        // Get the oneTimePassword
        restOneTimePasswordMockMvc.perform(delete("/api/one-time-passwords/{id}", oneTimePassword.getId())
            .accept(TestUtil.APPLICATION_JSON_UTF8))
            .andExpect(status().isOk());

        // Validate the database is empty
        List<OneTimePassword> oneTimePasswordList = oneTimePasswordRepository.findAll();
        assertThat(oneTimePasswordList).hasSize(databaseSizeBeforeDelete - 1);
    }

    @Test
    @Transactional
    public void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(OneTimePassword.class);
        OneTimePassword oneTimePassword1 = new OneTimePassword();
        oneTimePassword1.setId(1L);
        OneTimePassword oneTimePassword2 = new OneTimePassword();
        oneTimePassword2.setId(oneTimePassword1.getId());
        assertThat(oneTimePassword1).isEqualTo(oneTimePassword2);
        oneTimePassword2.setId(2L);
        assertThat(oneTimePassword1).isNotEqualTo(oneTimePassword2);
        oneTimePassword1.setId(null);
        assertThat(oneTimePassword1).isNotEqualTo(oneTimePassword2);
    }

    @Test
    @Transactional
    public void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(OneTimePasswordDTO.class);
        OneTimePasswordDTO oneTimePasswordDTO1 = new OneTimePasswordDTO();
        oneTimePasswordDTO1.setId(1L);
        OneTimePasswordDTO oneTimePasswordDTO2 = new OneTimePasswordDTO();
        assertThat(oneTimePasswordDTO1).isNotEqualTo(oneTimePasswordDTO2);
        oneTimePasswordDTO2.setId(oneTimePasswordDTO1.getId());
        assertThat(oneTimePasswordDTO1).isEqualTo(oneTimePasswordDTO2);
        oneTimePasswordDTO2.setId(2L);
        assertThat(oneTimePasswordDTO1).isNotEqualTo(oneTimePasswordDTO2);
        oneTimePasswordDTO1.setId(null);
        assertThat(oneTimePasswordDTO1).isNotEqualTo(oneTimePasswordDTO2);
    }

    @Test
    @Transactional
    public void testEntityFromId() {
        assertThat(oneTimePasswordMapper.fromId(42L).getId()).isEqualTo(42);
        assertThat(oneTimePasswordMapper.fromId(null)).isNull();
    }
}
