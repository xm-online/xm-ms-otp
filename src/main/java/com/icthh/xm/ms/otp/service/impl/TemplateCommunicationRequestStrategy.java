package com.icthh.xm.ms.otp.service.impl;

import static com.icthh.xm.ms.otp.client.domain.Receiver.emailReceiver;

import com.icthh.xm.ms.otp.client.domain.CommunicationMessage;
import com.icthh.xm.ms.otp.client.domain.CommunicationMessage.CommunicationMessageCharacteristic;
import com.icthh.xm.ms.otp.client.domain.Sender;
import com.icthh.xm.ms.otp.domain.OtpSpec.OtpTypeSpec;
import com.icthh.xm.ms.otp.service.dto.OneTimePasswordDto;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TemplateCommunicationRequestStrategy implements CommunicationRequestStrategy {

    private final static String TEMPLATED_EMAIL_MESSAGE_TYPE = "TemplatedEmail";
    private static final String LANGUAGE_MODEL_KEY = "language";

    @Override
    public CommunicationMessage prepareRequest(String otp, OtpTypeSpec otpTypeSpec, OneTimePasswordDto otpDto) {
        log.info("prepareRequest: otpTypeSpec: {} otpDto: {}", otpTypeSpec, otpDto);

        List<CommunicationMessageCharacteristic> characteristics =
            toCharacteristicList(otp, otpTypeSpec, otpDto);

        CommunicationMessage communicationMessage = new CommunicationMessage();
        communicationMessage
            .setType(TEMPLATED_EMAIL_MESSAGE_TYPE)
            .setContent(otpTypeSpec.getMessageTemplate())
            .setSender(new Sender(otpTypeSpec.getOtpSenderId()))
            .setReceiver(List.of(emailReceiver(otpDto.getReceiver())))
            .setCharacteristic(characteristics);
        return communicationMessage;
    }

    @Override
    public boolean isApplicable(OtpTypeSpec otpTypeSpec, String langKey) {
        return StringUtils.isNoneBlank(otpTypeSpec.getMessageTemplate(), langKey);
    }

    private List<CommunicationMessageCharacteristic> toCharacteristicList(String otp,
                                                                          OtpTypeSpec otpTypeSpec,
                                                                          OneTimePasswordDto otpDto) {

        List<CommunicationMessageCharacteristic> defaultCharacteristics = List.of(
            new CommunicationMessageCharacteristic(OTP_MODEL_KEY, otp),
            new CommunicationMessageCharacteristic(TEMPLATE_NAME, otpTypeSpec.getMessageTemplate()),
            new CommunicationMessageCharacteristic(LANGUAGE_MODEL_KEY, otpDto.getLangKey())
        );
        List<CommunicationMessageCharacteristic> result = new ArrayList<>(defaultCharacteristics);

        Map<String, Object> modelParams = otpDto.getModel();
        if (MapUtils.isNotEmpty(modelParams)) {
            List<CommunicationMessageCharacteristic> modelCharacteristics = otpTypeSpec.getTemplateModelKeys().stream()
                .filter(modelParams::containsKey)
                .map(modelKey -> new CommunicationMessageCharacteristic(modelKey, String.valueOf(modelParams.get(modelKey))))
                .collect(Collectors.toList());
            result.addAll(modelCharacteristics);
        }

        return result;
    }
}
