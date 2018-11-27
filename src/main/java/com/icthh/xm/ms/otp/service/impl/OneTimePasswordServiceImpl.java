package com.icthh.xm.ms.otp.service.impl;

import com.icthh.xm.ms.otp.client.domain.CommunicationMessage;
import com.icthh.xm.ms.otp.client.domain.Receiver;
import com.icthh.xm.ms.otp.client.domain.Sender;
import com.icthh.xm.ms.otp.config.ApplicationProperties;
import com.icthh.xm.ms.otp.domain.OneTimePassword;
import com.icthh.xm.ms.otp.domain.OtpSpec;
import com.icthh.xm.ms.otp.domain.enumeration.StateKey;
import com.icthh.xm.ms.otp.repository.OneTimePasswordRepository;
import com.icthh.xm.ms.otp.security.CommunicationService;
import com.icthh.xm.ms.otp.service.OneTimePasswordService;
import com.icthh.xm.ms.otp.service.OtpSpecService;
import com.icthh.xm.ms.otp.service.dto.OneTimePasswordCheckDTO;
import com.icthh.xm.ms.otp.service.dto.OneTimePasswordDTO;
import com.icthh.xm.ms.otp.service.mapper.OneTimePasswordMapper;
import com.icthh.xm.ms.otp.web.rest.errors.OtpInvalidPasswordException;
import com.mifmif.common.regex.Generex;

import java.net.URI;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import static org.springframework.http.HttpMethod.POST;

/**
 * Service Implementation for managing OneTimePassword.
 */
@Service
@Slf4j
@Transactional
public class OneTimePasswordServiceImpl implements OneTimePasswordService {

    private final OtpSpecService otpSpecService;

    private final OneTimePasswordRepository oneTimePasswordRepository;

    private final OneTimePasswordMapper oneTimePasswordMapper;
    private final ApplicationProperties applicationProperties;

    private final RestTemplate loadBalancedRestTemplate;
    private final CommunicationService communicationService;

    public OneTimePasswordServiceImpl(OneTimePasswordRepository oneTimePasswordRepository,
                                      OneTimePasswordMapper oneTimePasswordMapper,
                                      OtpSpecService otpSpecService,
                                      @Qualifier("loadBalancedRestTemplate") RestTemplate template,
                                      CommunicationService communicationService,
                                      ApplicationProperties applicationProperties) {
        this.oneTimePasswordRepository = oneTimePasswordRepository;
        this.oneTimePasswordMapper = oneTimePasswordMapper;
        this.otpSpecService = otpSpecService;
        this.loadBalancedRestTemplate = template;
        this.communicationService = communicationService;
        this.applicationProperties = applicationProperties;
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

        //generate otp
        Generex generex = new Generex(oneType.getPattern());
        String randomPasswrd = generex.random();

        //build domain
        OneTimePassword otp = getOneTimePassword(oneTimePasswordDTO, oneType, randomPasswrd);

        String message = oneType.getMessage().getEn().replaceAll("\\$\\{otp}", randomPasswrd);
        oneTimePasswordRepository.saveAndFlush(otp);

        sendOneTimePassword(message, otp.getReceiver(), oneType.getOtpSenderId());
        return oneTimePasswordMapper.toDto(otp);
    }

    private void sendOneTimePassword(String message, String receiver, String senderId) {
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, communicationService.getSystemToken());
        headers.setContentType(MediaType.APPLICATION_JSON);
        String url = applicationProperties.getCommunicationUrl()
            + "/tmf-api/communicationManagement/v2/communicationMessage/send";
        CommunicationMessage body = new CommunicationMessage();
        body.setContent(message);
        body.setType("SMS");
        body.setSender(new Sender(senderId));
        body.setReceiver(new ArrayList<>());
        body.getReceiver().add(new Receiver(receiver, receiver));
        RequestEntity<Object> request = new RequestEntity<>(body, headers, POST, URI.create(url));
        loadBalancedRestTemplate.exchange(request, Object.class);
    }

    private OneTimePassword getOneTimePassword(
        OneTimePasswordDTO oneTimePasswordDTO,
        OtpSpec.OtpTypeSpec oneType,
        String randomPasswrd
    ) {
        String sha256hex = DigestUtils.sha256Hex(randomPasswrd);
        OneTimePassword oneTimePassword = new OneTimePassword();
        Instant startDate = Instant.now();
        Instant endDate = startDate.plusSeconds(oneType.getTtl());
        oneTimePassword.startDate(startDate);
        oneTimePassword.setEndDate(endDate);
        oneTimePassword.setPasswordHash(randomPasswrd);
        oneTimePassword.setReceiverTypeKey(oneType.getReceiverTypeKey());
        oneTimePassword.setRetries(oneType.getMaxRetries());
        oneTimePassword.setTypeKey(oneTimePasswordDTO.getTypeKey());
        oneTimePassword.setReceiver(oneTimePasswordDTO.getReceiver());
        oneTimePassword.setPasswordHash(sha256hex);
        oneTimePassword.setStateKey(StateKey.ACTIVE);
        return oneTimePassword;
    }

    @Override
    public void check(OneTimePasswordCheckDTO oneTimePasswordDTO) {

        OneTimePassword passwordEntity = oneTimePasswordRepository.getOne(oneTimePasswordDTO.getId());

        if (passwordEntity.getStateKey() != StateKey.ACTIVE
            || passwordEntity.getEndDate().isBefore(Instant.now())
            || passwordEntity.getRetries() >= getOneTypeSpec(passwordEntity.getTypeKey()).getMaxRetries()
            || !passwordEntity.getPasswordHash().equals(DigestUtils.sha256Hex(oneTimePasswordDTO.getOtp()))) {

            //if not - retries+
            int retries = passwordEntity.getRetries();
            passwordEntity.setRetries(++retries);
            oneTimePasswordRepository.save(passwordEntity);
            throw new OtpInvalidPasswordException();
        } else {
            passwordEntity.setStateKey(StateKey.VERIFIED);
            oneTimePasswordRepository.save(passwordEntity);
        }
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
