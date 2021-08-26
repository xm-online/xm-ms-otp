package com.icthh.xm.ms.otp.service.impl;

import static com.icthh.xm.ms.otp.client.domain.Receiver.emailReceiver;

import com.icthh.xm.ms.otp.client.domain.CommunicationMessage;
import com.icthh.xm.ms.otp.client.domain.CommunicationMessage.CommunicationMessageCharacteristic;
import com.icthh.xm.ms.otp.client.domain.Sender;
import com.icthh.xm.ms.otp.domain.OtpSpec.OtpTypeSpec;
import com.icthh.xm.ms.otp.service.dto.OneTimePasswordDto;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TemplateCommunicationRequestStrategy implements CommunicationRequestStrategy {

    private final static String MSISDN_MODEL_KEY = "msisdn";
    private final static String TEMPLATED_EMAIL_MESSAGE_TYPE = "TemplatedEmail";
    private static final String LANGUAGE_MODEL_KEY = "language";

    @Override
    public CommunicationMessage prepareRequest(String otp, OtpTypeSpec otpTypeSpec, OneTimePasswordDto otpDto) {
        log.info("prepareRequest: otpTypeSpec: {} otpDto: {}", otpTypeSpec, otpDto);

        List<CommunicationMessageCharacteristic> characteristics = toCharacteristicList(otp,
            otpDto.getLangKey(),
            otpDto.getReceiver(),
            otpTypeSpec.getMessageTemplate());

        CommunicationMessage communicationMessage = new CommunicationMessage();
        communicationMessage
            .setType(TEMPLATED_EMAIL_MESSAGE_TYPE)
            .setSender(new Sender(otpTypeSpec.getOtpSenderId()))
            .setReceiver(List.of(emailReceiver(otpDto.getReceiver())))
            .setCharacteristics(characteristics);
        return communicationMessage;
    }

    @Override
    public boolean isApplicable(OtpTypeSpec otpTypeSpec, String langKey) {
        return StringUtils.isNoneBlank(otpTypeSpec.getMessageTemplate(), langKey);
    }

    private List<CommunicationMessageCharacteristic> toCharacteristicList(String otp,
                                                                          String langKey,
                                                                          String receiver,
                                                                          String messageTemplate) {
        return List.of(
            new CommunicationMessageCharacteristic(OTP_MODEL_KEY, otp),
            new CommunicationMessageCharacteristic(MSISDN_MODEL_KEY, receiver),
            new CommunicationMessageCharacteristic(TEMPLATE_NAME, messageTemplate),
            new CommunicationMessageCharacteristic(LANGUAGE_MODEL_KEY, langKey)
        );
    }
}
