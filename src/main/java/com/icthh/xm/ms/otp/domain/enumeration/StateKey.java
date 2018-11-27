package com.icthh.xm.ms.otp.domain.enumeration;

import com.fasterxml.jackson.annotation.JsonValue;

public enum StateKey {

    ACTIVE("ACTIVE"),
    VERIFIED("VERIFIED");

    private final String value;

    StateKey(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

}
