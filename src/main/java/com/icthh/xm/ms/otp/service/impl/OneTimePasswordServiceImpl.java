package com.icthh.xm.ms.otp.service.impl;

import com.google.common.hash.Hashing;
import com.icthh.xm.commons.config.client.repository.TenantConfigRepository;
import com.icthh.xm.ms.otp.domain.OtpSpec;
import com.icthh.xm.ms.otp.service.OneTimePasswordService;
import com.icthh.xm.ms.otp.domain.OneTimePassword;
import com.icthh.xm.ms.otp.domain.OtpSpec;
import com.icthh.xm.ms.otp.repository.OneTimePasswordRepository;
import com.icthh.xm.ms.otp.service.OneTimePasswordService;
import com.icthh.xm.ms.otp.service.OtpSpecService;
import com.icthh.xm.ms.otp.service.dto.OneTimePasswordCheckDTO;
import com.icthh.xm.ms.otp.service.dto.OneTimePasswordDTO;
import com.icthh.xm.ms.otp.service.mapper.OneTimePasswordMapper;
import com.mifmif.common.regex.Generex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.RequestEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
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

    public OneTimePasswordServiceImpl(OneTimePasswordRepository oneTimePasswordRepository,
                                      OneTimePasswordMapper oneTimePasswordMapper,
                                      OtpSpecService otpSpecService,
                                      RestTemplate loadBalancedRestTemplate) {
        this.oneTimePasswordRepository = oneTimePasswordRepository;
        this.oneTimePasswordMapper = oneTimePasswordMapper;
        this.otpSpecService = otpSpecService;
        this.loadBalancedRestTemplate = loadBalancedRestTemplate;
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

        OtpSpec.OtpTypeSpec oneType = getOneTypeSpec(oneTimePasswordDTO.getTypeKey());

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


        String sha256hex = Hashing.sha256()
            .hashString(randomStr, StandardCharsets.UTF_8)
            .toString();

        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, "sdfsd");

        String url = "http://communication/tmf-api/communicationManagement/v2/communicationMessage/send";

        RequestEntity<Object> objectRequestEntity = new RequestEntity<>(null,headers, POST, URI.create(url));
        loadBalancedRestTemplate.exchange(objectRequestEntity, Object.class);

        //4. send through communication ms (bean loadBalancedRestTemplate)

        return oneTimePasswordMapper.toDto(oneTimePassword);
    }

    @Override
    public OneTimePasswordCheckDTO check(OneTimePasswordCheckDTO oneTimePasswordDTO) {
        //1. get otp from db

        //2. check state (must be ACTIVE)

        //3. check expiration date

        //4. check retries < maxRetries

        //5. check otp (hash 256)
        //if success -> change state to VERIFIED
        //if not - retries+

        return null;
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
