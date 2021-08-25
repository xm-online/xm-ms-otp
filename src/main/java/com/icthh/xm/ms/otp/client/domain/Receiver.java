package com.icthh.xm.ms.otp.client.domain;

import com.icthh.xm.ms.otp.domain.enumeration.ReceiverTypeKey;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Receiver {
    private String email;
    private String phoneNumber;
    private String id;

    public static Receiver emailReceiver(String email) {
        return new Receiver(email, null, email);
    }

    public static Receiver phoneReceiver(String phoneNumber) {
        return new Receiver(null, phoneNumber, phoneNumber);
    }

    public static Receiver fromReceiverType(ReceiverTypeKey receiverTypeKey, String receiverId) {
        switch (receiverTypeKey) {
            case PHONE_NUMBER:
                return phoneReceiver(receiverId);
            case EMAIL:
                return emailReceiver(receiverId);
            default:
                throw new IllegalArgumentException("Unsupported receiver type: " + receiverTypeKey);
        }
    }
}
