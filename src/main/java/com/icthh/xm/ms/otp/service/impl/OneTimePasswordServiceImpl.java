package com.icthh.xm.ms.otp.service.impl;

import static java.util.Optional.ofNullable;

import com.icthh.xm.commons.lep.LogicExtensionPoint;
import com.icthh.xm.commons.lep.spring.LepService;
import com.icthh.xm.ms.otp.domain.OneTimePassword;
import com.icthh.xm.ms.otp.domain.OtpSpec.OtpTypeSpec;
import com.icthh.xm.ms.otp.domain.enumeration.StateKey;
import com.icthh.xm.ms.otp.lep.keyresolver.OtpTypeKeyResolver;
import com.icthh.xm.ms.otp.repository.OneTimePasswordRepository;
import com.icthh.xm.ms.otp.service.CommunicationService;
import com.icthh.xm.ms.otp.service.OneTimePasswordService;
import com.icthh.xm.ms.otp.service.OtpSpecService;
import com.icthh.xm.ms.otp.service.dto.OneTimePasswordCheckDto;
import com.icthh.xm.ms.otp.service.dto.OneTimePasswordDto;
import com.icthh.xm.ms.otp.service.mapper.OneTimePasswordMapper;
import com.icthh.xm.ms.otp.web.rest.errors.OtpInvalidPasswordException;
import com.mifmif.common.regex.Generex;
import java.math.BigInteger;
import java.time.Instant;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing OneTimePassword.
 */
@Service
@Slf4j
@RequiredArgsConstructor
@LepService(group = "service", name = "default")
public class OneTimePasswordServiceImpl implements OneTimePasswordService {

    private static final String OTP = "otp";
    private final OtpSpecService otpSpecService;

    private final OneTimePasswordRepository oneTimePasswordRepository;

    private final OneTimePasswordMapper oneTimePasswordMapper;

    private final CommunicationService communicationService;

    private final MessageRenderingStrategyFactory messageRenderingFactory;

    /**
     * Save a oneTimePassword.
     *
     * @param oneTimePasswordDto the entity to generate
     * @return the persisted entity
     */
    @Override
    @Transactional
    @SneakyThrows
    @LogicExtensionPoint(value = "Generate", resolver = OtpTypeKeyResolver.class)
    public OneTimePasswordDto generate(OneTimePasswordDto oneTimePasswordDto) {
        log.debug("Request to generate OneTimePassword : {}", oneTimePasswordDto);
        OtpTypeSpec otpType = otpSpecService.getOtpTypeSpec(oneTimePasswordDto.getTypeKey());

        //generate otp
        Generex generex = new Generex(otpType.getPattern());
        String randomPassword = generex.random();

        //build domain
        OneTimePassword otp = getOneTimePassword(oneTimePasswordDto, otpType, randomPassword);

        String message = renderMessage(otpType, randomPassword, oneTimePasswordDto);

        oneTimePasswordRepository.saveAndFlush(otp);

        communicationService.sendOneTimePassword(message, otp.getReceiver(),
            otpType.getOtpSenderId(), otp.getReceiverTypeKey());

        OneTimePasswordDto otpResult = oneTimePasswordMapper.toDto(otp);

        otpResult.setLangKey(oneTimePasswordDto.getLangKey());

        return otpResult;
    }

    protected String renderMessage(OtpTypeSpec otpTypeSpec,
                                   String randomPassword,
                                   OneTimePasswordDto oneTimePasswordDto) {
        MessageRenderingStrategy messageRenderingStrategy = ofNullable(messageRenderingFactory.resolveStrategy(otpTypeSpec))
            .orElseThrow(() ->
                new IllegalArgumentException("Failed to resolve message rendering strategy for spec: " + otpTypeSpec));

        Map<String, Object> model = messageRenderingStrategy.prepareModel(randomPassword, oneTimePasswordDto);

        return messageRenderingStrategy.render(otpTypeSpec, oneTimePasswordDto.getLangKey(), model);
    }

    private OneTimePassword getOneTimePassword(OneTimePasswordDto oneTimePasswordDto,
                                               OtpTypeSpec oneType,
                                               String randomPasswrd) {
        String sha256hex = DigestUtils.sha256Hex(randomPasswrd);
        Instant startDate = Instant.now();
        Instant endDate = startDate.plusSeconds(oneType.getTtl());
        return OneTimePassword.builder()
            .startDate(startDate)
            .endDate(endDate)
            .receiverTypeKey(oneType.getReceiverTypeKey())
            .receiverTypeKey(oneType.getReceiverTypeKey())
            .retries(BigInteger.ZERO.intValue())
            .typeKey(oneTimePasswordDto.getTypeKey())
            .receiver(oneTimePasswordDto.getReceiver())
            .passwordHash(sha256hex)
            .stateKey(StateKey.ACTIVE)
            .build();
    }

    @Override
    public void check(OneTimePasswordCheckDto oneTimePasswordCheckDto) {

        OneTimePassword otp = oneTimePasswordRepository.findById(oneTimePasswordCheckDto.getId())
            .orElseThrow(OtpInvalidPasswordException::new);

        if (checkOtpState(otp)
            || checkOtpDate(otp)
            || checkOtpRetries(otp)
            || checkOtpPasswd(otp, oneTimePasswordCheckDto)) {

            //if not - retries+
            int retries = otp.getRetries();
            otp.setRetries(++retries);
            oneTimePasswordRepository.saveAndFlush(otp);
            throw new OtpInvalidPasswordException();
        } else {
            otp.setStateKey(StateKey.VERIFIED);
            oneTimePasswordRepository.saveAndFlush(otp);
        }
    }

    private boolean checkOtpState(OneTimePassword otp) {
        return otp.getStateKey() != StateKey.ACTIVE;
    }

    private boolean checkOtpDate(OneTimePassword otp) {
        return otp.getEndDate().isBefore(Instant.now());
    }

    private boolean checkOtpRetries(OneTimePassword otp) {
        return otp.getRetries() >= otpSpecService.getOtpTypeSpec(otp.getTypeKey()).getMaxRetries();
    }

    private boolean checkOtpPasswd(OneTimePassword otp, OneTimePasswordCheckDto otpForCheck) {
        return !otp.getPasswordHash().equals(DigestUtils.sha256Hex(otpForCheck.getOtp()));
    }

    /**
     * Get all the oneTimePasswords.
     *
     * @return the list of entities
     */
    @Override
    @Transactional(readOnly = true)
    public List<OneTimePasswordDto> findAll() {
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
    public Optional<OneTimePasswordDto> findOne(Long id) {
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
