package com.icthh.xm.ms.otp.client.domain;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
public class CommunicationMessage {

    private List<Receiver> receiver;
    private String content;
    private String type;
    private Sender sender;
}
