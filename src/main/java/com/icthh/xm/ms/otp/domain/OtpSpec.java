package com.icthh.xm.ms.otp.domain;

import com.icthh.xm.ms.otp.client.domain.CommunicationMessage.CommunicationMessageCharacteristic;
import com.icthh.xm.ms.otp.domain.enumeration.ReceiverTypeKey;
import com.icthh.xm.ms.otp.service.dto.OneTimePasswordDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.SortedMap;

@Data
public class OtpSpec {
    private List<OtpTypeSpec> types;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class OtpTypeSpec {
        private String key;
        private String pattern;
        private ReceiverTypeKey receiverTypeKey;
        private SortedMap<String, String> message;
        private Integer length;
        private Integer maxRetries;
        private Integer ttl;
        private String otpSenderId;

        /**
         * Message Template.
         *
         * Represent a path to a file stored in tenant config repository.
         */
        private String messageTemplate;

        /**
         * Available template model keys.
         *
         * Values for configured model keys will be looked up in
         * {@link OneTimePasswordDto#getModel()} and passed as {@link CommunicationMessageCharacteristic}
         * in request to communication.
         */
        private List<String> templateModelKeys;

        /**
         * Specification generation limit.
         */
        private GenerationLimit generationLimit;
    }

    /**
     * Specification generation limit.
     *
     * Used to limit how many times specification can be used by a given subscriber.
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GenerationLimit {
        /**
         * Period in seconds during which limit is applied.
         */
        private Integer periodSeconds;
        /**
         * Invocation limit value
         */
        private Integer value;
    }
}
