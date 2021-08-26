package com.icthh.xm.ms.otp.client.domain;

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
}
