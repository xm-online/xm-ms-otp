package com.icthh.xm.ms.otp.client.domain;

import lombok.Data;

import java.util.List;

@Data
public class CommunicationMessage {

    private List<Receiver> receiver;
    private String content;
    private String type;
    private Sender sender;
}
