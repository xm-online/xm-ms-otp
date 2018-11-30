package com.icthh.xm.ms.otp.service.impl;

import com.icthh.xm.commons.lep.LogicExtensionPoint;
import com.icthh.xm.commons.lep.spring.LepService;
import com.icthh.xm.ms.otp.domain.OneTimePassword;
import com.icthh.xm.ms.otp.domain.OtpSpec;
import com.icthh.xm.ms.otp.domain.enumeration.StateKey;
import com.icthh.xm.ms.otp.lep.keyresolver.OtpTypeKeyResolver;
import com.icthh.xm.ms.otp.repository.OneTimePasswordRepository;
import com.icthh.xm.ms.otp.service.OneTimePasswordService;
import com.icthh.xm.ms.otp.service.OtpSpecService;
import com.icthh.xm.ms.otp.service.dto.OneTimePasswordCheckDto;
import com.icthh.xm.ms.otp.service.dto.OneTimePasswordDto;
import com.icthh.xm.ms.otp.service.mapper.OneTimePasswordMapper;
import com.mifmif.common.regex.Generex;
import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;
import com.icthh.xm.ms.otp.web.rest.errors.OtpInvalidPasswordException;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.time.Instant;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.icthh.xm.ms.otp.config.Constants.DEFAULT_FREMARKER_VERSION;

/**
 * Service Implementation for managing OneTimePassword.
 */
@Service
@Slf4j
@LepService(group = "service", name = "default")
public class OneTimePasswordServiceImpl implements OneTimePasswordService {

    public static final String OTP = "otp";
    public static final String TEMPLATE_NAME = "templateName";
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
     * @param oneTimePasswordDto the entity to generate
     * @return the persisted entity
     */
    @Override
    @Transactional
    @SneakyThrows
    @LogicExtensionPoint(value = "Generate", resolver = OtpTypeKeyResolver.class)
    public OneTimePasswordDto generate(OneTimePasswordDto oneTimePasswordDto) {
        log.debug("Request to generate OneTimePassword : {}", oneTimePasswordDto);
        OtpSpec.OtpTypeSpec otpType = otpSpecService.getOtpTypeSpec(oneTimePasswordDto.getTypeKey());

        //generate otp
        Generex generex = new Generex(otpType.getPattern());
        String randomPasswrd = generex.random();

        //build domain
        OneTimePassword otp = getOneTimePassword(oneTimePasswordDto, otpType, randomPasswrd);

        String message = renderMessage(otpType, randomPasswrd);

        oneTimePasswordRepository.saveAndFlush(otp);

        communicationService.sendOneTimePassword(message, otp.getReceiver(), otpType.getOtpSenderId());
        return oneTimePasswordMapper.toDto(otp);
    }

    private String renderMessage(OtpSpec.OtpTypeSpec oneType,
                                 String randomPasswrd) throws IOException, TemplateException {
        Map<String, Object> model = new HashMap<>();
        model.put(OTP, randomPasswrd);
        Configuration cfg = new Configuration(DEFAULT_FREMARKER_VERSION);
        cfg.setObjectWrapper(new DefaultObjectWrapper(DEFAULT_FREMARKER_VERSION));
        Template t = new Template(TEMPLATE_NAME, new StringReader(oneType.getMessage().getEn()), cfg);
        Writer out = new StringWriter();
        t.process(model, out);
        return out.toString();
    }

    private OneTimePassword getOneTimePassword(OneTimePasswordDto oneTimePasswordDto,
                                               OtpSpec.OtpTypeSpec oneType,
                                               String randomPasswrd) {
        String sha256hex = DigestUtils.sha256Hex(randomPasswrd);
        Instant startDate = Instant.now();
        Instant endDate = startDate.plusSeconds(oneType.getTtl());
        OneTimePassword oneTimePassword = OneTimePassword.builder()
            .startDate(startDate)
            .endDate(endDate)
            .receiverTypeKey(oneType.getReceiverTypeKey())
            .receiverTypeKey(oneType.getReceiverTypeKey())
            .retries(oneType.getMaxRetries())
            .typeKey(oneTimePasswordDto.getTypeKey())
            .receiver(oneTimePasswordDto.getReceiver())
            .passwordHash(sha256hex)
            .stateKey(StateKey.ACTIVE)
            .build();
        return oneTimePassword;
    }

    @Override
    public void check(OneTimePasswordCheckDto oneTimePasswordCheckDto) {

        OneTimePassword otp = oneTimePasswordRepository.getOne(oneTimePasswordCheckDto.getId());

        if (checkOtpState(otp)
            || checkOtpDate(otp)
            || checkOtpRetries(otp)
            || checkOtpPasswd(otp, oneTimePasswordCheckDto)) {

            //if not - retries+
            int retries = otp.getRetries();
            otp.setRetries(++retries);
            oneTimePasswordRepository.save(otp);
            throw new OtpInvalidPasswordException();
        } else {
            otp.setStateKey(StateKey.VERIFIED);
            oneTimePasswordRepository.save(otp);
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
