package com.icthh.xm.ms.otp.service.impl;

import static com.icthh.xm.ms.otp.client.domain.Receiver.phoneReceiver;
import static com.icthh.xm.ms.otp.config.Constants.DEFAULT_FREMARKER_VERSION;

import com.icthh.xm.ms.otp.client.domain.CommunicationMessage;
import com.icthh.xm.ms.otp.client.domain.Sender;
import com.icthh.xm.ms.otp.domain.OtpSpec.OtpTypeSpec;
import com.icthh.xm.ms.otp.service.dto.OneTimePasswordDto;
import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.Template;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RawCommunicationRequestStrategy implements CommunicationRequestStrategy {

    private final static String SMS_MESSAGE_TYPE = "SMS";

    @Override
    public CommunicationMessage prepareRequest(String otp,
                                               OtpTypeSpec otpTypeSpec,
                                               OneTimePasswordDto otpDto) {
        log.info("prepareRequest: otpTypeSpec: {} otpDto: {}", otpTypeSpec, otpDto);

        SortedMap<String, String> messageConfig = otpTypeSpec.getMessage();
        if (MapUtils.isEmpty(messageConfig)) {
            log.error("prepareRequest: Invalid OTP config: {}, no message", otpTypeSpec);
            throw new IllegalStateException("Missing configuration");
        }

        String langKey = otpDto.getLangKey();
        if (langKey == null || StringUtils.isEmpty(messageConfig.get(langKey))) {
            langKey = messageConfig.firstKey();
        }
        String messageText = messageConfig.get(langKey);

        Map<String, String> model = new HashMap<>();
        model.put(OTP_MODEL_KEY, otp);
        String renderedMessage = renderFromRawText(messageText, model);

        CommunicationMessage communicationMessage = new CommunicationMessage();
        communicationMessage.setContent(renderedMessage)
            .setType(SMS_MESSAGE_TYPE)
            .setSender(new Sender(otpTypeSpec.getOtpSenderId()))
            .setReceiver(List.of(phoneReceiver(otpDto.getReceiver())));
        return communicationMessage;
    }

    @SneakyThrows
    private String renderFromRawText(String messageText, Map<String, String> model) {
        Configuration cfg = new Configuration(DEFAULT_FREMARKER_VERSION);
        cfg.setObjectWrapper(new DefaultObjectWrapper(DEFAULT_FREMARKER_VERSION));
        Template t = new Template(TEMPLATE_NAME, new StringReader(messageText), cfg);
        Writer out = new StringWriter();
        t.process(model, out);
        return out.toString();
    }

    @Override
    public boolean isApplicable(OtpTypeSpec otpTypeSpec, String langKey) {
        return MapUtils.isNotEmpty(otpTypeSpec.getMessage());
    }
}
