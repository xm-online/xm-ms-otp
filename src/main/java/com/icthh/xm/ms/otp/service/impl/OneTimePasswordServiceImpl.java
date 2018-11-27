package com.icthh.xm.ms.otp.service.impl;

import com.icthh.xm.ms.otp.domain.OneTimePassword;
import com.icthh.xm.ms.otp.domain.OtpSpec;
import com.icthh.xm.ms.otp.repository.OneTimePasswordRepository;
import com.icthh.xm.ms.otp.security.CommunicationService;
import com.icthh.xm.ms.otp.service.OneTimePasswordService;
import com.icthh.xm.ms.otp.service.OtpSpecService;
import com.icthh.xm.ms.otp.service.dto.OneTimePasswordDTO;
import com.icthh.xm.ms.otp.service.mapper.OneTimePasswordMapper;
import com.mifmif.common.regex.Generex;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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

    private final CommunicationService communicationService;

    public OneTimePasswordServiceImpl(OneTimePasswordRepository oneTimePasswordRepository,
                                      OneTimePasswordMapper oneTimePasswordMapper,
                                      OtpSpecService otpSpecService,
                                      CommunicationService communicationService) {
        this.oneTimePasswordRepository = oneTimePasswordRepository;
        this.oneTimePasswordMapper = oneTimePasswordMapper;
        this.otpSpecService = otpSpecService;
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
        OtpSpec.OtpTypeSpec oneType = otpSpecService.getOneTypeSpec(oneTimePasswordDTO.getTypeKey());

        //generate otp
        Generex generex = new Generex(oneType.getPattern());
        String randomPasswrd = generex.random();

        //build domain
        OneTimePassword otp = getOneTimePassword(oneTimePasswordDTO, oneType, randomPasswrd);

        String message = oneType.getMessage().getEn().replaceAll("\\$\\{otp}", randomPasswrd);
        oneTimePasswordRepository.saveAndFlush(otp);

        communicationService.sendOneTimePassword(message, otp.getReceiver(), oneType.getOtpSenderId());
        return oneTimePasswordMapper.toDto(otp);
    }


    private OneTimePassword getOneTimePassword(OneTimePasswordDTO oneTimePasswordDTO,
                                               OtpSpec.OtpTypeSpec oneType,
                                               String randomPasswrd) {
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
        oneTimePassword.setStateKey("ACTIVE");
        return oneTimePassword;
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
