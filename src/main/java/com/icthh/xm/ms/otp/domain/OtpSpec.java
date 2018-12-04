package com.icthh.xm.ms.otp.domain;

import com.icthh.xm.ms.otp.domain.enumeration.ReceiverTypeKey;
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
    }
}
