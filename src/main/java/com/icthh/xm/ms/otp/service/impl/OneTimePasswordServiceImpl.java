package com.icthh.xm.ms.otp.service.impl;

import com.icthh.xm.commons.config.client.repository.TenantConfigRepository;
import com.icthh.xm.ms.otp.domain.OtpSpec;
import com.icthh.xm.ms.otp.service.OneTimePasswordService;
import com.icthh.xm.ms.otp.domain.OneTimePassword;
import com.icthh.xm.ms.otp.repository.OneTimePasswordRepository;
import com.icthh.xm.ms.otp.service.OtpSpecService;
import com.icthh.xm.ms.otp.service.dto.OneTimePasswordDTO;
import com.icthh.xm.ms.otp.service.mapper.OneTimePasswordMapper;
import com.mifmif.common.regex.Generex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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

    public OneTimePasswordServiceImpl(OneTimePasswordRepository oneTimePasswordRepository,
                                      OneTimePasswordMapper oneTimePasswordMapper,
                                      OtpSpecService otpSpecService) {
        this.oneTimePasswordRepository = oneTimePasswordRepository;
        this.oneTimePasswordMapper = oneTimePasswordMapper;
        this.otpSpecService = otpSpecService;
    }

    /**
     * Save a oneTimePassword.
     *
     * @param oneTimePasswordDTO the entity to save
     * @return the persisted entity
     */
    @Override
    public OneTimePasswordDTO save(OneTimePasswordDTO oneTimePasswordDTO) {
        log.debug("Request to save OneTimePassword : {}", oneTimePasswordDTO);

        List<OtpSpec.OtpTypeSpec> types = otpSpecService.getOtpSpec().getTypes();
        OtpSpec.OtpTypeSpec oneType = null;
        for (OtpSpec.OtpTypeSpec type : types) {
            if (type.getKey().equals(oneTimePasswordDTO.getTypeKey())) {
                oneType = type;
            }
        }
        if (oneType == null) {
            throw new IllegalArgumentException(
                String.format("Profile %s not found", oneTimePasswordDTO.getTypeKey())
            );
        }

        Generex generex = new Generex(oneType.getPattern());
        String randomStr = generex.random();

        //3. save
        OneTimePassword oneTimePassword = new OneTimePassword();
        long now = new Date().getTime();
        Instant startDate = Instant.ofEpochMilli(now);
        Instant endDate = Instant.ofEpochMilli(now + oneType.getTtl());
        oneTimePassword.startDate(startDate);
        oneTimePassword.setEndDate(endDate);
        oneTimePassword.setPasswordHash(randomStr);

        //4. send through communication ms (bean loadBalancedRestTemplate)

        return oneTimePasswordMapper.toDto(oneTimePassword);
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
