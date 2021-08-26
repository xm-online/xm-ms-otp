package com.icthh.xm.ms.otp.service.impl;

import com.icthh.xm.ms.otp.domain.OtpSpec.OtpTypeSpec;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class CommunicationMessageStrategyFactory {

    private final List<CommunicationRequestStrategy> strategies;

    /**
     * Create CommunicationMessageStrategyFactory, define strategies list.
     * Element order matters, RawCommunicationRequestStrategy as default one must be the last in the list.
     *
     * @param templateRequestStrategy Template request strategy
     * @param rawRequestStrategy Raw message request strategy
     */
    public CommunicationMessageStrategyFactory(TemplateCommunicationRequestStrategy templateRequestStrategy,
                                               RawCommunicationRequestStrategy rawRequestStrategy) {
        // order matters, rawRequestStrategy must be the last one
        this.strategies = List.of(
            templateRequestStrategy,
            rawRequestStrategy
        );
    }

    public CommunicationRequestStrategy resolveStrategy(OtpTypeSpec otpTypeSpec, String langKey) {
        return strategies.stream()
            .filter(strategy -> strategy.isApplicable(otpTypeSpec, langKey))
            .findFirst()
            .orElse(null);
    }
}
