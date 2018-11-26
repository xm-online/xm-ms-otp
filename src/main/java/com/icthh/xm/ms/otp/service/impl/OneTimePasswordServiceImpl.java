package com.icthh.xm.ms.otp.service.impl;

import com.google.common.hash.Hashing;
import com.icthh.xm.ms.otp.client.domain.CommunicationMessage;
import com.icthh.xm.ms.otp.client.domain.Receiver;
import com.icthh.xm.ms.otp.domain.OneTimePassword;
import com.icthh.xm.ms.otp.domain.OtpSpec;
import com.icthh.xm.ms.otp.repository.OneTimePasswordRepository;
import com.icthh.xm.ms.otp.security.CommunicationService;
import com.icthh.xm.ms.otp.service.OneTimePasswordService;
import com.icthh.xm.ms.otp.service.OtpSpecService;
import com.icthh.xm.ms.otp.service.dto.OneTimePasswordDTO;
import com.icthh.xm.ms.otp.service.mapper.OneTimePasswordMapper;
import com.mifmif.common.regex.Generex;
import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

import static org.springframework.http.HttpMethod.POST;

/**
 * Service Implementation for managing OneTimePassword.
 */
@Service
@Transactional
public class OneTimePasswordServiceImpl implements OneTimePasswordService {

    private final Logger log = LoggerFactory.getLogger(OneTimePasswordServiceImpl.class);

    private final OtpSpecService otpSpecService;

    private final OneTimePasswordRepository oneTimePasswordRepository;

    private final OneTimePasswordMapper oneTimePasswordMapper;

    private final RestTemplate loadBalancedRestTemplate;
    private final CommunicationService communicationService;

    public OneTimePasswordServiceImpl(OneTimePasswordRepository oneTimePasswordRepository,
                                      OneTimePasswordMapper oneTimePasswordMapper,
                                      OtpSpecService otpSpecService,
                                      @Qualifier("loadBalancedRestTemplate") RestTemplate loadBalancedRestTemplate,
                                      CommunicationService communicationService) {
        this.oneTimePasswordRepository = oneTimePasswordRepository;
        this.oneTimePasswordMapper = oneTimePasswordMapper;
        this.otpSpecService = otpSpecService;
        this.loadBalancedRestTemplate = loadBalancedRestTemplate;
        this.communicationService = communicationService;
    }

    /**
     * Save a oneTimePassword.
     *
     * @param oneTimePasswordDTO the entity to generate
     * @return the persisted entity
     */
    @Override
    @Transactional
    public OneTimePasswordDTO generate(OneTimePasswordDTO oneTimePasswordDTO) {
        log.debug("Request to generate OneTimePassword : {}", oneTimePasswordDTO);

        OtpSpec.OtpTypeSpec oneType = getOneTypeSpec(oneTimePasswordDTO.getTypeKey());

        Generex generex = new Generex(oneType.getPattern());
        String randomStr = generex.random();
        //3. generate
        OneTimePassword oneTimePassword = new OneTimePassword();
        long now = new Date().getTime();
        Instant startDate = Instant.ofEpochMilli(now);
        Instant endDate = Instant.ofEpochMilli(now + oneType.getTtl()*1000);
        oneTimePassword.startDate(startDate);
        oneTimePassword.setEndDate(endDate);
        oneTimePassword.setPasswordHash(randomStr);
        oneTimePassword.setReceiverTypeKey(oneType.getReceiverTypeKey());
        oneTimePassword.setRetries(oneType.getMaxRetries());
        oneTimePassword.setTypeKey(oneTimePasswordDTO.getTypeKey());
        oneTimePassword.setReceiver(oneTimePasswordDTO.getReceiver());
        oneTimePassword.setStateKey("ACTIVE");

        String sha256hex = Arrays.toString(DigestUtils.sha256(randomStr));

        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, communicationService.getSystemToken());
        headers.setContentType(MediaType.APPLICATION_JSON);

        String url = "http://communication/tmf-api/communicationManagement/v2/communicationMessage/send";
        oneTimePassword.setPasswordHash(sha256hex);
        CommunicationMessage body = new CommunicationMessage();
        String mesage = oneType.getMessage().getEn().replaceAll("\\$\\{otp}", randomStr);
        body.setContent(mesage);
        body.setReceiver(new ArrayList<>());
        body.getReceiver().add(new Receiver(oneTimePassword.getReceiver()));
        RequestEntity<Object> objectRequestEntity = new RequestEntity<>(body, headers, POST, URI.create(url));
        loadBalancedRestTemplate.exchange(objectRequestEntity, Object.class);

        //4. send through communication ms (bean loadBalancedRestTemplate)
        oneTimePasswordRepository.saveAndFlush(oneTimePassword);
        return oneTimePasswordMapper.toDto(oneTimePassword);
    }

    private OtpSpec.OtpTypeSpec getOneTypeSpec(String typeKey) {
        List<OtpSpec.OtpTypeSpec> types = otpSpecService.getOtpSpec().getTypes();
        OtpSpec.OtpTypeSpec oneType = null;
        for (OtpSpec.OtpTypeSpec type : types) {
            if (type.getKey().equals(typeKey)) {
                oneType = type;
            }
        }
        if (oneType == null) {
            throw new IllegalArgumentException(
                String.format("Profile %s not found", typeKey)
            );
        }
        return oneType;
    }

    /**
     * Get all the oneTimePasswords.
     *
     * @return the list of entities
     */
    @Override
    @Transactional(readOnly = true)
    public List<OneTimePasswordDTO> findAll() {
        log.debug("Request to get all OneTimePasswords");
        return oneTimePasswordRepository.findAll().stream()
            .map(oneTimePasswordMapper::toDto)
            .collect(Collectors.toCollection(LinkedList::new));
    }


    /**
     * Get one oneTimePassword by id.
     *
     * @param id the id of the entity
     * @return the entity
     */
    @Override
    @Transactional(readOnly = true)
    public Optional<OneTimePasswordDTO> findOne(Long id) {
        log.debug("Request to get OneTimePassword : {}", id);
        return oneTimePasswordRepository.findById(id)
            .map(oneTimePasswordMapper::toDto);
    }

    /**
     * Delete the oneTimePassword by id.
     *
     * @param id the id of the entity
     */
    @Override
    public void delete(Long id) {
        log.debug("Request to delete OneTimePassword : {}", id);
        oneTimePasswordRepository.deleteById(id);
    }
}
