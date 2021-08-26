package com.icthh.xm.ms.otp.client.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
public class CommunicationMessage {

    private List<Receiver> receiver;
    private String content;
    private String type;
    private Sender sender;

    private List<CommunicationMessageCharacteristic> characteristics;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CommunicationMessageCharacteristic {
        private String name;
        private String value;
    }
}
