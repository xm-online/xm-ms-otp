package com.icthh.xm.ms.otp.service.impl;

import com.icthh.xm.ms.otp.domain.OtpSpec;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class MessageRenderingStrategyFactory {

    private final List<MessageRenderingStrategy> strategies;

    public MessageRenderingStrategyFactory(RawMessageRenderingStrategy rawMessageRenderingStrategy,
                                           TemplateMessageRenderingStrategy templateMessageRenderingStrategy) {
        this.strategies = List.of(
            templateMessageRenderingStrategy,
            rawMessageRenderingStrategy
        );
    }

    public MessageRenderingStrategy resolveStrategy(OtpSpec.OtpTypeSpec otpTypeSpec) {
        return strategies.stream()
            .filter(strategy -> strategy.isApplicable(otpTypeSpec))
            .findFirst()
            .orElse(null);
    }
}
